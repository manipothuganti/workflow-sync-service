package com.wbd.distribute.workflowsyncservice.handler;

import com.scrippsnetworks.nonlinear.workflow.tracking.api.WorkFlow;
import com.scrippsnetworks.nonlinear.workflow.tracking.api.WorkFlowTask;
import com.wbd.distribute.playlisteventgenerator.api.PlannerEvent;
import com.wbd.distribute.workflowsyncservice.client.WFTSClient;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.wbd.distribute.workflowsyncservice.constants.WorkflowSyncServiceConstants.*;

@Component
public class DeleteWorkflowHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteWorkflowHandler.class);

    @Inject
    private WFTSClient wftsClient;

    public void handle(PlannerEvent plannerEvent) {
        LOGGER.info("Handling DELETE action for event: {}", plannerEvent);

        UUID workflowId = plannerEvent.getId();
        if (workflowId == null) {
            LOGGER.warn("Workflow ID is null for DELETE action, cannot proceed.");
            return;
        }

        WorkFlow workflow = prepareDeleteWorkFlow(workflowId);
        wftsClient.saveWorkFlowTasks(List.of(workflow));
    }

    private WorkFlow prepareDeleteWorkFlow(UUID workflowId) {
        WorkFlow workflow = new WorkFlow();
        workflow.setId(workflowId);
        workflow.setCaller(CALLER);
        workflow.setTasks(prepareDeleteWorkFlowTasks());
        return workflow;
    }

    private List<WorkFlowTask> prepareDeleteWorkFlowTasks() {
        List<WorkFlowTask> workflowTasks = new ArrayList<>();

        Map<String, String> taskStatuses = new HashMap<>();
        taskStatuses.put(TASK_ID_PLANNING, TASK_VALUE_REMOVED);
        taskStatuses.put(TASK_ID_PLANNING_DV, TASK_VALUE_NOT_APPLICABLE);

        taskStatuses.forEach((taskId, status) -> {
            WorkFlowTask workFlowTask = new WorkFlowTask();
            workFlowTask.setId(taskId);
            workFlowTask.setName(taskId);
            workFlowTask.setValue(status);
            workFlowTask.setCaller(CALLER);
            workflowTasks.add(workFlowTask);
        });

        return workflowTasks;
    }
}