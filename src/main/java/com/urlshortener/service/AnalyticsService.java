package com.urlshortener.service;

import com.urlshortener.dto.response.AnalyticsResponse;
import com.urlshortener.dto.response.DashboardResponse;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.Url;
import com.urlshortener.model.User;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;
    private final GeoLocationService geoLocationService;
    private final UserAgentParser userAgentParser;
    private final UserService userService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async("analyticsExecutor")
    public void recordClick(Url url, HttpServletRequest request) {
        try {
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");

            UserAgentParser.ParsedUserAgent parsedUA = userAgentParser.parse(userAgent);
            GeoLocationService.GeoLocation geoLocation = geoLocationService.getLocation(ipAddress);

            ClickEvent clickEvent = ClickEvent.builder()
                    .urlId(url.getId())
                    .userId(url.getUserId())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referer(referer)
                    .country(geoLocation.getCountry())
                    .city(geoLocation.getCity())
                    .region(geoLocation.getRegion())
                    .timezone(geoLocation.getTimezone())
                    .isp(geoLocation.getIsp())
                    .browser(parsedUA.getBrowser())
                    .browserVersion(parsedUA.getBrowserVersion())
                    .operatingSystem(parsedUA.getOperatingSystem())
                    .osVersion(parsedUA.getOsVersion())
                    .deviceType(parsedUA.getDeviceType())
                    .isMobile(parsedUA.isMobile())
                    .isBot(parsedUA.isBot())
                    .timestamp(LocalDateTime.now())
                    .build();

            clickEventRepository.save(clickEvent);
            log.debug("Click recorded for URL: {}", url.getShortCode());
        } catch (Exception e) {
            log.error("Failed to record click: {}", e.getMessage());
        }
    }

    public AnalyticsResponse getUrlAnalytics(String urlId, LocalDateTime startDate, LocalDateTime endDate) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL", "id", urlId));

        long totalClicks = clickEventRepository.countByUrlId(urlId);

        List<ClickEvent> uniqueIps = clickEventRepository.findDistinctIpAddressesByUrlId(urlId);
        long uniqueVisitors = uniqueIps != null ? uniqueIps.stream()
                .map(ClickEvent::getIpAddress)
                .distinct()
                .count() : 0;

        List<AnalyticsResponse.ClicksByDate> clicksByDate = new java.util.ArrayList<>();
        List<AnalyticsResponse.CountryStats> topCountries = new java.util.ArrayList<>();
        List<AnalyticsResponse.BrowserStats> topBrowsers = new java.util.ArrayList<>();
        List<AnalyticsResponse.DeviceStats> deviceBreakdown = new java.util.ArrayList<>();
        List<AnalyticsResponse.ReferrerStats> topReferrers = new java.util.ArrayList<>();
        Map<String, Long> clicksByHour = new HashMap<>();

        try {
            var dateResults = clickEventRepository.getClicksByDateRange(urlId, startDate, endDate);
            if (dateResults != null) {
                clicksByDate = dateResults.stream()
                        .map(dc -> AnalyticsResponse.ClicksByDate.builder()
                                .date(dc.get_id())
                                .clicks(dc.getCount())
                                .build())
                        .collect(Collectors.toList());
            }

            var countryResults = clickEventRepository.getTopCountries(urlId);
            if (countryResults != null) {
                topCountries = countryResults.stream()
                        .map(fc -> AnalyticsResponse.CountryStats.builder()
                                .country(fc.get_id() != null ? fc.get_id().toString() : "Unknown")
                                .clicks(fc.getCount())
                                .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                                .build())
                        .collect(Collectors.toList());
            }

            var browserResults = clickEventRepository.getTopBrowsers(urlId);
            if (browserResults != null) {
                topBrowsers = browserResults.stream()
                        .map(fc -> AnalyticsResponse.BrowserStats.builder()
                                .browser(fc.get_id() != null ? fc.get_id().toString() : "Unknown")
                                .clicks(fc.getCount())
                                .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                                .build())
                        .collect(Collectors.toList());
            }

            var deviceResults = clickEventRepository.getDeviceBreakdown(urlId);
            if (deviceResults != null) {
                deviceBreakdown = deviceResults.stream()
                        .map(fc -> AnalyticsResponse.DeviceStats.builder()
                                .deviceType(fc.get_id() != null ? fc.get_id().toString() : "Unknown")
                                .clicks(fc.getCount())
                                .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                                .build())
                        .collect(Collectors.toList());
            }

            var referrerResults = clickEventRepository.getTopReferrers(urlId);
            if (referrerResults != null) {
                topReferrers = referrerResults.stream()
                        .map(fc -> AnalyticsResponse.ReferrerStats.builder()
                                .referer(fc.get_id() != null ? fc.get_id().toString() : "Direct")
                                .clicks(fc.getCount())
                                .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                                .build())
                        .collect(Collectors.toList());
            }

            var hourResults = clickEventRepository.getClicksByHour(urlId);
            if (hourResults != null) {
                hourResults.forEach(fc -> {
                    if (fc.get_id() != null) {
                        clicksByHour.put(fc.get_id().toString(), fc.getCount());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Error fetching analytics aggregations: {}", e.getMessage());
        }

        return AnalyticsResponse.builder()
                .urlId(urlId)
                .shortCode(url.getShortCode())
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .clicksByDate(clicksByDate)
                .topCountries(topCountries)
                .topBrowsers(topBrowsers)
                .deviceBreakdown(deviceBreakdown)
                .topReferrers(topReferrers)
                .clicksByHour(clicksByHour)
                .build();
    }

    public DashboardResponse getDashboard(String userEmail) {
        User user = userService.findByEmail(userEmail);
        String userId = user.getId();

        long totalUrls = urlRepository.countByUserId(userId);
        long activeUrls = urlRepository.countByUserIdAndIsActive(userId, true);
        Long expiredCount = urlRepository.countByUserIdAndExpired(userId, LocalDateTime.now());
        long expiredUrls = expiredCount != null ? expiredCount : 0L;
        long totalClicks = clickEventRepository.countByUserId(userId);

        List<UrlResponse> recentUrls = urlRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(url -> UrlResponse.fromUrl(url, baseUrl))
                .collect(Collectors.toList());

        List<UrlResponse> topPerformingUrls = urlRepository.findTop5ByUserIdOrderByClickCountDesc(userId)
                .stream()
                .map(url -> UrlResponse.fromUrl(url, baseUrl))
                .collect(Collectors.toList());

        List<AnalyticsResponse.ClicksByDate> clicksLast7Days = new java.util.ArrayList<>();
        List<AnalyticsResponse.ClicksByDate> clicksLast30Days = new java.util.ArrayList<>();

        try {
            LocalDateTime now = LocalDateTime.now();
            var last7Days = clickEventRepository.getClicksByDateRangeForUser(userId, now.minusDays(7), now);
            if (last7Days != null) {
                clicksLast7Days = last7Days.stream()
                        .map(dc -> AnalyticsResponse.ClicksByDate.builder()
                                .date(dc.get_id())
                                .clicks(dc.getCount())
                                .build())
                        .collect(Collectors.toList());
            }

            var last30Days = clickEventRepository.getClicksByDateRangeForUser(userId, now.minusDays(30), now);
            if (last30Days != null) {
                clicksLast30Days = last30Days.stream()
                        .map(dc -> AnalyticsResponse.ClicksByDate.builder()
                                .date(dc.get_id())
                                .clicks(dc.getCount())
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Error fetching dashboard click data: {}", e.getMessage());
        }

        return DashboardResponse.builder()
                .totalUrls(totalUrls)
                .totalClicks(totalClicks)
                .activeUrls(activeUrls)
                .expiredUrls(expiredUrls)
                .recentUrls(recentUrls)
                .topPerformingUrls(topPerformingUrls)
                .clicksLast7Days(clicksLast7Days)
                .clicksLast30Days(clicksLast30Days)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
