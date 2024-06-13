package com.wbd.distribute.workflowsyncservice.resource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorHeaderExceptionMapperTest {

    @Test
    void testWithWebAppException() {
        ErrorHeaderExceptionMapper sut = new ErrorHeaderExceptionMapper();
        WebApplicationException exception = new WebApplicationException("Exception message",
                Response.Status.BAD_REQUEST);

        Response response = sut.toResponse(exception);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertNotNull(response.getHeaderString(ErrorHeaderExceptionMapper.WARNING_HEADER));
    }

    @Test
    void testWithOtherException() {
        ErrorHeaderExceptionMapper sut = new ErrorHeaderExceptionMapper();
        IllegalStateException exception = new IllegalStateException();

        Response response = sut.toResponse(exception);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(500, response.getStatus());
        Assertions.assertNotNull(response.getHeaderString(ErrorHeaderExceptionMapper.WARNING_HEADER));
    }

    @Test
    void testWithJsonProcessingProblem() {
        ErrorHeaderExceptionMapper sut = new ErrorHeaderExceptionMapper();
        IllegalArgumentException exception = new IllegalArgumentException();

        Response response = sut.toResponse(exception);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertNotNull(response.getHeaderString(ErrorHeaderExceptionMapper.WARNING_HEADER));
    }
}
