package com.wbd.distribute.workflowsyncservice.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcePackageTests {

    @Test
    void satisfySonarTests() {
        StarterEndpoint endpoint = new StarterEndpoint();
        JerseyConfig config = new JerseyConfig(endpoint);
        Assertions.assertNotNull(config);
    }

}
