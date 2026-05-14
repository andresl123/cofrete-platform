package com.cofrete.coreapi.compliance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ComplianceProfileRepository extends JpaRepository<ComplianceProfile, String> {

    Optional<ComplianceProfile> findByDriverId(String driverId);
}

interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, String> {

    List<InsurancePolicy> findByDriverIdOrderByCreatedAtAsc(String driverId);

    List<InsurancePolicy> findByDriverIdAndActiveOrderByCreatedAtAsc(String driverId, boolean active);
}

interface ComplianceDocumentRepository extends JpaRepository<ComplianceDocument, String> {

    List<ComplianceDocument> findByDriverIdOrderByCreatedAtAsc(String driverId);

    List<ComplianceDocument> findByDriverIdAndActiveOrderByCreatedAtAsc(String driverId, boolean active);

    List<ComplianceDocument> findByDriverIdAndDocumentTypeOrderByCreatedAtAsc(
        String driverId,
        ComplianceDocumentType documentType
    );

    List<ComplianceDocument> findByDriverIdAndDocumentTypeAndActiveOrderByCreatedAtAsc(
        String driverId,
        ComplianceDocumentType documentType,
        boolean active
    );
}

interface ComplianceAlertRepository extends JpaRepository<ComplianceAlert, String> {

    void deleteByDriverId(String driverId);

    List<ComplianceAlert> findByDriverIdOrderByDueOnAscGeneratedAtAsc(String driverId);
}

interface ComplianceScoreSnapshotRepository extends JpaRepository<ComplianceScoreSnapshot, String> {
}

interface ComplianceCalendarItemRepository extends JpaRepository<ComplianceCalendarItem, String> {

    void deleteByDriverId(String driverId);

    List<ComplianceCalendarItem> findByDriverIdOrderByDueOnAsc(String driverId);

    List<ComplianceCalendarItem> findByDriverIdAndDueOnBetweenOrderByDueOnAsc(
        String driverId,
        LocalDate from,
        LocalDate to
    );
}

interface WaitingTimeRuleRepository extends JpaRepository<WaitingTimeRule, String> {

    Optional<WaitingTimeRule> findByEffectiveFrom(LocalDate effectiveFrom);

    @Query("""
        select rule from WaitingTimeRule rule
        where rule.effectiveFrom <= :effectiveDate
          and (rule.effectiveTo is null or rule.effectiveTo >= :effectiveDate)
        order by rule.effectiveFrom desc
        """)
    List<WaitingTimeRule> findEffectiveRules(@Param("effectiveDate") LocalDate effectiveDate, Pageable pageable);
}

interface InsuranceRequirementRuleRepository extends JpaRepository<InsuranceRequirementRule, String> {

    Optional<InsuranceRequirementRule> findByRequirementScopeAndPolicyTypeAndEffectiveFrom(
        String requirementScope,
        InsurancePolicyType policyType,
        LocalDate effectiveFrom
    );

    @Query("""
        select rule from InsuranceRequirementRule rule
        where rule.effectiveFrom <= :effectiveDate
          and (rule.effectiveTo is null or rule.effectiveTo >= :effectiveDate)
        order by rule.effectiveFrom desc
        """)
    List<InsuranceRequirementRule> findEffectiveRules(@Param("effectiveDate") LocalDate effectiveDate, Pageable pageable);

    @Query("""
        select rule from InsuranceRequirementRule rule
        where rule.policyType = :policyType
          and rule.effectiveFrom <= :effectiveDate
          and (rule.effectiveTo is null or rule.effectiveTo >= :effectiveDate)
        order by rule.effectiveFrom desc
        """)
    List<InsuranceRequirementRule> findEffectiveRulesForPolicy(
        @Param("policyType") InsurancePolicyType policyType,
        @Param("effectiveDate") LocalDate effectiveDate,
        Pageable pageable
    );
}
