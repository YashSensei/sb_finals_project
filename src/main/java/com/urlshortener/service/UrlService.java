package com.urlshortener.service;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.request.UpdateUrlRequest;
import com.urlshortener.dto.response.PageResponse;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.BadRequestException;
import com.urlshortener.exception.DuplicateResourceException;
import com.urlshortener.exception.ForbiddenException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.model.Url;
import com.urlshortener.model.User;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final QrCodeService qrCodeService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.default-expiration-days:30}")
    private int defaultExpirationDays;

    @Transactional
    public UrlResponse createUrl(CreateUrlRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);

        String shortCode;
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByShortCode(request.getCustomAlias())) {
                throw new DuplicateResourceException("URL", "alias", request.getCustomAlias());
            }
            shortCode = request.getCustomAlias();
        } else {
            do {
                shortCode = shortCodeGenerator.generate();
            } while (urlRepository.existsByShortCode(shortCode));
        }

        LocalDateTime expiresAt = request.getExpiresAt();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(defaultExpirationDays);
        }

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .customAlias(request.getCustomAlias())
                .userId(user.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .tags(request.getTags())
                .expiresAt(expiresAt)
                .isActive(true)
                .clickCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            url.setPassword(passwordEncoder.encode(request.getPassword()));
            url.setPasswordProtected(true);
        }

        Url savedUrl = urlRepository.save(url);

        if (request.isGenerateQrCode()) {
            String qrPath = qrCodeService.generateQrCode(baseUrl + "/r/" + shortCode, shortCode);
            savedUrl.setQrCodePath(qrPath);
            savedUrl = urlRepository.save(savedUrl);
        }

        log.info("URL created: {} -> {} by user: {}", shortCode, request.getOriginalUrl(), userEmail);
        return UrlResponse.fromUrl(savedUrl, baseUrl);
    }

    @Cacheable(value = "urls", key = "#shortCode")
    public Url findByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL", "shortCode", shortCode));
    }

    public UrlResponse getUrlByShortCode(String shortCode, String userEmail) {
        Url url = findByShortCode(shortCode);
        validateOwnership(url, userEmail);
        return UrlResponse.fromUrl(url, baseUrl);
    }

    public String resolveUrl(String shortCode, String password) {
        Url url = findByShortCode(shortCode);

        if (!url.isActive()) {
            throw new BadRequestException("This URL has been deactivated");
        }

        if (url.isExpired()) {
            throw new UrlExpiredException();
        }

        if (url.isPasswordProtected()) {
            if (password == null || !passwordEncoder.matches(password, url.getPassword())) {
                throw new ForbiddenException("Password required or incorrect");
            }
        }

        return url.getOriginalUrl();
    }

    public boolean isPasswordProtected(String shortCode) {
        Url url = findByShortCode(shortCode);
        return url.isPasswordProtected();
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    @Transactional
    public void incrementClickCount(String shortCode) {
        Url url = findByShortCode(shortCode);
        url.incrementClickCount();
        urlRepository.save(url);
    }

    public PageResponse<UrlResponse> getUserUrls(String userEmail, int page, int size,
                                                   String sortBy, String sortDir,
                                                   Boolean isActive, String search, List<String> tags) {
        User user = userService.findByEmail(userEmail);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Url> urlPage;

        if (search != null && !search.isBlank()) {
            urlPage = urlRepository.searchByUserIdAndKeyword(user.getId(), search, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            urlPage = urlRepository.findByUserIdAndTagsIn(user.getId(), tags, pageable);
        } else if (isActive != null) {
            urlPage = urlRepository.findByUserIdAndIsActive(user.getId(), isActive, pageable);
        } else {
            urlPage = urlRepository.findByUserId(user.getId(), pageable);
        }

        Page<UrlResponse> responsePage = urlPage.map(url -> UrlResponse.fromUrl(url, baseUrl));
        return PageResponse.from(responsePage);
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    @Transactional
    public UrlResponse updateUrl(String shortCode, UpdateUrlRequest request, String userEmail) {
        Url url = findByShortCode(shortCode);
        validateOwnership(url, userEmail);

        if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
            url.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getTitle() != null) {
            url.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            url.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            url.setTags(request.getTags());
        }
        if (request.getExpiresAt() != null) {
            url.setExpiresAt(request.getExpiresAt());
        }
        if (request.getIsActive() != null) {
            url.setActive(request.getIsActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            url.setPassword(passwordEncoder.encode(request.getPassword()));
            url.setPasswordProtected(true);
        }
        if (Boolean.TRUE.equals(request.getRemovePassword())) {
            url.setPassword(null);
            url.setPasswordProtected(false);
        }

        url.setUpdatedAt(LocalDateTime.now());
        Url updatedUrl = urlRepository.save(url);

        log.info("URL updated: {} by user: {}", shortCode, userEmail);
        return UrlResponse.fromUrl(updatedUrl, baseUrl);
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    @Transactional
    public void deleteUrl(String shortCode, String userEmail) {
        Url url = findByShortCode(shortCode);
        validateOwnership(url, userEmail);

        urlRepository.delete(url);
        log.info("URL deleted: {} by user: {}", shortCode, userEmail);
    }

    public UrlResponse generateQrCode(String shortCode, String userEmail) {
        Url url = findByShortCode(shortCode);
        validateOwnership(url, userEmail);

        if (url.getQrCodePath() == null) {
            String qrPath = qrCodeService.generateQrCode(baseUrl + "/r/" + shortCode, shortCode);
            url.setQrCodePath(qrPath);
            url.setUpdatedAt(LocalDateTime.now());
            url = urlRepository.save(url);
        }

        return UrlResponse.fromUrl(url, baseUrl);
    }

    private void validateOwnership(Url url, String userEmail) {
        User user = userService.findByEmail(userEmail);
        if (!url.getUserId().equals(user.getId())) {
            throw new ForbiddenException("You don't have permission to access this URL");
        }
    }
}
