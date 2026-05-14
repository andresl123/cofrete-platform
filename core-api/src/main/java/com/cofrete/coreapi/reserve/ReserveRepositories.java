package com.cofrete.coreapi.reserve;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReserveRuleRepository extends JpaRepository<ReserveRule, String> {

    List<ReserveRule> findByAccountIdAndActiveOrderByBucketAsc(String accountId, boolean active);

    List<ReserveRule> findByAccountIdAndBucketAndActiveOrderByCreatedAtAsc(
        String accountId,
        ReserveBucket bucket,
        boolean active
    );
}

interface ReserveWalletRepository extends JpaRepository<ReserveWallet, String> {

    List<ReserveWallet> findByAccountIdOrderByBucketAsc(String accountId);

    Optional<ReserveWallet> findByAccountIdAndBucket(String accountId, ReserveBucket bucket);
}

interface ReserveAllocationRepository extends JpaRepository<ReserveAllocation, String> {

    Optional<ReserveAllocation> findByAccountIdAndIdempotencyKey(String accountId, String idempotencyKey);

    Optional<ReserveAllocation> findTopByAccountIdAndAllocationSubjectIdOrderByAllocationRevisionDesc(
        String accountId,
        String allocationSubjectId
    );
}

interface ReserveTransactionRepository extends JpaRepository<ReserveTransaction, String> {

    List<ReserveTransaction> findTop20ByAccountIdAndWalletOrderByCreatedAtDesc(String accountId, ReserveWallet wallet);

    List<ReserveTransaction> findByAllocationOrderByBucketAsc(ReserveAllocation allocation);
}
