package com.taxi.trip_service.client;

import java.util.List;

public interface UserServiceClient {

    boolean passengerExists(Long passengerId);

    List<Long> getAvailableDriverIds();

    void updateDriverStatus(Long driverId, String status);
}