package com.taxi.trip_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users";

    @Override
    public boolean passengerExists(Long passengerId) {
        try {
            restTemplate.getForObject(
                    USER_SERVICE_URL + "/passengers/" + passengerId,
                    Object.class
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long findAvailableDriver() {
        try {
            Long[] drivers = restTemplate.getForObject(
                    USER_SERVICE_URL + "/drivers/available",
                    Long[].class
            );
            if (drivers != null && drivers.length > 0) {
                return drivers[0];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void updateDriverStatus(Long driverId, String status) {
        try {
            restTemplate.patchForObject(
                    USER_SERVICE_URL + "/drivers/" + driverId + "/status?status=" + status,
                    null,
                    Void.class
            );
        } catch (Exception e) {
            log.error("Failed to update driver status", e);
        }
    }
}