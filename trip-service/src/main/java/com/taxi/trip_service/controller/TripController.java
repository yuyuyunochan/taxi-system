package com.taxi.trip_service.controller;

import com.taxi.trip_service.dto.*;
import com.taxi.trip_service.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@Valid @RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }

    @GetMapping("/{id}")
    public TripResponse getTrip(@PathVariable Long id) {
        return tripService.getTrip(id);
    }

    @GetMapping
    public List<TripResponse> getTripsByPassenger(
            @RequestParam Long passengerId) {
        return tripService.getTripsByPassenger(passengerId);
    }
    @GetMapping("/drivers/available")
    public List<?> getAvailableDrivers() {
        return tripService.getAvailableDrivers();
    }

    @PatchMapping("/{id}/status")
    public TripResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return tripService.updateStatus(id, request);
    }

    @PostMapping("/{id}/rating")
    public TripResponse rateTrip(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {
        return tripService.rateTrip(id, request);
    }

    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        return tripService.getStatistics();
    }
}