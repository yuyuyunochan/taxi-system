package com.taxi.trip_service.repository;

import com.taxi.trip_service.entity.Driver;
import com.taxi.trip_service.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT d FROM Driver d
        WHERE d.status = 'AVAILABLE'
        ORDER BY d.id
        LIMIT 1
    """)
    Optional<Driver> findFirstAvailableDriverForUpdate();
}