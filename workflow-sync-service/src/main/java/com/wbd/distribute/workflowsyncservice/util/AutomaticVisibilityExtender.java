package com.wbd.distribute.workflowsyncservice.util;

import io.awspring.cloud.sqs.listener.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutomaticVisibilityExtender {
    public static final Logger LOGGER = LoggerFactory.getLogger(AutomaticVisibilityExtender.class);
    private final String id;
    private final Visibility visibility;
    private final Duration visibilityDuration;
    private final Duration extensionDuration;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public AutomaticVisibilityExtender(String id, Visibility visibility, Duration visibilityDuration, Duration extensionDuration) {
        this.id = id;
        this.visibility = visibility;
        this.visibilityDuration = visibilityDuration;
        this.extensionDuration = extensionDuration;
    }

    public void start() {

        executorService.scheduleAtFixedRate(() -> {
            LOGGER.info("Extending message - {} visibility to {} seconds", id, visibilityDuration.getSeconds());
            try {
                visibility.changeTo((int) 60);
            } catch (Exception e) {
                LOGGER.error("Couldn't extend visibility - " + e.getMessage());
            }
        }, 30, 120, TimeUnit.SECONDS);
    }

    public void stop() {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LOGGER.error("Couldn't shutdown executorService - {}", e.getMessage());
        }
    }
}