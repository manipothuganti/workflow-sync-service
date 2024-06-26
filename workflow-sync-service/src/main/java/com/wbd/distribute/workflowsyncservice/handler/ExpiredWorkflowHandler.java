package com.wbd.distribute.workflowsyncservice.handler;

import com.wbd.distribute.playlisteventgenerator.api.PlannerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExpiredWorkflowHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredWorkflowHandler.class);

    public void handle(PlannerEvent plannerEvent) {
        LOGGER.info("Handling EXPIRED action for event: {}", plannerEvent);
        // Implement the logic for handling expired workflows
    }
}