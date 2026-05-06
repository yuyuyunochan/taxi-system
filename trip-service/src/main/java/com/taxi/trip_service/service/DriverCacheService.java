package com.taxi.trip_service.service;

import com.taxi.trip_service.entity.Driver;
import com.taxi.trip_service.enums.DriverStatus;
import com.taxi.trip_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DriverRepository driverRepository;

    @Value("${app.cache.available-drivers.key:drivers:available}")
    private String cacheKey;

    @Value("${app.cache.available-drivers.ttl-seconds:30}")
    private long ttlSeconds;

    @SuppressWarnings("unchecked")
    public List<Driver> getAvailableDrivers() {

        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Redis HIT - available drivers from cache");
            return (List<Driver>) cached;
        }

        log.info("Redis MISS - loading drivers from DB");

        List<Driver> drivers =
                driverRepository.findByStatus(DriverStatus.AVAILABLE);

        redisTemplate.opsForValue()
                .set(cacheKey, drivers, ttlSeconds, TimeUnit.SECONDS);

        return drivers;
    }

    public void invalidateCache() {
        redisTemplate.delete(cacheKey);
        log.info("Driver cache invalidated");
    }
}