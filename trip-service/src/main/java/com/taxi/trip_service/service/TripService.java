package com.taxi.trip_service.service;

import com.taxi.trip_service.client.UserServiceClient;
import com.taxi.trip_service.dto.*;
import com.taxi.trip_service.entity.Trip;
import com.taxi.trip_service.enums.TripStatus;
import com.taxi.trip_service.exception.*;
import com.taxi.trip_service.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserServiceClient userServiceClient;

    @Value("${tariff.price-per-km:2.5}")
    private double pricePerKm;

    @Transactional
    public TripResponse createTrip(TripRequest request) {

        if (!userServiceClient.passengerExists(request.getPassengerId())) {
            throw new PassengerNotFoundException(request.getPassengerId());
        }

        tripRepository.findActiveTripByPassenger(request.getPassengerId())
                .ifPresent(existingTrip -> {
                    throw new RuntimeException(
                            "Passenger already has an active trip with id: " + existingTrip.getId()
                    );
                });

        Long driverId = userServiceClient.findAvailableDriver();
        if (driverId == null) {
            throw new NoAvailableDriverException();
        }

        userServiceClient.updateDriverStatus(driverId, "BUSY");

        double distance = 5 + Math.random() * 45;

        BigDecimal price = BigDecimal.valueOf(distance * pricePerKm);

        Trip trip = Trip.builder()
                .passengerId(request.getPassengerId())
                .driverId(driverId)
                .status(TripStatus.CREATED)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .distance(distance)
                .price(price)
                .build();

        Trip saved = tripRepository.save(trip);
        log.info("Trip created: id={}, driver={}", saved.getId(), driverId);

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

    @Transactional
    public TripResponse updateStatus(Long id, StatusUpdateRequest request) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));

        TripStatus currentStatus = trip.getStatus();
        TripStatus newStatus = request.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }

        trip.setStatus(newStatus);

        if (newStatus == TripStatus.COMPLETED || newStatus == TripStatus.CANCELLED) {
            if (trip.getDriverId() != null) {
                userServiceClient.updateDriverStatus(trip.getDriverId(), "AVAILABLE");
            }
        }

        Trip updated = tripRepository.save(trip);
        log.info("Trip {} status updated to {}", id, newStatus);

        return toResponse(updated);
    }

    @Transactional
    public TripResponse rateTrip(Long id, RatingRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed trips");
        }

        if (trip.getRating() != null) {
            throw new RuntimeException("Trip already rated");
        }

        trip.setRating(request.getRating());
        return toResponse(tripRepository.save(trip));
    }

    public StatisticsResponse getStatistics() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Trip> todayTrips = tripRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        Double avgPrice = tripRepository.findAveragePriceByStatus(TripStatus.COMPLETED);

        return StatisticsResponse.builder()
                .totalTripsToday(todayTrips.size())
                .averagePrice(avgPrice != null ? avgPrice : 0.0)
                .build();
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