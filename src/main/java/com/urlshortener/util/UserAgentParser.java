package com.urlshortener.util;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentParser {

    private final Parser parser;

    public UserAgentParser() {
        this.parser = new Parser();
    }

    public ParsedUserAgent parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return ParsedUserAgent.unknown();
        }

        try {
            Client client = parser.parse(userAgentString);

            String browser = client.userAgent.family;
            String browserVersion = client.userAgent.major;
            String os = client.os.family;
            String osVersion = client.os.major;
            String device = client.device.family;

            boolean isMobile = isMobileDevice(userAgentString, device);
            boolean isBot = isBot(userAgentString);
            String deviceType = determineDeviceType(userAgentString, device, isMobile);

            return ParsedUserAgent.builder()
                    .browser(browser)
                    .browserVersion(browserVersion != null ? browserVersion : "")
                    .operatingSystem(os)
                    .osVersion(osVersion != null ? osVersion : "")
                    .deviceType(deviceType)
                    .isMobile(isMobile)
                    .isBot(isBot)
                    .build();
        } catch (Exception e) {
            return ParsedUserAgent.unknown();
        }
    }

    private boolean isMobileDevice(String userAgent, String device) {
        String ua = userAgent.toLowerCase();
        return ua.contains("mobile") || ua.contains("android") ||
                ua.contains("iphone") || ua.contains("ipad") ||
                ua.contains("windows phone") || "iPhone".equalsIgnoreCase(device) ||
                "Android".equalsIgnoreCase(device);
    }

    private boolean isBot(String userAgent) {
        String ua = userAgent.toLowerCase();
        return ua.contains("bot") || ua.contains("crawler") ||
                ua.contains("spider") || ua.contains("scraper") ||
                ua.contains("curl") || ua.contains("wget") ||
                ua.contains("python") || ua.contains("java/");
    }

    private String determineDeviceType(String userAgent, String device, boolean isMobile) {
        String ua = userAgent.toLowerCase();

        if (isBot(ua)) {
            return "Bot";
        }
        if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        if (isMobile) {
            return "Mobile";
        }
        return "Desktop";
    }

    @Data
    @Builder
    public static class ParsedUserAgent {
        private String browser;
        private String browserVersion;
        private String operatingSystem;
        private String osVersion;
        private String deviceType;
        private boolean isMobile;
        private boolean isBot;

        public static ParsedUserAgent unknown() {
            return ParsedUserAgent.builder()
                    .browser("Unknown")
                    .browserVersion("")
                    .operatingSystem("Unknown")
                    .osVersion("")
                    .deviceType("Unknown")
                    .isMobile(false)
                    .isBot(false)
                    .build();
        }
    }
}
