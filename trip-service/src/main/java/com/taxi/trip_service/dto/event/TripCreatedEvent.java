package com.taxi.trip_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TripCreatedEvent {

    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private BigDecimal price;
    private LocalDateTime createdAt;
}