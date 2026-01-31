package com.urlshortener.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private String urlId;
    private String shortCode;
    private long totalClicks;
    private long uniqueVisitors;
    private List<ClicksByDate> clicksByDate;
    private List<CountryStats> topCountries;
    private List<BrowserStats> topBrowsers;
    private List<DeviceStats> deviceBreakdown;
    private List<ReferrerStats> topReferrers;
    private Map<String, Long> clicksByHour;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClicksByDate {
        private String date;
        private long clicks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryStats {
        private String country;
        private long clicks;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrowserStats {
        private String browser;
        private long clicks;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStats {
        private String deviceType;
        private long clicks;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferrerStats {
        private String referer;
        private long clicks;
        private double percentage;
    }
}
