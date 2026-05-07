package com.taxi.notification_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TripCreatedEvent {
    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private BigDecimal price;
}