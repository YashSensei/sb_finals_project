package com.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

    public String generateBase62(long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(ALPHANUMERIC.charAt((int) (number % 62)));
            number /= 62;
        }
        while (sb.length() < 6) {
            sb.append(ALPHANUMERIC.charAt(0));
        }
        return sb.reverse().toString();
    }
}
