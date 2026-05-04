package com.taxi.trip_service.client;

public interface UserServiceClient {

    boolean passengerExists(Long passengerId);

    Long findAvailableDriver();

    void updateDriverStatus(Long driverId, String status);
}