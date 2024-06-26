package com.wbd.distribute.workflowsyncservice.client;

import com.google.common.collect.Lists;
import com.scrippsnetworks.nonlinear.workflow.tracking.api.WorkFlow;
import com.wbd.distribute.workflowsyncservice.WorkflowSyncServiceDataException;
import com.wbd.distribute.workflowsyncservice.config.BasicRxClientConfig;
import com.wbd.distribute.workflowsyncservice.config.RemoteServicesConfig;
import com.wbd.distribute.workflowsyncservice.constants.WorkflowSyncServiceConstants;
import com.wbd.distribute.workflowsyncservice.util.ClientCommunicationException;
import com.wbd.distribute.workflowsyncservice.util.CredsUtils;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class WFTSClientImpl implements WFTSClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WFTSClientImpl.class);

    @Inject
    RemoteServicesConfig remoteServicesConfig;

    @Inject
    @Qualifier("applicationJsonRestTemplate")
    RestTemplate restTemplate;

    BasicRxClientConfig basicRxClientConfig;

    @Inject
    @Qualifier("HttpClientScheduler")
    Scheduler httpClientScheduler;

    @Inject
    CredsUtils credsUtils;

    @PostConstruct
    void init() {

        basicRxClientConfig = new BasicRxClientConfig(remoteServicesConfig.getWorkflowTrackingService(),
                remoteServicesConfig, restTemplate);
    }

    public ClientCommunicationException wrapException(Throwable throwable, Object query) {
        return new ClientCommunicationException("WorkFlowService", throwable.getMessage(), query.toString());
    }

    public void saveWorkFlowTasks(final List<WorkFlow> workFlows) {
        if (CollectionUtils.isEmpty(workFlows)) {
            LOGGER.warn("Workflows are empty, nothing to save.");
            return;
        }
        LOGGER.info("Saving workflows ... total workflows # {}", workFlows.size());

        for (final List<WorkFlow> ws : Lists.partition(workFlows, 1)) {
            try {
                this.saveWorkflowTasksBatch(ws, null).blockingGet();
            } catch (final Exception e) {
                throw new WorkflowSyncServiceDataException(e.getLocalizedMessage(), e,
                        WorkflowSyncServiceConstants.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private Single<List<WorkFlow>> saveWorkflowTasksBatch(List<WorkFlow> request,
                                                          ContainerRequestContext requestContext) {
        LOGGER.info("Saving batch # {} of workflows ", request.size());
        final String remoteServiceUrl = basicRxClientConfig.getCurrentConfig().getBaseUrl();
        HttpHeaders httpHeaders = new HttpHeaders();

        if (requestContext != null) {
            credsUtils.headersWithCreds(requestContext, basicRxClientConfig.getHttpHeaders());
        }

        final String url = UriComponentsBuilder
                .fromHttpUrl(remoteServiceUrl).path("/workflows/tasks").build().toString();

        return Single.fromCallable(() -> {

            long start = System.currentTimeMillis();
            final ResponseEntity<List<WorkFlow>> response = restTemplate.exchange(url,
                    HttpMethod.PUT,
                    new HttpEntity<>(request, httpHeaders),
                    new ParameterizedTypeReference<>() {
                    },
                    MediaType.APPLICATION_JSON);

            LOGGER.info("Save workflows request for the batch processed in : {} ms",
                    (System.currentTimeMillis() - start));
            return response.getBody();

        }).doOnError(ex -> {
            LOGGER.error("error: {}", ex.toString());
            throw wrapException(ex, request);
        }).subscribeOn(httpClientScheduler);
    }
}