package com.taxi.trip_service.config;

import com.taxi.trip_service.entity.Driver;
import com.taxi.trip_service.enums.DriverStatus;
import com.taxi.trip_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("stub")
public class DataInitializer implements CommandLineRunner {

    private final DriverRepository driverRepository;

    @Override
    public void run(String... args) {

        if (driverRepository.count() == 0) {
            for (int i = 0; i < 5; i++) {
                driverRepository.save(
                        Driver.builder()
                                .status(DriverStatus.AVAILABLE)
                                .build()
                );
            }
        }
    }
}