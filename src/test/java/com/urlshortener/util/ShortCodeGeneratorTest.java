package com.urlshortener.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    private ShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ShortCodeGenerator();
    }

    @Test
    void generate_DefaultLength_Returns7Characters() {
        String code = generator.generate();

        assertNotNull(code);
        assertEquals(7, code.length());
    }

    @Test
    void generate_CustomLength_ReturnsCorrectLength() {
        String code = generator.generate(10);

        assertNotNull(code);
        assertEquals(10, code.length());
    }

    @Test
    void generate_ContainsOnlyAlphanumeric() {
        String code = generator.generate();

        assertTrue(code.matches("^[A-Za-z0-9]+$"));
    }

    @Test
    void generate_ProducesUniqueValues() {
        Set<String> codes = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate());
        }

        assertEquals(1000, codes.size());
    }

    @Test
    void generateBase62_ProducesValidCode() {
        String code = generator.generateBase62(123456789L);

        assertNotNull(code);
        assertTrue(code.length() >= 6);
        assertTrue(code.matches("^[A-Za-z0-9]+$"));
    }
}
