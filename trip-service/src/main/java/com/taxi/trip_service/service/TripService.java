package com.taxi.trip_service.service;

import com.taxi.trip_service.client.UserServiceClient;
import com.taxi.trip_service.client.OsmMapClient;
import com.taxi.trip_service.dto.*;
import com.taxi.trip_service.entity.Driver;
import com.taxi.trip_service.entity.Trip;
import com.taxi.trip_service.enums.DriverStatus;
import com.taxi.trip_service.enums.TripStatus;
import com.taxi.trip_service.exception.*;
import com.taxi.trip_service.repository.DriverRepository;
import com.taxi.trip_service.repository.TripRepository;
import com.taxi.trip_service.service.DriverCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {
    private final TripEventPublisher tripEventPublisher;
    private final DriverCacheService driverCacheService;
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final UserServiceClient userServiceClient;
    private final DriverSyncService driverSyncService;

    private final OsmMapClient osmMapClient;

    @Value("${tariff.price-per-km:50.0}") // Поставил дефолт 50.0
    private double pricePerKm;

    @Transactional
    public TripResponse createTrip(Long passengerId, TripRequest request) {
        driverSyncService.syncAvailableDrivers();
        if (!userServiceClient.passengerExists(passengerId)) {
            throw new PassengerNotFoundException(passengerId);
        }

        tripRepository.findActiveTripByPassenger(passengerId)
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "Passenger already has active trip with id: " + existing.getId()
                    );
                });

        Driver driver = driverRepository
                .findFirstAvailableDriverForUpdate()
                .orElseThrow(NoAvailableDriverException::new);

        driver.setStatus(DriverStatus.BUSY);
        userServiceClient.updateDriverStatus(driver.getId(), "BUSY");
        driverCacheService.invalidateCache();

        double distance = osmMapClient.getDistanceInKm(request.getOrigin(), request.getDestination());

        BigDecimal price = BigDecimal
                .valueOf(distance * pricePerKm)
                .setScale(2, RoundingMode.HALF_UP);

        Trip trip = Trip.builder()
                .passengerId(passengerId)
                .driverId(driver.getId())
                .status(TripStatus.CREATED)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .distance(distance)
                .price(price)
                .build();

        Trip saved = tripRepository.save(trip);
        tripEventPublisher.publishTripCreated(saved);
        log.info("Trip created: id={}, driver={}, distance={}km, price={}",
                saved.getId(), driver.getId(), distance, price);

        return toResponse(saved);
    }

    public TripResponse getTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));
        return toResponse(trip);
    }

    public List<TripResponse> getTripsByPassenger(Long passengerId) {
        return tripRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public List<Driver> getAvailableDrivers() {
        return driverCacheService.getAvailableDrivers();
    }
    @Transactional
    public TripResponse updateStatus(Long id, StatusUpdateRequest request) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));

        TripStatus current = trip.getStatus();
        TripStatus next = request.getStatus();

        if (!isValidStatusTransition(current, next)) {
            throw new InvalidStatusTransitionException(current, next);
        }

        trip.setStatus(next);

        if (next == TripStatus.COMPLETED || next == TripStatus.CANCELLED) {
            releaseDriver(trip.getDriverId());
        }

        Trip updated = tripRepository.save(trip);
        tripEventPublisher.publishTripStatusChanged(updated, current.name());
        log.info("Trip {} status updated from {} to {}", id, current, next);

        return toResponse(updated);
    }

    @Transactional
    public TripResponse rateTrip(Long id, RatingRequest request) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BusinessException("Can only rate completed trips");
        }

        if (trip.getRating() != null) {
            throw new BusinessException("Trip already rated");
        }

        trip.setRating(request.getRating());

        return toResponse(tripRepository.save(trip));
    }

    public StatisticsResponse getStatistics() {

        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);

        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Trip> todayTrips =
                tripRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        Double avgPrice =
                tripRepository.findAveragePriceByStatus(TripStatus.COMPLETED);

        return StatisticsResponse.builder()
                .totalTripsToday(todayTrips.size())
                .averagePrice(avgPrice != null ? avgPrice : 0.0)
                .build();
    }
    private void releaseDriver(Long driverId) {

        if (driverId == null) return;
        userServiceClient.updateDriverStatus(driverId, "AVAILABLE");
        driverRepository.findById(driverId)
                .ifPresent(driver -> {
                    driver.setStatus(DriverStatus.AVAILABLE);
                    driverCacheService.invalidateCache();
                    log.info("Driver {} released and set to AVAILABLE", driverId);
                });
    }

    private TripResponse toResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .passengerId(trip.getPassengerId())
                .driverId(trip.getDriverId())
                .status(trip.getStatus())
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .price(trip.getPrice())
                .rating(trip.getRating())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    private boolean isValidStatusTransition(TripStatus current, TripStatus next) {
        return switch (current) {
            case CREATED -> next == TripStatus.ACCEPTED || next == TripStatus.CANCELLED;
            case ACCEPTED -> next == TripStatus.STARTED || next == TripStatus.CANCELLED;
            case STARTED -> next == TripStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}