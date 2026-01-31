package com.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "Invalid URL format")
    private String originalUrl;

    @Size(min = 3, max = 20, message = "Custom alias must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Custom alias can only contain letters, numbers, hyphens, and underscores")
    private String customAlias;

    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Set<String> tags;

    private LocalDateTime expiresAt;

    private String password;

    private boolean generateQrCode;
}
