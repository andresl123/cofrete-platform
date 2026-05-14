package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.auth.AppUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ReserveService {

    private static final String BRL = "BRL";

    private final ReserveRuleRepository rules;
    private final ReserveWalletRepository wallets;
    private final ReserveAllocationRepository allocations;
    private final ReserveTransactionRepository transactions;
    private final ReserveAllocationEventPublisher allocationEvents;

    ReserveService(
        ReserveRuleRepository rules,
        ReserveWalletRepository wallets,
        ReserveAllocationRepository allocations,
        ReserveTransactionRepository transactions,
        ReserveAllocationEventPublisher allocationEvents
    ) {
        this.rules = rules;
        this.wallets = wallets;
        this.allocations = allocations;
        this.transactions = transactions;
        this.allocationEvents = allocationEvents;
    }

    @Transactional
    ReserveRuleResponse createOrUpdateRule(AppUser user, ReserveRuleRequest request) {
        validateRule(request);
        String accountId = user.getAccountId();
        var rule = activeRule(accountId, request.bucket());
        rule.updateFrom(request);
        ReserveRule saved;
        try {
            saved = rules.saveAndFlush(rule);
        } catch (DataIntegrityViolationException exception) {
            throw new ReserveConflictException("An active reserve rule already exists for bucket " + request.bucket() + ".");
        }
        var wallet = wallet(accountId, saved.getBucket(), saved.getCurrency());
        wallet.setTargetBalance(saved.getTargetBalance());
        wallets.save(wallet);
        return ReserveRuleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    List<ReserveWalletResponse> listWallets(AppUser user) {
        String accountId = user.getAccountId();
        Map<ReserveBucket, ReserveRule> activeRules = activeRulesByBucket(accountId);
        return wallets.findByAccountIdOrderByBucketAsc(accountId).stream()
            .map(wallet -> ReserveWalletResponse.from(
                wallet,
                activeRules.get(wallet.getBucket()),
                transactions.findTop20ByAccountIdAndWalletOrderByCreatedAtDesc(accountId, wallet)
            ))
            .toList();
    }

    @Transactional
    ReserveAllocationResponse allocate(AppUser user, ReserveAllocationRequest request) {
        validateAllocationRequest(request);
        String accountId = user.getAccountId();
        String fingerprint = fingerprint(request);
        var duplicate = allocations.findByAccountIdAndIdempotencyKey(accountId, request.idempotencyKey());
        if (duplicate.isPresent()) {
            var existing = duplicate.get();
            if (!existing.hasSameFingerprint(fingerprint)) {
                throw new ReserveConflictException("Idempotency key already exists with different allocation inputs.");
            }
            var existingTransactions = transactions.findByAllocationOrderByBucketAsc(existing);
            return ReserveAllocationResponse.from(
                existing,
                ReserveAllocationRequestStatus.DUPLICATE_IGNORED,
                bucketAmounts(existingTransactions),
                existingTransactions
            );
        }

        List<ReserveRule> activeRules = rules.findByAccountIdAndActiveOrderByBucketAsc(accountId, true);
        validateUniqueActiveRuleBuckets(activeRules);
        if (activeRules.isEmpty()) {
            throw new ReserveValidationException("At least one active reserve rule is required before allocation.");
        }
        if (request.distanceKm() == null && activeRules.stream().anyMatch(rule -> rule.getPolicy() == ReserveRulePolicy.PER_KM)) {
            throw new ReserveValidationException("distanceKm is required for active PER_KM reserve rules.");
        }

        BigDecimal gross = ReserveMoney.money(request.grossAmount(), "grossAmount");
        BigDecimal passThrough = ReserveMoney.money(request.passThroughAmount(), "passThroughAmount");
        if (passThrough.compareTo(gross) > 0) {
            throw new ReserveValidationException("passThroughAmount cannot exceed grossAmount.");
        }
        BigDecimal allocatable = gross.subtract(passThrough);
        var allocation = allocations.save(new ReserveAllocation(
            accountId,
            request,
            fingerprint,
            allocatable,
            ReserveAllocationStatus.REQUESTED
        ));

        allocationEvents.publish(ReserveAllocationRequestedEvent.from(
            accountId,
            user.getId(),
            request,
            activeRules
        ));

        var allocationTransactions = transactions.findByAllocationOrderByBucketAsc(allocation);
        return ReserveAllocationResponse.from(
            allocation,
            ReserveAllocationRequestStatus.REQUESTED,
            Map.of(),
            allocationTransactions
        );
    }

    @Transactional
    ReserveAllocationResponse persistWorkerResult(ReserveAllocationCompletedEvent event) {
        validateResultEvent(event);
        var latestForSubject = allocations
            .findTopByAccountIdAndAllocationSubjectIdOrderByAllocationRevisionDesc(
                event.accountId(),
                event.allocationSubjectId()
            );
        if (latestForSubject.isPresent() && event.allocationRevision() < latestForSubject.get().getAllocationRevision()) {
            return ReserveAllocationResponse.from(
                latestForSubject.get(),
                ReserveAllocationRequestStatus.DUPLICATE_IGNORED,
                Map.of(),
                transactions.findByAllocationOrderByBucketAsc(latestForSubject.get())
            );
        }
        ReserveAllocation allocation = allocations.findByAccountIdAndIdempotencyKey(
                event.accountId(),
                event.idempotencyKey()
            )
            .orElseThrow(() -> new ReserveValidationException("Reserve allocation request not found for worker result."));
        if (!allocation.isRequested()) {
            var existingTransactions = transactions.findByAllocationOrderByBucketAsc(allocation);
            return ReserveAllocationResponse.from(
                allocation,
                ReserveAllocationRequestStatus.DUPLICATE_IGNORED,
                bucketAmounts(existingTransactions),
                existingTransactions
            );
        }
        if (event.allocationRevision() != allocation.getAllocationRevision()
            || !event.allocationSubjectId().equals(allocation.getAllocationSubjectId())) {
            throw new ReserveConflictException("Worker result does not match the reserve allocation request.");
        }
        if (!matchesRequestAmounts(allocation, event)) {
            throw new ReserveConflictException("Worker result amounts do not match the reserve allocation request.");
        }

        allocation.applyWorkerResult(event);
        allocations.save(allocation);
        event.bucketAllocations().forEach((bucket, value) -> {
            BigDecimal amount = ReserveMoney.money(new BigDecimal(value), "bucketAllocations." + bucket);
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                var wallet = wallet(event.accountId(), bucket, event.currency());
                wallet.credit(amount, event.allocatedAt());
                wallets.save(wallet);
                transactions.save(new ReserveTransaction(event.accountId(), wallet, allocation, amount, event.idempotencyKey()));
            }
        });

        var allocationTransactions = transactions.findByAllocationOrderByBucketAsc(allocation);
        return ReserveAllocationResponse.from(
            allocation,
            ReserveAllocationRequestStatus.ALLOCATED,
            bucketAmounts(allocationTransactions),
            allocationTransactions
        );
    }

    @Transactional(readOnly = true)
    FinancialHealthResponse financialHealth(AppUser user) {
        var userWallets = wallets.findByAccountIdOrderByBucketAsc(user.getAccountId());
        List<FinancialHealthComponentResponse> components = userWallets.stream()
            .map(this::healthComponent)
            .toList();
        var targeted = userWallets.stream()
            .filter(wallet -> wallet.getTargetBalance() != null && wallet.getTargetBalance().compareTo(BigDecimal.ZERO) > 0)
            .toList();
        BigDecimal totalBalance = targeted.stream()
            .map(ReserveWallet::getCurrentBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTarget = targeted.stream()
            .map(ReserveWallet::getTargetBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal coverage = totalTarget.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalBalance.multiply(new BigDecimal("100")).divide(totalTarget, 2, RoundingMode.HALF_UP);
        int score = totalTarget.compareTo(BigDecimal.ZERO) == 0
            ? 0
            : coverage.min(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
        FinancialHealthStatus status = totalTarget.compareTo(BigDecimal.ZERO) == 0
            ? FinancialHealthStatus.UNKNOWN
            : statusForCoverage(coverage);
        BigDecimal safeWithdrawal = userWallets.stream()
            .filter(wallet -> wallet.getBucket() == ReserveBucket.DRIVER_SALARY)
            .map(ReserveWallet::getCurrentBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinancialHealthResponse(
            score,
            status,
            coverage.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            ReserveMoney.money(safeWithdrawal),
            BRL,
            "financial_health_" + sha256Hex(user.getAccountId() + "|" + coverage + "|" + safeWithdrawal).substring(0, 20),
            components,
            "Financial health is advisory planning output, not legal, tax, accounting, insurance, or government guidance."
        );
    }

    private ReserveWallet wallet(String accountId, ReserveBucket bucket, String currency) {
        return wallets.findByAccountIdAndBucket(accountId, bucket)
            .orElseGet(() -> new ReserveWallet(accountId, bucket, currency));
    }

    private Map<ReserveBucket, ReserveRule> activeRulesByBucket(String accountId) {
        List<ReserveRule> activeRules = rules.findByAccountIdAndActiveOrderByBucketAsc(accountId, true);
        validateUniqueActiveRuleBuckets(activeRules);
        Map<ReserveBucket, ReserveRule> byBucket = new EnumMap<>(ReserveBucket.class);
        activeRules.forEach(rule -> byBucket.put(rule.getBucket(), rule));
        return byBucket;
    }

    private ReserveRule activeRule(String accountId, ReserveBucket bucket) {
        List<ReserveRule> activeRules = rules.findByAccountIdAndBucketAndActiveOrderByCreatedAtAsc(accountId, bucket, true);
        if (activeRules.size() > 1) {
            throw new ReserveConflictException("Multiple active reserve rules exist for bucket " + bucket + ".");
        }
        return activeRules.stream()
            .findFirst()
            .orElseGet(() -> new ReserveRule(accountId, new ReserveRuleRequest(
                bucket,
                ReserveRulePolicy.PERCENT_OF_AMOUNT,
                BigDecimal.ZERO,
                null,
                null,
                null,
                BRL,
                null,
                null,
                true,
                null
            )));
    }

    private static void validateUniqueActiveRuleBuckets(List<ReserveRule> activeRules) {
        var buckets = new HashSet<ReserveBucket>();
        for (ReserveRule rule : activeRules) {
            if (!buckets.add(rule.getBucket())) {
                throw new ReserveConflictException("Multiple active reserve rules exist for bucket " + rule.getBucket() + ".");
            }
        }
    }

    private FinancialHealthComponentResponse healthComponent(ReserveWallet wallet) {
        BigDecimal coverage = wallet.getTargetBalance() == null || wallet.getTargetBalance().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : wallet.getCurrentBalance()
                .multiply(new BigDecimal("100"))
                .divide(wallet.getTargetBalance(), 2, RoundingMode.HALF_UP);
        FinancialHealthStatus status = wallet.getTargetBalance() == null || wallet.getTargetBalance().compareTo(BigDecimal.ZERO) == 0
            ? FinancialHealthStatus.UNKNOWN
            : statusForCoverage(coverage);
        return new FinancialHealthComponentResponse(
            wallet.getBucket(),
            ReserveMoney.money(wallet.getCurrentBalance()),
            ReserveMoney.optionalMoney(wallet.getTargetBalance()),
            coverage.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            status
        );
    }

    private static FinancialHealthStatus statusForCoverage(BigDecimal coverage) {
        if (coverage.compareTo(new BigDecimal("80.00")) >= 0) {
            return FinancialHealthStatus.GOOD;
        }
        if (coverage.compareTo(new BigDecimal("50.00")) >= 0) {
            return FinancialHealthStatus.ATTENTION;
        }
        return FinancialHealthStatus.RISK;
    }

    private static void validateRule(ReserveRuleRequest request) {
        if (request.currency() != null && !BRL.equals(request.currency())) {
            throw new ReserveValidationException("currency must be BRL.");
        }
        switch (request.policy()) {
            case PERCENT_OF_AMOUNT -> {
                if (request.rate() == null || request.rate().compareTo(BigDecimal.ONE) > 0) {
                    throw new ReserveValidationException("rate is required and must be between 0 and 1.");
                }
                if (request.fixedAmount() != null || request.perKmAmount() != null) {
                    throw new ReserveValidationException("PERCENT_OF_AMOUNT reserve rules only accept rate.");
                }
            }
            case FIXED_AMOUNT -> {
                if (request.fixedAmount() == null) {
                    throw new ReserveValidationException("fixedAmount is required for FIXED_AMOUNT reserve rules.");
                }
                if (request.rate() != null || request.perKmAmount() != null) {
                    throw new ReserveValidationException("FIXED_AMOUNT reserve rules only accept fixedAmount.");
                }
            }
            case PER_KM -> {
                if (request.perKmAmount() == null) {
                    throw new ReserveValidationException("perKmAmount is required for PER_KM reserve rules.");
                }
                if (request.rate() != null || request.fixedAmount() != null) {
                    throw new ReserveValidationException("PER_KM reserve rules only accept perKmAmount.");
                }
            }
        }
    }

    private static void validateAllocationRequest(ReserveAllocationRequest request) {
        if (!BRL.equals(request.currency())) {
            throw new ReserveValidationException("currency must be BRL.");
        }
    }

    private static void validateResultEvent(ReserveAllocationCompletedEvent event) {
        if (event == null) {
            throw new ReserveValidationException("Reserve allocation result event is required.");
        }
        if (!ReserveAllocationCompletedEvent.EVENT_TYPE.equals(event.eventType())) {
            throw new ReserveValidationException("Expected eventType " + ReserveAllocationCompletedEvent.EVENT_TYPE + ".");
        }
        if (event.version() != ReserveAllocationCompletedEvent.VERSION) {
            throw new ReserveValidationException("reserve.allocation.completed version must be 1.");
        }
        if (!BRL.equals(event.currency())) {
            throw new ReserveValidationException("currency must be BRL.");
        }
        if (!ReserveAllocationCompletedEvent.PRODUCER.equals(event.producer())) {
            throw new ReserveValidationException("producer must be finance-worker.");
        }
        if (event.allocatedAt() == null) {
            throw new ReserveValidationException("allocatedAt is required.");
        }
        if (event.bucketAllocations() == null || event.bucketAllocations().isEmpty()) {
            throw new ReserveValidationException("bucketAllocations are required.");
        }
    }

    private static boolean matchesRequestAmounts(ReserveAllocation allocation, ReserveAllocationCompletedEvent event) {
        BigDecimal gross = new BigDecimal(event.grossAmount());
        BigDecimal passThrough = new BigDecimal(event.passThroughAmount());
        BigDecimal allocatable = new BigDecimal(event.allocatableAmount());
        return allocation.getGrossAmount().compareTo(gross) == 0
            && allocation.getPassThroughAmount().compareTo(passThrough) == 0
            && allocation.getAllocatableAmount().compareTo(allocatable) == 0
            && allocation.getCurrency().equals(event.currency());
    }

    private static String fingerprint(ReserveAllocationRequest request) {
        String canonical = String.join("|",
            nullToEmpty(request.tripId()),
            nullToEmpty(request.freightPaymentId()),
            nullToEmpty(request.allocationSubjectId()),
            Integer.toString(request.allocationRevision()),
            ReserveMoney.money(request.grossAmount(), "grossAmount").toPlainString(),
            ReserveMoney.money(request.passThroughAmount(), "passThroughAmount").toPlainString(),
            request.distanceKm() == null ? "" : ReserveMoney.distance(request.distanceKm()).toPlainString(),
            request.currency(),
            request.idempotencyKey(),
            request.reason().name()
        );
        return sha256Hex(canonical);
    }

    private static Map<ReserveBucket, String> bucketAmounts(List<ReserveTransaction> transactions) {
        Map<ReserveBucket, String> amounts = new TreeMap<>();
        transactions.forEach(transaction -> amounts.put(transaction.getBucket(), ReserveMoney.money(transaction.getAmount())));
        return amounts;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
