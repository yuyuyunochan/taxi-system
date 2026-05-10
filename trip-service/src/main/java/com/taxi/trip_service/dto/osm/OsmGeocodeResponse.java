package com.taxi.trip_service.dto.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsmGeocodeResponse(String lat, String lon) {
}