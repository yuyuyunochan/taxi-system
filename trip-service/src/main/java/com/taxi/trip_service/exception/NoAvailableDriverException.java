package com.taxi.trip_service.exception;

public class NoAvailableDriverException extends RuntimeException {

    public NoAvailableDriverException() {
        super("No available drivers at the moment");
    }
}