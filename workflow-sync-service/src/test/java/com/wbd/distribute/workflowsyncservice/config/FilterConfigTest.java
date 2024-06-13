package com.wbd.distribute.workflowsyncservice.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FilterConfigTest {

    @Test
    void testCors() {
        FilterConfig config = new FilterConfig();
        Assertions.assertNotNull(config.cors());
    }
}
