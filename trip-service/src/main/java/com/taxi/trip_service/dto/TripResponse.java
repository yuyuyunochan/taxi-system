package com.taxi.trip_service.dto;

import com.taxi.trip_service.enums.TripStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TripResponse {

    private Long id;
    private Long passengerId;
    private Long driverId;
    private TripStatus status;
    private String origin;
    private String destination;
    private BigDecimal price;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}