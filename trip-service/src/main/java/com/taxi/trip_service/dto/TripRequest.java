package com.taxi.trip_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripRequest {

    @NotNull
    private Long passengerId;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;
}