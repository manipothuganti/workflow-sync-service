package com.wbd.distribute.workflowsyncservice.service;

import com.wbd.distribute.playlisteventgenerator.api.PlannerEvent;
import com.wbd.distribute.workflowsyncservice.handler.CreateWorkflowHandler;
import com.wbd.distribute.workflowsyncservice.handler.DeleteWorkflowHandler;
import com.wbd.distribute.workflowsyncservice.handler.ExpiredWorkflowHandler;
import com.wbd.distribute.workflowsyncservice.handler.UpdateWorkflowHandler;
import com.wbd.distribute.workflowsyncservice.util.AutomaticVisibilityExtender;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PlaylistEventReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistEventReader.class);
    private final CreateWorkflowHandler createWorkflowHandler;
    private final UpdateWorkflowHandler updateWorkflowHandler;
    private final DeleteWorkflowHandler deleteWorkflowHandler;
    private final ExpiredWorkflowHandler expiredWorkflowHandler;
    @Value("${sqs-visibility.max-extensions}")
    private int maxVisibilityExtensions;

    public PlaylistEventReader(CreateWorkflowHandler createWorkflowHandler, UpdateWorkflowHandler updateWorkflowHandler,
                               DeleteWorkflowHandler deleteWorkflowHandler, ExpiredWorkflowHandler expiredWorkflowHandler) {
        this.createWorkflowHandler = createWorkflowHandler;
        this.updateWorkflowHandler = updateWorkflowHandler;
        this.deleteWorkflowHandler = deleteWorkflowHandler;
        this.expiredWorkflowHandler = expiredWorkflowHandler;
    }

    @SqsListener(value = "${playlistEventsWorkflowSyncQueue}")
    public void processMessage(PlannerEvent plannerEvent, Visibility visibility) {
        AutomaticVisibilityExtender visibilityExtender = new AutomaticVisibilityExtender(
                plannerEvent.getId() != null ? plannerEvent.getId().toString() : null, visibility,
                Duration.ofMinutes(2),
                Duration.ofMinutes(1));
        visibilityExtender.start();
        try {
            if (isValid(plannerEvent)) {
                process(plannerEvent);
            } else {
                LOGGER.warn("Received invalid PlannerEvent: {}", plannerEvent);
            }
        } catch (Exception e) {
            LOGGER.error("Exception during processing the Planner Event message ", e);
            throw e;
        } finally {
            visibilityExtender.stop();
        }
    }

    private boolean isValid(PlannerEvent plannerEvent) {
        if (plannerEvent == null) {
            LOGGER.error("Received null PlannerEvent");
            return false;
        }

        if (plannerEvent.getPlaylist() == null) {
            LOGGER.error("PlannerEvent has null Playlist: {}", plannerEvent);
            return false;
        }

        if (plannerEvent.getPlaylist().getAssets() == null || plannerEvent.getPlaylist().getAssets().isEmpty()) {
            LOGGER.error("PlannerEvent has null Asset: {}", plannerEvent);
            return false;
        }

        return true;
    }

    public void process(PlannerEvent plannerEvent) {

        if (plannerEvent.getType().equals(PlannerEvent.EVENT_WORKFLOW)) {
            switch (plannerEvent.getAction()) {
                case PlannerEvent.CREATE:
                    createWorkflowHandler.handle(plannerEvent);
                    break;
                case PlannerEvent.UPDATE:
                    updateWorkflowHandler.handle(plannerEvent);
                    break;
                case PlannerEvent.DELETE:
                    deleteWorkflowHandler.handle(plannerEvent);
                    break;
                case PlannerEvent.EVENT_WORKFLOW:
                    expiredWorkflowHandler.handle(plannerEvent);
                    break;
                default:
                    LOGGER.warn("Unhandled action type: {}", plannerEvent.getAction());
            }
        }
    }
}