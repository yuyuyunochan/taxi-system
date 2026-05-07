package com.taxi.trip_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
@Slf4j
@Component
@Profile("stub")
public class UserServiceClientStub implements UserServiceClient {

    private Long driverCounter = 1L;

    @Override
    public boolean passengerExists(Long passengerId) {
        log.info("[STUB] Checking passenger: {}", passengerId);
        return true;
    }

    @Override
    public void updateDriverStatus(Long driverId, String status) {
        log.info("[STUB] Updating driver {} status to {}", driverId, status);
    }
    @Override
    public List<Long> getAvailableDriverIds() {
        log.info("[STUB] Returning fake available drivers");
        return List.of(1L, 2L, 3L, 4L, 5L);
    }
}