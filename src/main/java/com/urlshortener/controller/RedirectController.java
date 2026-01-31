package com.urlshortener.controller;

import com.urlshortener.dto.request.UrlPasswordRequest;
import com.urlshortener.dto.response.ApiResponse;
import com.urlshortener.model.Url;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/r")
@RequiredArgsConstructor
@Tag(name = "Redirect", description = "URL redirect endpoints")
public class RedirectController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL", description = "Redirects to the original URL or returns password required status")
    public ResponseEntity<?> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        if (urlService.isPasswordProtected(shortCode)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Password required"));
        }

        return performRedirect(shortCode, null, request);
    }

    @PostMapping("/{shortCode}/verify")
    @Operation(summary = "Verify password and redirect", description = "Verifies password for protected URLs and returns redirect URL")
    public ResponseEntity<?> verifyAndRedirect(
            @PathVariable String shortCode,
            @Valid @RequestBody UrlPasswordRequest passwordRequest,
            HttpServletRequest request) {

        String originalUrl = urlService.resolveUrl(shortCode, passwordRequest.getPassword());

        Url url = urlService.findByShortCode(shortCode);
        urlService.incrementClickCount(shortCode);
        analyticsService.recordClick(url, request);

        return ResponseEntity.ok(ApiResponse.success(Map.of("redirectUrl", originalUrl)));
    }

    @GetMapping("/{shortCode}/preview")
    @Operation(summary = "Preview URL", description = "Returns URL details without redirecting")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewUrl(
            @PathVariable String shortCode) {

        Url url = urlService.findByShortCode(shortCode);

        Map<String, Object> preview = Map.of(
                "shortCode", url.getShortCode(),
                "originalUrl", url.getOriginalUrl(),
                "title", url.getTitle() != null ? url.getTitle() : "",
                "description", url.getDescription() != null ? url.getDescription() : "",
                "isPasswordProtected", url.isPasswordProtected(),
                "clickCount", url.getClickCount(),
                "createdAt", url.getCreatedAt().toString()
        );

        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    private ResponseEntity<?> performRedirect(String shortCode, String password, HttpServletRequest request) {
        String originalUrl = urlService.resolveUrl(shortCode, password);

        Url url = urlService.findByShortCode(shortCode);
        urlService.incrementClickCount(shortCode);
        analyticsService.recordClick(url, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        log.info("Redirecting {} to {}", shortCode, originalUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
