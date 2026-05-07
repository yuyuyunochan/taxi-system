package com.taxi.trip_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TripStatusChangedEvent {

    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;
}