package com.taxi.trip_service.exception;

public class PassengerNotFoundException extends RuntimeException {

    public PassengerNotFoundException(Long id) {
        super("Passenger not found with id: " + id);
    }
}