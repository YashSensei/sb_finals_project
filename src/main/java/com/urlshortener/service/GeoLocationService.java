package com.urlshortener.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class GeoLocationService {

    @Value("${external.geo-api.url}")
    private String geoApiUrl;

    @Value("${external.geo-api.enabled:true}")
    private boolean geoApiEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "geolocations", key = "#ipAddress")
    public GeoLocation getLocation(String ipAddress) {
        if (!geoApiEnabled || isLocalIP(ipAddress)) {
            return GeoLocation.unknown();
        }

        try {
            String url = geoApiUrl + ipAddress;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                return GeoLocation.builder()
                        .country((String) response.get("country"))
                        .countryCode((String) response.get("countryCode"))
                        .region((String) response.get("regionName"))
                        .city((String) response.get("city"))
                        .timezone((String) response.get("timezone"))
                        .isp((String) response.get("isp"))
                        .latitude(response.get("lat") != null ? ((Number) response.get("lat")).doubleValue() : 0)
                        .longitude(response.get("lon") != null ? ((Number) response.get("lon")).doubleValue() : 0)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to get geolocation for IP {}: {}", ipAddress, e.getMessage());
        }

        return GeoLocation.unknown();
    }

    private boolean isLocalIP(String ipAddress) {
        return ipAddress == null ||
                ipAddress.equals("127.0.0.1") ||
                ipAddress.equals("0:0:0:0:0:0:0:1") ||
                ipAddress.equals("::1") ||
                ipAddress.startsWith("192.168.") ||
                ipAddress.startsWith("10.") ||
                ipAddress.startsWith("172.");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoLocation {
        private String country;
        private String countryCode;
        private String region;
        private String city;
        private String timezone;
        private String isp;
        private double latitude;
        private double longitude;

        public static GeoLocation unknown() {
            return GeoLocation.builder()
                    .country("Unknown")
                    .countryCode("XX")
                    .region("Unknown")
                    .city("Unknown")
                    .timezone("Unknown")
                    .isp("Unknown")
                    .latitude(0)
                    .longitude(0)
                    .build();
        }
    }
}
