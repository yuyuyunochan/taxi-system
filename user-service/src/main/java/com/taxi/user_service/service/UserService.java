package com.taxi.user_service.service;

import com.taxi.user_service.dto.DriverRequest;
import com.taxi.user_service.dto.PassengerRequest;
import com.taxi.user_service.entity.*;
import com.taxi.user_service.exception.*;
import com.taxi.user_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public Passenger createPassenger(PassengerRequest request) {

        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        Passenger passenger = Passenger.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        log.info("Creating passenger: {}", passenger.getEmail());

        return passengerRepository.save(passenger);
    }

    public Driver createDriver(DriverRequest request) {

        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("License already exists");
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .status(DriverStatus.OFFLINE)
                .build();

        log.info("Creating driver: {}", driver.getEmail());

        return driverRepository.save(driver);
    }

    public Driver updateDriverStatus(Long id, DriverStatus status) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        driver.setStatus(status);

        return driverRepository.save(driver);
    }

    public Passenger getPassenger(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
    }

    public Driver getDriver(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
    }

    public List<Driver> getAvailableDrivers() {
        return driverRepository.findByStatus(DriverStatus.AVAILABLE);
    }
}