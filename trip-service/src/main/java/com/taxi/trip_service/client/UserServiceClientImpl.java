package com.taxi.trip_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {
    private final RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8081/api/users";

    @Override
    public boolean passengerExists(Long passengerId) {
        try {
            restTemplate.getForObject(
                    BASE_URL + "/passengers/" + passengerId,
                    Object.class
            );
            return true;
        } catch (Exception e) {
            log.error("Passenger check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<Long> getAvailableDriverIds() {
        try {
            DriverDto[] drivers = restTemplate.getForObject(
                    BASE_URL + "/drivers/available",
                    DriverDto[].class
            );

            if (drivers == null) {
                return List.of();
            }

            return Arrays.stream(drivers)
                    .map(DriverDto::getId)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch available drivers: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateDriverStatus(Long driverId, String status) {
        try {
            String url = BASE_URL + "/drivers/" + driverId + "/status?status=" + status;

            restTemplate.patchForObject(url, null, Void.class);

            log.info("Driver {} updated to {}", driverId, status);

        } catch (Exception e) {
            log.error("Failed to update driver status: {}", e.getMessage());
        }
    }

    static class DriverDto {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}