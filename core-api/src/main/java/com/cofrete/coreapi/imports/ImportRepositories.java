package com.cofrete.coreapi.imports;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ImportJobRepository extends JpaRepository<ImportJob, String> {

    Optional<ImportJob> findFirstByDatasetOrderByCompletedAtDescCreatedAtDesc(String dataset);

    Optional<ImportJob> findFirstByDatasetAndSourceOrderByCompletedAtDescCreatedAtDesc(String dataset, String source);
}

interface FuelPriceRepository extends JpaRepository<FuelPrice, String> {

    Optional<FuelPrice> findFirstByFuelTypeAndStateAndCityOrderBySourcePeriodEndDescRetrievedAtDesc(
        String fuelType,
        String state,
        String city
    );

    Optional<FuelPrice> findFirstByFuelTypeAndStateOrderBySourcePeriodEndDescRetrievedAtDesc(String fuelType, String state);

    List<FuelPrice> findByFuelTypeAndStateAndCityOrderBySourcePeriodStartAsc(
        String fuelType,
        String state,
        String city
    );

    List<FuelPrice> findByFuelTypeAndStateOrderBySourcePeriodStartAsc(String fuelType, String state);
}

interface DriverFuelReportRepository extends JpaRepository<DriverFuelReport, String> {
}

interface TollTariffRepository extends JpaRepository<TollTariff, String> {

    @Query("""
        select tariff
        from TollTariff tariff
        join fetch tariff.plaza plaza
        where tariff.axleCount = :axles
          and upper(tariff.vehicleCategory) = upper(:vehicleCategory)
          and plaza.state in :states
        order by plaza.state, plaza.highway, plaza.name
        """)
    List<TollTariff> findRouteTariffs(
        @Param("states") Collection<String> states,
        @Param("axles") int axles,
        @Param("vehicleCategory") String vehicleCategory
    );
}
