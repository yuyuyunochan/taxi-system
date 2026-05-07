package com.taxi.notification_service.dto;

import lombok.Data;

@Data
public class TripStatusChangedEvent {
    private Long tripId;
    private String oldStatus;
    private String newStatus;
}