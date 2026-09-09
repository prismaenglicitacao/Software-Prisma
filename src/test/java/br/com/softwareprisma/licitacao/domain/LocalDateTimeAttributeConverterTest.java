package br.com.softwareprisma.licitacao.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeAttributeConverterTest {

    private final LocalDateTimeAttributeConverter converter = new LocalDateTimeAttributeConverter();

    @Test
    void convertToEntityAttribute_OffsetSemMinutos_DeveFuncionar() {
        String timestamp = "2026-09-03T11:18:29.964094-03";
        LocalDateTime result = converter.convertToEntityAttribute(timestamp);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(9, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    @Test
    void convertToEntityAttribute_OffsetComMinutos_DeveFuncionar() {
        String timestamp = "2026-09-03T11:18:29.964094-03:00";
        LocalDateTime result = converter.convertToEntityAttribute(timestamp);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(9, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    @Test
    void convertToEntityAttribute_ISO8601SemOffset_DeveFuncionar() {
        String timestamp = "2026-08-26T09:28:22.339980400";
        LocalDateTime result = converter.convertToEntityAttribute(timestamp);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(26, result.getDayOfMonth());
    }

    @Test
    void convertToEntityAttribute_EspacoComOffsetSemMinutos_DeveFuncionar() {
        String timestamp = "2026-09-03 11:18:07.547805-03";
        LocalDateTime result = converter.convertToEntityAttribute(timestamp);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(9, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    @Test
    void convertToEntityAttribute_EspacoComOffsetComMinutos_DeveFuncionar() {
        String timestamp = "2026-09-03 11:18:07.547805-03:00";
        LocalDateTime result = converter.convertToEntityAttribute(timestamp);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(9, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    @Test
    void convertToEntityAttribute_EspacoSemOffset_DeveFuncionar() {
        String timestamp = "2026-08-26 09:28:22";
        LocalDateTime result = converter.convertToEntityAttribute(timestamp);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(26, result.getDayOfMonth());
    }

    @Test
    void convertToEntityAttribute_EpochMilliseconds_DeveFuncionar() {
        String epoch = "1725359909000";
        LocalDateTime result = converter.convertToEntityAttribute(epoch);
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void convertToEntityAttribute_EpochSeconds_DeveFuncionar() {
        String epoch = "1725359909";
        LocalDateTime result = converter.convertToEntityAttribute(epoch);
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void convertToEntityAttribute_Null_DeveRetornarNull() {
        LocalDateTime result = converter.convertToEntityAttribute(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_Vazio_DeveRetornarNull() {
        LocalDateTime result = converter.convertToEntityAttribute("");
        assertNull(result);
    }
}
