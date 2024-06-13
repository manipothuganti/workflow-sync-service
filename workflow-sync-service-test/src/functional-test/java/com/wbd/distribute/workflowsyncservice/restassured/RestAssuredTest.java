package com.wbd.distribute.workflowsyncservice.restassured;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

public abstract class RestAssuredTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssuredTest.class);
    public static final String BASE_API_SYSTEM_KEY = "application.base.api.url";
    public static final String ROOT_CONTEXT_PATH = "/workflow-sync-service/";
    public static final String LOCAL_API_URL = "http://localhost:8080/";

    public static String baseUrl() {
        String value = System.getProperty(BASE_API_SYSTEM_KEY, LOCAL_API_URL);
        LOGGER.info("Using base URL {} for functional tests.", value);
        return value;
    }

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = baseUrl();
        RestAssured.basePath = ROOT_CONTEXT_PATH;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
