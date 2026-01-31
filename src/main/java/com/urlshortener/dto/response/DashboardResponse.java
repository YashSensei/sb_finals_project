package com.urlshortener.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalUrls;
    private long totalClicks;
    private long activeUrls;
    private long expiredUrls;
    private List<UrlResponse> recentUrls;
    private List<UrlResponse> topPerformingUrls;
    private List<AnalyticsResponse.ClicksByDate> clicksLast7Days;
    private List<AnalyticsResponse.ClicksByDate> clicksLast30Days;
}
