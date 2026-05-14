package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.auth.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripFuelLookupService {

    private final TripRepository trips;

    TripFuelLookupService(TripRepository trips) {
        this.trips = trips;
    }

    @Transactional(readOnly = true)
    public TripFuelEstimateContext requireFuelEstimateContext(AppUser user, String tripId) {
        var trip = trips.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found."));
        if (!trip.getAccountId().equals(user.getAccountId())) {
            throw new TripForbiddenException("Trip is not visible to this principal.");
        }
        return new TripFuelEstimateContext(
            trip.getId(),
            trip.getTruckId(),
            trip.getOriginCity(),
            trip.getOriginState(),
            trip.totalDistanceKm()
        );
    }
}
