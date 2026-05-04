package com.taxi.trip_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsResponse {

    private long totalTripsToday;
    private double averagePrice;
}