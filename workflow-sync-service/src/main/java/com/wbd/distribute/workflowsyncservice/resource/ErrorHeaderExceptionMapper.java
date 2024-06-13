package com.wbd.distribute.workflowsyncservice.resource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This exception mapper handles uncaught exceptions in any Jersey ReST endpoints,
 * converts them to WebApplicationExceptions (if necessary), and sets the warning
 * header on the response back to the caller using the exception in the message.
 */
@Provider
public class ErrorHeaderExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHeaderExceptionMapper.class);

    public static final String WARNING_HEADER = "Warning";

    @Override
    public Response toResponse(Exception exception) {

        WebApplicationException waexc = translate(exception);

        Response response = waexc.getResponse();
        response.getHeaders().add(WARNING_HEADER, exception.getMessage());

        LOGGER.error("An Exception occurred. HTTP status: {}, cause: {}, message: {}",
                response.getStatus(), waexc.getCause(), waexc.getMessage());

        return response;
    }

    private WebApplicationException translate(Exception exception) {
        WebApplicationException waexc;

        if (isInputException(exception)) {
            waexc = new WebApplicationException(exception, Status.BAD_REQUEST);
        } else if (exception instanceof WebApplicationException wea) {
            waexc = wea;
        } else {
            waexc = new WebApplicationException(exception);
        }
        return waexc;
    }

    private boolean isInputException(Exception e) {
        return (e instanceof IllegalArgumentException || e instanceof JsonProcessingException);
    }
}
