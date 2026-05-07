package com.taxi.notification_service.dto;

import lombok.Data;

@Data
public class TripStatusChangedEvent {
    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private String oldStatus;
    private String newStatus;
}