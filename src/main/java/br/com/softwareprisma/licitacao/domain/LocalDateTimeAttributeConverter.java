package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

@Converter(autoApply = false)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter SQLITE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter();

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        if (value.matches("\\d+")) {
            long epoch = Long.parseLong(value);
            if (epoch > 10_000_000_000L) {
                return Instant.ofEpochMilli(epoch).atZone(ZoneOffset.UTC).toLocalDateTime();
            }
            return Instant.ofEpochSecond(epoch).atZone(ZoneOffset.UTC).toLocalDateTime();
        }

        // Handle timestamps with offset (with or without T)
        if (value.contains("T") || value.matches(".*[+-]\\d{2}(:\\d{2})?$")) {
            try {
                // First try LocalDateTime.parse (for timestamps without offset)
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignored) {
                // Try with offset - normalize offsets like -03 to -03:00
                try {
                    String normalized = normalizeOffset(value);
                    return OffsetDateTime.parse(normalized).toLocalDateTime();
                } catch (DateTimeParseException ignored2) {
                    // Try replacing space with T first, then normalize offset
                    try {
                        String withT = value.replace(' ', 'T');
                        String normalized = normalizeOffset(withT);
                        return OffsetDateTime.parse(normalized).toLocalDateTime();
                    } catch (DateTimeParseException ignored3) {
                        // fallback below
                    }
                }
            }
        }

        try {
            return LocalDateTime.parse(value, SQLITE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value.replace(' ', 'T'));
        }
    }

    private String normalizeOffset(String value) {
        // Normalize offsets like -03, +05 to -03:00, +05:00
        // Pattern: matches offset at end like -03, +05, -03:00, +05:00
        return value.replaceAll("([+-]\\d{2})(?!:\\d{2})$", "$1:00");
    }
}
