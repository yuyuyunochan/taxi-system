package com.taxi.trip_service.exception;

import com.taxi.trip_service.enums.TripStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TripStatus from, TripStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}