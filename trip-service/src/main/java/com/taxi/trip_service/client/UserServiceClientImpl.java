package com.taxi.trip_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${services.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    @Override
    public boolean passengerExists(Long passengerId) {
        try {
            String url = userServiceUrl + "/passengers/" + passengerId;
            restTemplate.getForObject(url, Object.class);
            return true;
        } catch (Exception e) {
            log.error("Passenger check failed on URL {}: {}", userServiceUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Long> getAvailableDriverIds() {
        try {
            String url = userServiceUrl + "/drivers/available";
            DriverDto[] drivers = restTemplate.getForObject(url, DriverDto[].class);

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
            String url = userServiceUrl + "/drivers/" + driverId + "/status?status=" + status;

            restTemplate.patchForObject(url, null, Void.class);

            log.info("Driver {} updated to {}", driverId, status);

        } catch (Exception e) {
            log.error("Failed to update driver status: {}", e.getMessage());
        }
    }

    static class DriverDto {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
}