package com.wbd.distribute.workflowsyncservice.client;


import com.scrippsnetworks.nonlinear.workflow.tracking.api.WorkFlow;

import java.util.List;

public interface WFTSClient {

    void saveWorkFlowTasks(final List<WorkFlow> workFlows);
}