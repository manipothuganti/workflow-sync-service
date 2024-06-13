package com.wbd.distribute.workflowsyncservice.restassured;

import static io.restassured.RestAssured.get;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("smoke")
public class ActuatorEndpointTest extends RestAssuredTest {

    @Test
    public void testInfoActuator() {
        get("actuator/info").then()
                .statusCode(200)
                .and().log().body();
    }

    @Test
    public void testHealthActuator() {
        get("actuator/health").then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .and().log().body();
    }
}
