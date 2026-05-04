package com.taxi.trip_service.dto;

import com.taxi.trip_service.enums.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull
    private TripStatus status;
}