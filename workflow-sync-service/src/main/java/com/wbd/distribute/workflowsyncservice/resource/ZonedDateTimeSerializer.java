package com.wbd.distribute.workflowsyncservice.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;

import jakarta.inject.Inject;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * The default ZonedDateTimeSerializer does not use the Spring's spring.jackson.date-format property
 * and uses a format that does not render millisecond/nanosecond digits when they are 0 (0 are padded out).
 * <p>
 * This serializer replaces the default behaviour and ensures milliseconds are always provided.
 */
@JsonComponent
public class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
    private final DateTimeFormatter formatter;

    @Inject
    public ZonedDateTimeSerializer(@Value("${spring.jackson.date-format}") final String pattern) {
        formatter = DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (null == value) {
            return;
        }
        gen.writeString(value.format(formatter));
    }
}

