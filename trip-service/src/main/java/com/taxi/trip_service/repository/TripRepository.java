package com.taxi.trip_service.repository;

import com.taxi.trip_service.entity.Trip;
import com.taxi.trip_service.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByPassengerId(Long passengerId);

    List<Trip> findByDriverId(Long driverId);

    @Query("SELECT t FROM Trip t WHERE t.createdAt >= :from AND t.createdAt <= :to")
    List<Trip> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT AVG(t.price) FROM Trip t WHERE t.status = :status")
    Double findAveragePriceByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.passengerId = :passengerId AND t.status IN ('CREATED','ACCEPTED','STARTED')")
    Optional<Trip> findActiveTripByPassenger(Long passengerId);
}