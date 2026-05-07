package com.taxi.trip_service.service;

import com.taxi.trip_service.client.UserServiceClient;
import com.taxi.trip_service.entity.Driver;
import com.taxi.trip_service.enums.DriverStatus;
import com.taxi.trip_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverSyncService {

    private final UserServiceClient userServiceClient;
    private final DriverRepository driverRepository;

    public void syncAvailableDrivers() {
        List<Long> externalDrivers = userServiceClient.getAvailableDriverIds();

        for (Long driverId : externalDrivers) {
            Driver localDriver = driverRepository.findById(driverId).orElse(null);

            if (localDriver == null) {
                driverRepository.save(Driver.builder()
                        .id(driverId)
                        .status(DriverStatus.AVAILABLE)
                        .build());
            } else if (localDriver.getStatus() != DriverStatus.AVAILABLE) {
                localDriver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.save(localDriver);
                log.info("Driver {} status refreshed to AVAILABLE from User Service", driverId);
            }
        }
        log.info("Synced {} drivers from user-service", externalDrivers.size());
    }
}