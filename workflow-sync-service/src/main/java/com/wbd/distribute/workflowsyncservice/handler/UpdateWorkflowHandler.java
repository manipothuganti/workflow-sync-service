package com.wbd.distribute.workflowsyncservice.handler;

import com.scrippsnetworks.nonlinear.workflow.tracking.api.WorkFlow;
import com.scrippsnetworks.nonlinear.workflow.tracking.api.WorkFlowTask;
import com.wbd.distribute.playlisteventgenerator.api.PlannerEvent;
import com.wbd.distribute.workflowsyncservice.client.WFTSClient;
import com.wbd.distribute.workflowsyncservice.util.WorkflowUtils;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.wbd.distribute.workflowsyncservice.constants.AttributeConstants.EXPECTED__ATTRIBUTES;
import static com.wbd.distribute.workflowsyncservice.constants.WorkflowSyncServiceConstants.*;

@Component
public class UpdateWorkflowHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateWorkflowHandler.class);

    @Inject
    WFTSClient wftsClient;

    public static WorkFlow prepareWorkFlow(final String workflowId, Map<String, String> tasksStatuses, Map<String, Map<String, String>> taskAttributes) {
        WorkFlow workFlow = new WorkFlow();
        workFlow.setId(UUID.fromString(workflowId));
        List<WorkFlowTask> workflowTasks = new ArrayList<>();

        tasksStatuses.forEach((taskId, status) -> {
            WorkFlowTask workFlowTask = new WorkFlowTask();
            workFlowTask.setId(taskId);
            workFlowTask.setName(taskId);
            workFlowTask.setValue(status);
            workFlowTask.setCaller(CALLER);
            workFlowTask.setAtts(taskAttributes.get(taskId));
            workflowTasks.add(workFlowTask);
        });
        workFlow.setTasks(workflowTasks);
        workFlow.setCaller(CALLER);
        return workFlow;
    }

    public void handle(PlannerEvent plannerEvent) {
        LOGGER.info("Handling UPDATE action for event: {}", plannerEvent);

        Map<String, String> taskStatuses = new HashMap<>();
        Map<String, Map<String, String>> taskAttributes = new HashMap<>();
        List<String> expectedAttributes = new ArrayList<>();

        try {
            Optional.ofNullable(plannerEvent.getUpdates())
                    .ifPresent(updates -> updates.forEach(updateInfo -> {
                        if (PATH_ASSET_LAUNCH_DATES.equals(updateInfo.getPath())) {
                            String nlcdStatus;
                            if (updateInfo.getOldValue() == null && updateInfo.getNewValue() != null) {
                                nlcdStatus = TASK_VALUE_SCHEDULED;
                                taskStatuses.put(TASK_ID_PLANNING, nlcdStatus);

                            } else if (updateInfo.getOldValue() != null && updateInfo.getNewValue() == null) {
                                nlcdStatus = TASK_VALUE_UNSCHEDULED;
                                taskStatuses.put(TASK_ID_PLANNING, nlcdStatus);
                            }
                        }
                        if (EXPECTED__ATTRIBUTES.contains(updateInfo.getPath())) {
                            expectedAttributes.add(updateInfo.getPath());
                        }
                    }));

            if (plannerEvent.getPlaylist() != null) {
                String dvStatus = WorkflowUtils.getStatus(plannerEvent.getPlaylist(), TASK_ID_PLANNING_DV);
                taskStatuses.put(TASK_ID_PLANNING_DV, dvStatus);
                if (!expectedAttributes.isEmpty()) {
                    Map<String, String> dvPlanningTaskAttributes = WorkflowUtils.getAttributes(plannerEvent.getPlaylist(), expectedAttributes);
                    taskAttributes.put(TASK_ID_PLANNING_DV, dvPlanningTaskAttributes);
                }
            } else {
                LOGGER.warn("Playlist is null in the plannerEvent.");
            }

            if (!taskStatuses.isEmpty()) {
                UUID workflowId = plannerEvent.getId();
                if (workflowId != null) {
                    WorkFlow workflow = prepareWorkFlow(workflowId.toString(), taskStatuses, taskAttributes);
                    wftsClient.saveWorkFlowTasks(List.of(workflow));
                } else {
                    LOGGER.warn("Workflow ID is null for plannerEvent: {}", plannerEvent);
                }
            } else {
                LOGGER.info("No task statuses to update for plannerEvent: {}", plannerEvent);
            }

        } catch (Exception e) {
            LOGGER.error("Error handling UPDATE action for event: {}", plannerEvent, e);
        }
    }
}