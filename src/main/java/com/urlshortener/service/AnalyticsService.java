package com.urlshortener.service;

import com.urlshortener.dto.response.AnalyticsResponse;
import com.urlshortener.dto.response.DashboardResponse;
import com.urlshortener.dto.response.UrlResponse;
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
                .orElseThrow(() -> new RuntimeException("URL not found"));

        long totalClicks = clickEventRepository.countByUrlId(urlId);

        List<ClickEvent> uniqueIps = clickEventRepository.findDistinctIpAddressesByUrlId(urlId);
        long uniqueVisitors = uniqueIps.stream()
                .map(ClickEvent::getIpAddress)
                .distinct()
                .count();

        List<AnalyticsResponse.ClicksByDate> clicksByDate = clickEventRepository
                .getClicksByDateRange(urlId, startDate, endDate)
                .stream()
                .map(dc -> AnalyticsResponse.ClicksByDate.builder()
                        .date(dc.get_id())
                        .clicks(dc.getCount())
                        .build())
                .collect(Collectors.toList());

        List<AnalyticsResponse.CountryStats> topCountries = clickEventRepository
                .getTopCountries(urlId)
                .stream()
                .map(fc -> AnalyticsResponse.CountryStats.builder()
                        .country(fc.get_id() != null ? fc.get_id().toString() : "Unknown")
                        .clicks(fc.getCount())
                        .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                        .build())
                .collect(Collectors.toList());

        List<AnalyticsResponse.BrowserStats> topBrowsers = clickEventRepository
                .getTopBrowsers(urlId)
                .stream()
                .map(fc -> AnalyticsResponse.BrowserStats.builder()
                        .browser(fc.get_id() != null ? fc.get_id().toString() : "Unknown")
                        .clicks(fc.getCount())
                        .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                        .build())
                .collect(Collectors.toList());

        List<AnalyticsResponse.DeviceStats> deviceBreakdown = clickEventRepository
                .getDeviceBreakdown(urlId)
                .stream()
                .map(fc -> AnalyticsResponse.DeviceStats.builder()
                        .deviceType(fc.get_id() != null ? fc.get_id().toString() : "Unknown")
                        .clicks(fc.getCount())
                        .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                        .build())
                .collect(Collectors.toList());

        List<AnalyticsResponse.ReferrerStats> topReferrers = clickEventRepository
                .getTopReferrers(urlId)
                .stream()
                .map(fc -> AnalyticsResponse.ReferrerStats.builder()
                        .referer(fc.get_id() != null ? fc.get_id().toString() : "Direct")
                        .clicks(fc.getCount())
                        .percentage(totalClicks > 0 ? (fc.getCount() * 100.0 / totalClicks) : 0)
                        .build())
                .collect(Collectors.toList());

        Map<String, Long> clicksByHour = new HashMap<>();
        clickEventRepository.getClicksByHour(urlId).forEach(fc ->
                clicksByHour.put(fc.get_id().toString(), fc.getCount()));

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
        long expiredUrls = urlRepository.countByUserIdAndExpired(userId, LocalDateTime.now());
        long totalClicks = clickEventRepository.countByUserId(userId);

        List<UrlResponse> recentUrls = urlRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(url -> UrlResponse.fromUrl(url, baseUrl))
                .collect(Collectors.toList());

        List<UrlResponse> topPerformingUrls = urlRepository.findTop5ByUserIdOrderByClickCountDesc(userId)
                .stream()
                .map(url -> UrlResponse.fromUrl(url, baseUrl))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        List<AnalyticsResponse.ClicksByDate> clicksLast7Days = clickEventRepository
                .getClicksByDateRangeForUser(userId, now.minusDays(7), now)
                .stream()
                .map(dc -> AnalyticsResponse.ClicksByDate.builder()
                        .date(dc.get_id())
                        .clicks(dc.getCount())
                        .build())
                .collect(Collectors.toList());

        List<AnalyticsResponse.ClicksByDate> clicksLast30Days = clickEventRepository
                .getClicksByDateRangeForUser(userId, now.minusDays(30), now)
                .stream()
                .map(dc -> AnalyticsResponse.ClicksByDate.builder()
                        .date(dc.get_id())
                        .clicks(dc.getCount())
                        .build())
                .collect(Collectors.toList());

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
