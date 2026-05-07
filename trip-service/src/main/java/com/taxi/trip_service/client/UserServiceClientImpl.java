package com.taxi.trip_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

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
    public List<Long> getAvailableDriverIds() {
        try {
            List<java.util.Map<String, Object>> response = restTemplate.getForObject(
                    USER_SERVICE_URL + "/drivers/available",
                    List.class
            );

            if (response == null) return List.of();

            return response.stream()
                    .map(driverMap -> Long.valueOf(driverMap.get("id").toString()))
                    .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch available drivers from User Service: {}", e.getMessage());
            return List.of();
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