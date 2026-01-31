package com.urlshortener.service;

import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTasks {

    private final UrlRepository urlRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void deactivateExpiredUrls() {
        log.info("Running scheduled task: Deactivating expired URLs");

        List<Url> expiredUrls = urlRepository.findExpiredUrls(LocalDateTime.now());

        for (Url url : expiredUrls) {
            url.setActive(false);
            urlRepository.save(url);
            log.debug("Deactivated expired URL: {}", url.getShortCode());
        }

        if (!expiredUrls.isEmpty()) {
            log.info("Deactivated {} expired URLs", expiredUrls.size());
        }
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void weeklyCleanup() {
        log.info("Running weekly cleanup task");
    }
}
