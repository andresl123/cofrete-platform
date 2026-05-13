package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface DriverRepository extends JpaRepository<Driver, String> {

    Optional<Driver> findByUser(AppUser user);

    boolean existsByUser(AppUser user);
}

interface TruckRepository extends JpaRepository<Truck, String> {

    List<Truck> findByDriverOrderByCreatedAtAsc(Driver driver);

    List<Truck> findByDriverAndActiveOrderByCreatedAtAsc(Driver driver, boolean active);

    boolean existsByDriverAndPlate(Driver driver, String plate);
}

interface TaxProfileRepository extends JpaRepository<TaxProfile, String> {

    Optional<TaxProfile> findByDriverAndPlanningYear(Driver driver, int planningYear);

    Optional<TaxProfile> findFirstByDriverOrderByPlanningYearDesc(Driver driver);
}
