package com.wbd.distribute.workflowsyncservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "logging.file.name=build/logs/unittest.log")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// Default to local profile,
// but a project may override to "integration" if an integration test specific profile is needed
@ActiveProfiles("local")
public class SpringBootApplicationTest {

    @Test
    @DisplayName("Context loads on application start")
    public void contextLoads() {
        //this test exists to verify that the application context config is complete.
        // If it does not throw an exception and fail, then springboot was able to load the local app context completely
    }

}
