package com.urlshortener.controller;

import com.urlshortener.dto.response.ApiResponse;
import com.urlshortener.dto.response.PageResponse;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.dto.response.UserResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.Url;
import com.urlshortener.model.User;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/stats")
    @Operation(summary = "Get system stats", description = "Returns overall system statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalUrls", urlRepository.count());
        stats.put("totalClicks", clickEventRepository.count());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Returns paginated list of all users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::fromUser);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/urls")
    @Operation(summary = "Get all URLs", description = "Returns paginated list of all URLs")
    public ResponseEntity<ApiResponse<PageResponse<UrlResponse>>> getAllUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Url> urlPage = urlRepository.findAll(pageable);
        Page<UrlResponse> responsePage = urlPage.map(url -> UrlResponse.fromUrl(url, baseUrl));

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @PutMapping("/users/{userId}/disable")
    @Operation(summary = "Disable user", description = "Disables a user account")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User disabled successfully"));
    }

    @PutMapping("/users/{userId}/enable")
    @Operation(summary = "Enable user", description = "Enables a user account")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User enabled successfully"));
    }

    @DeleteMapping("/urls/{urlId}")
    @Operation(summary = "Delete URL", description = "Deletes any URL")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(@PathVariable String urlId) {
        urlRepository.deleteById(urlId);
        return ResponseEntity.ok(ApiResponse.success("URL deleted successfully"));
    }
}
