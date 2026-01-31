package com.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "click_events")
@CompoundIndex(name = "url_timestamp_idx", def = "{'urlId': 1, 'timestamp': -1}")
public class ClickEvent {

    @Id
    private String id;

    @Indexed
    private String urlId;

    @Indexed
    private String userId;

    private String ipAddress;

    private String userAgent;

    private String referer;

    private String country;

    private String city;

    private String region;

    private String timezone;

    private String isp;

    private String deviceType;

    private String browser;

    private String browserVersion;

    private String operatingSystem;

    private String osVersion;

    private boolean isMobile;

    private boolean isBot;

    @CreatedDate
    @Indexed
    private LocalDateTime timestamp;
}
