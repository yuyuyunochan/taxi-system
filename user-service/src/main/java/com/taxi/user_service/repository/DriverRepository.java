package com.taxi.user_service.repository;

import com.taxi.user_service.entity.Driver;
import com.taxi.user_service.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(DriverStatus status);

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);
}