package com.urlshortener.dto.response;

import com.urlshortener.model.Url;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    private String id;
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private String customAlias;
    private String title;
    private String description;
    private Set<String> tags;
    private long clickCount;
    private boolean isActive;
    private boolean isPasswordProtected;
    private boolean hasQrCode;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UrlResponse fromUrl(Url url, String baseUrl) {
        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .customAlias(url.getCustomAlias())
                .title(url.getTitle())
                .description(url.getDescription())
                .tags(url.getTags())
                .clickCount(url.getClickCount())
                .isActive(url.isActive())
                .isPasswordProtected(url.isPasswordProtected())
                .hasQrCode(url.getQrCodePath() != null)
                .expiresAt(url.getExpiresAt())
                .createdAt(url.getCreatedAt())
                .updatedAt(url.getUpdatedAt())
                .build();
    }
}
