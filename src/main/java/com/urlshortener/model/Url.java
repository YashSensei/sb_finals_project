package com.urlshortener.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "urls")
public class Url {

    @Id
    private String id;

    @NotBlank(message = "Original URL is required")
    private String originalUrl;

    @Indexed(unique = true)
    private String shortCode;

    private String customAlias;

    @Indexed
    private String userId;

    private String title;

    private String description;

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Builder.Default
    private long clickCount = 0;

    @Builder.Default
    private boolean isActive = true;

    private LocalDateTime expiresAt;

    private String password;

    @Builder.Default
    private boolean isPasswordProtected = false;

    private String qrCodePath;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void incrementClickCount() {
        this.clickCount++;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
