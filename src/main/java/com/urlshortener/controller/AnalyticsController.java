package com.urlshortener.controller;

import com.urlshortener.dto.response.AnalyticsResponse;
import com.urlshortener.dto.response.ApiResponse;
import com.urlshortener.dto.response.DashboardResponse;
import com.urlshortener.exception.ForbiddenException;
import com.urlshortener.model.Url;
import com.urlshortener.model.User;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.UrlService;
import com.urlshortener.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Analytics", description = "URL analytics and reporting endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UrlService urlService;
    private final UserService userService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard", description = "Returns user's dashboard with summary statistics")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        DashboardResponse response = analyticsService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/urls/{shortCode}")
    @Operation(summary = "Get URL analytics", description = "Returns detailed analytics for a specific URL")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getUrlAnalytics(
            @PathVariable String shortCode,
            @Parameter(description = "Start date for analytics")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for analytics")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        Url url = urlService.findByShortCode(shortCode);
        User user = userService.findByEmail(userDetails.getUsername());

        if (!url.getUserId().equals(user.getId())) {
            throw new ForbiddenException("You don't have permission to view these analytics");
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        AnalyticsResponse response = analyticsService.getUrlAnalytics(url.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/urls/{shortCode}/summary")
    @Operation(summary = "Get URL analytics summary", description = "Returns a quick summary of URL performance")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getUrlAnalyticsSummary(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        Url url = urlService.findByShortCode(shortCode);
        User user = userService.findByEmail(userDetails.getUsername());

        if (!url.getUserId().equals(user.getId())) {
            throw new ForbiddenException("You don't have permission to view these analytics");
        }

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        AnalyticsResponse response = analyticsService.getUrlAnalytics(url.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
