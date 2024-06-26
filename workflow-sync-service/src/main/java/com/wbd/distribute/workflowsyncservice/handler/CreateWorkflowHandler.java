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
public class CreateWorkflowHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateWorkflowHandler.class);

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
        LOGGER.info("Handling CREATE action for event: {}", plannerEvent);
        try {
            Map<String, String> taskStatuses = new HashMap<>();
            Map<String, Map<String, String>> taskAttributes = new HashMap<>();

            Optional.ofNullable(plannerEvent.getPlaylist()).ifPresent(playlist -> {
                String nlcdStatus = WorkflowUtils.getStatus(playlist, TASK_ID_PLANNING);
                Map<String, String> nlcdTaskAttributes = WorkflowUtils.getAttributes(playlist, new ArrayList<>());
                taskStatuses.put(TASK_ID_PLANNING, nlcdStatus);
                taskAttributes.put(TASK_ID_PLANNING, nlcdTaskAttributes);

                String dvStatus = WorkflowUtils.getStatus(playlist, TASK_ID_PLANNING_DV);
                taskStatuses.put(TASK_ID_PLANNING_DV, dvStatus);
                Map<String, String> dvPlanningTaskAttributes = WorkflowUtils.getAttributes(playlist, EXPECTED__ATTRIBUTES);
                taskAttributes.put(TASK_ID_PLANNING_DV, dvPlanningTaskAttributes);
            });

            if (!taskStatuses.isEmpty()) {
                UUID workflowId = plannerEvent.getId();
                if (workflowId != null) {
                    WorkFlow workflow = prepareWorkFlow(workflowId.toString(), taskStatuses, taskAttributes);
                    wftsClient.saveWorkFlowTasks(List.of(workflow));
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error handling CREATE action for event: {}", plannerEvent, e);
        }
    }
}