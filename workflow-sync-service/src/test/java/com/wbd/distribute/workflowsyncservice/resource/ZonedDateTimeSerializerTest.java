package com.wbd.distribute.workflowsyncservice.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


class ZonedDateTimeSerializerTest {

    private static final String DISCO_STD_TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final ZonedDateTimeSerializer ZDT_SERIALIZER = new ZonedDateTimeSerializer(DISCO_STD_TS_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DISCO_STD_TS_FORMAT);

    private JsonGenerator generator;
    private SerializerProvider serializers;

    @BeforeEach
    void beforeEach() {
        this.generator = Mockito.mock(JsonGenerator.class);
        this.serializers = Mockito.mock(SerializerProvider.class);
    }

    @Test
    void testNull() throws IOException {
        ZDT_SERIALIZER.serialize(null, this.generator, this.serializers);
        Mockito.verify(this.generator, Mockito.never()).writeString(Mockito.anyString());
    }

    @Test
    void testFormat() throws IOException {
        final ZonedDateTime zdt = ZonedDateTime.now();
        ZDT_SERIALIZER.serialize(zdt, this.generator, this.serializers);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(this.generator, Mockito.times(1)).writeString(captor.capture());
        Assertions.assertEquals(DATE_TIME_FORMATTER.format(zdt), captor.getValue());
    }
}