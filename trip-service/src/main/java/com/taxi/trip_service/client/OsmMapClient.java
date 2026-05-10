package com.taxi.trip_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsmMapClient {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;

    @Value("${ors.api-key}")
    private String apiKey;

    @Value("${ors.geocode-url}")
    private String geocodeUrl;

    @Value("${ors.directions-url}")
    private String directionsUrl;

    public double getDistanceInKm(String origin, String destination) {

        String cacheKey = "ors:dist:" +
                origin.toLowerCase().replaceAll("\\s+", "") + ":" +
                destination.toLowerCase().replaceAll("\\s+", "");

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Distance from cache: {} km", cached);
            return Double.parseDouble(cached);
        }

        try {
            double[] start = geocode(origin);
            double[] end = geocode(destination);

            double distanceKm = calculateRoute(start, end);

            redisTemplate.opsForValue().set(cacheKey, String.valueOf(distanceKm), Duration.ofHours(24));

            log.info("Distance calculated via ORS: {} km", distanceKm);
            return distanceKm;

        } catch (Exception e) {
            log.error("ORS failed: {}", e.getMessage(), e);
            return 5.0;
        }
    }

    private double[] geocode(String address) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);

        String url = geocodeUrl +
                "?api_key=" + apiKey +
                "&text=Новосибирск " + address;

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        Map body = response.getBody();
        var features = (java.util.List<Map>) body.get("features");

        if (features == null || features.isEmpty()) {
            throw new RuntimeException("Address not found: " + address);
        }

        Map geometry = (Map) features.get(0).get("geometry");
        java.util.List<Double> coordinates =
                (java.util.List<Double>) geometry.get("coordinates");

        return new double[]{coordinates.get(1), coordinates.get(0)};
    }

    private double calculateRoute(double[] start, double[] end) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "coordinates": [
                    [%f, %f],
                    [%f, %f]
                  ]
                }
                """.formatted(start[1], start[0], end[1], end[0]);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        directionsUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(body, headers),
                        Map.class
                );

        Map responseBody = response.getBody();
        var routes = (java.util.List<Map>) responseBody.get("routes");
        Map summary = (Map) routes.get(0).get("summary");

        double distanceMeters = ((Number) summary.get("distance")).doubleValue();

        return Math.round((distanceMeters / 1000.0) * 100.0) / 100.0;
    }
}