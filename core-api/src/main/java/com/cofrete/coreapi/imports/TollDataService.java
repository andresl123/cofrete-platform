package com.cofrete.coreapi.imports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TollDataService {

    private static final String DATASET = "toll_plazas_and_tariffs";
    private static final String DEFAULT_SOURCE = "ANTT_DADOS_ABERTOS";

    private final TollTariffRepository tollTariffs;
    private final ImportJobRepository importJobs;

    TollDataService(TollTariffRepository tollTariffs, ImportJobRepository importJobs) {
        this.tollTariffs = tollTariffs;
        this.importJobs = importJobs;
    }

    @Transactional(readOnly = true)
    public TollDataEstimate estimate(TollEstimateCriteria criteria) {
        var states = new LinkedHashSet<String>();
        states.add(criteria.originState().trim().toUpperCase());
        states.add(criteria.destinationState().trim().toUpperCase());
        var tariffs = tollTariffs.findRouteTariffs(states, criteria.axles(), criteria.vehicleType().trim().toUpperCase());
        var items = tariffs.stream()
            .map(tariff -> new TollDataEstimateItem(
                tariff.getPlaza().getName(),
                tariff.getPlaza().getHighway(),
                tariff.getPlaza().getState(),
                tariff.getAmount(),
                tariff.getCurrency(),
                tariff.getConfidence(),
                tariff.getPlaza().getSource(),
                tariff.getPlaza().getSourceType(),
                tariff.getEffectiveStart(),
                tariff.getImportAuditId()
            ))
            .toList();
        BigDecimal total = items.stream()
            .map(TollDataEstimateItem::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        String freshness = items.isEmpty() ? "UNKNOWN" : worstFreshness(tariffs);
        String confidence = items.isEmpty() ? "no_imported_toll_data_available" : "imported_antt_fixture_route_state_match";
        return new TollDataEstimate(items, total, "BRL", freshness, confidence);
    }

    @Transactional(readOnly = true)
    public TollImportStatus importStatus(String source) {
        String normalizedSource = source == null || source.isBlank() ? DEFAULT_SOURCE : source.trim().toUpperCase();
        var job = DEFAULT_SOURCE.equals(normalizedSource)
            ? importJobs.findFirstByDatasetAndSourceOrderByCompletedAtDescCreatedAtDesc(DATASET, DEFAULT_SOURCE)
            : importJobs.findFirstByDatasetAndSourceOrderByCompletedAtDescCreatedAtDesc(DATASET, normalizedSource)
                .or(() -> importJobs.findFirstByDatasetOrderByCompletedAtDescCreatedAtDesc(DATASET));
        return job.map(this::toStatus)
            .orElseGet(() -> new TollImportStatus(
                normalizedSource,
                DATASET,
                null,
                null,
                null,
                "UNKNOWN",
                "not_imported",
                null,
                null,
                0,
                null,
                null,
                null
            ));
    }

    private TollImportStatus toStatus(ImportJob job) {
        return new TollImportStatus(
            job.getSource(),
            job.getDataset(),
            job.getSourceUrl(),
            job.getSourcePeriodStart() == null ? null : job.getSourcePeriodStart().toString(),
            job.getSourcePeriodEnd() == null ? null : job.getSourcePeriodEnd().toString(),
            job.getFreshnessStatus(),
            job.getConfidence(),
            job.getFileHash(),
            job.getParserErrorSummary(),
            job.getRowCount(),
            job.getRetrievedAt(),
            job.getCompletedAt(),
            job.getId()
        );
    }

    private static String worstFreshness(Iterable<TollTariff> tariffs) {
        for (TollTariff tariff : tariffs) {
            if (!"CURRENT".equals(tariff.getFreshnessStatus())) {
                return tariff.getFreshnessStatus();
            }
        }
        return "CURRENT";
    }
}
