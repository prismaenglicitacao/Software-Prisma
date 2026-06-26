package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
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

        if (value.contains("T")) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignored) {
                // fallback below
            }
        }

        try {
            return LocalDateTime.parse(value, SQLITE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value.replace(' ', 'T'));
        }
    }
}
