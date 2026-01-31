package com.urlshortener.controller;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.request.UpdateUrlRequest;
import com.urlshortener.dto.response.ApiResponse;
import com.urlshortener.dto.response.PageResponse;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "URLs", description = "URL shortening and management endpoints")
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    @Operation(summary = "Create short URL", description = "Creates a new shortened URL")
    public ResponseEntity<ApiResponse<UrlResponse>> createUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.createUrl(request, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("URL created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get user's URLs", description = "Returns paginated list of user's URLs with filtering options")
    public ResponseEntity<ApiResponse<PageResponse<UrlResponse>>> getUserUrls(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Search by URL or title")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by tags")
            @RequestParam(required = false) List<String> tags) {

        PageResponse<UrlResponse> response = urlService.getUserUrls(
                userDetails.getUsername(), page, size, sortBy, sortDir, isActive, search, tags);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get URL details", description = "Returns details of a specific URL")
    public ResponseEntity<ApiResponse<UrlResponse>> getUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.getUrlByShortCode(shortCode, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{shortCode}")
    @Operation(summary = "Update URL", description = "Updates an existing URL")
    public ResponseEntity<ApiResponse<UrlResponse>> updateUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdateUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.updateUrl(shortCode, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("URL updated successfully", response));
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete URL", description = "Deletes a URL")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        urlService.deleteUrl(shortCode, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("URL deleted successfully"));
    }

    @PostMapping("/{shortCode}/qr")
    @Operation(summary = "Generate QR code", description = "Generates a QR code for the URL")
    public ResponseEntity<ApiResponse<UrlResponse>> generateQrCode(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.generateQrCode(shortCode, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("QR code generated successfully", response));
    }
}
