package com.urlshortener.service;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.DuplicateResourceException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.Url;
import com.urlshortener.model.User;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private UrlService urlService;

    private User testUser;
    private Url testUrl;
    private CreateUrlRequest createUrlRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(urlService, "defaultExpirationDays", 30);

        testUser = User.builder()
                .id("user123")
                .name("Test User")
                .email("test@example.com")
                .build();

        testUrl = Url.builder()
                .id("url123")
                .originalUrl("https://www.example.com/very-long-url")
                .shortCode("abc123")
                .userId("user123")
                .isActive(true)
                .clickCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        createUrlRequest = CreateUrlRequest.builder()
                .originalUrl("https://www.example.com/very-long-url")
                .title("Test URL")
                .build();
    }

    @Test
    void createUrl_Success() {
        when(userService.findByEmail(anyString())).thenReturn(testUser);
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

        UrlResponse response = urlService.createUrl(createUrlRequest, "test@example.com");

        assertNotNull(response);
        assertEquals("abc123", response.getShortCode());
        assertEquals("https://www.example.com/very-long-url", response.getOriginalUrl());
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void createUrl_WithCustomAlias_Success() {
        createUrlRequest.setCustomAlias("my-link");

        when(userService.findByEmail(anyString())).thenReturn(testUser);
        when(urlRepository.existsByShortCode("my-link")).thenReturn(false);
        testUrl.setShortCode("my-link");
        testUrl.setCustomAlias("my-link");
        when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

        UrlResponse response = urlService.createUrl(createUrlRequest, "test@example.com");

        assertNotNull(response);
        assertEquals("my-link", response.getShortCode());
    }

    @Test
    void createUrl_DuplicateAlias_ThrowsException() {
        createUrlRequest.setCustomAlias("existing-alias");

        when(userService.findByEmail(anyString())).thenReturn(testUser);
        when(urlRepository.existsByShortCode("existing-alias")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> urlService.createUrl(createUrlRequest, "test@example.com"));
    }

    @Test
    void findByShortCode_Success() {
        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(testUrl));

        Url result = urlService.findByShortCode("abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getShortCode());
    }

    @Test
    void findByShortCode_NotFound_ThrowsException() {
        when(urlRepository.findByShortCode("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> urlService.findByShortCode("nonexistent"));
    }

    @Test
    void incrementClickCount_Success() {
        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(testUrl));
        when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

        urlService.incrementClickCount("abc123");

        assertEquals(1, testUrl.getClickCount());
        verify(urlRepository).save(testUrl);
    }
}
