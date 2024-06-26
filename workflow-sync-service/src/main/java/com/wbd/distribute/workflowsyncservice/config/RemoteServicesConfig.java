package com.wbd.distribute.workflowsyncservice.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties("remote-services")
public class RemoteServicesConfig {

    @Min(value = -1, groups = TimeoutsConfigGroup.class)
    @Max(value = 60000, groups = TimeoutsConfigGroup.class)
    int defaultReadTimeout;
    @Min(value = -1, groups = TimeoutsConfigGroup.class)
    @Max(value = 60000, groups = TimeoutsConfigGroup.class)
    int defaultConnectionTimeout;
    @Min(value = 1, groups = PerformanceConfigGroup.class)
    @Max(value = 5000, groups = PerformanceConfigGroup.class)
    int schedulerMaxThreads;
    private RemoteServiceConfig workflowTrackingService;
    private RemoteServiceConfig nlcdConfigService;
    private RemoteServiceConfig metadataExtensionServiceConfig;
    private RemoteServiceConfig programScheduleServiceConfig;
    private RemoteServiceConfig alternateInventoryConfig;

    public int getSchedulerMaxThreads() {
        return schedulerMaxThreads;
    }

    public void setSchedulerMaxThreads(int schedulerMaxThreads) {
        this.schedulerMaxThreads = schedulerMaxThreads;
    }

    public int getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    public void setDefaultReadTimeout(int defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }

    public int getDefaultConnectionTimeout() {
        return defaultConnectionTimeout;
    }

    public void setDefaultConnectionTimeout(int defaultConnectionTimeout) {
        this.defaultConnectionTimeout = defaultConnectionTimeout;
    }

    public RemoteServiceConfig getWorkflowTrackingService() {
        return workflowTrackingService;
    }

    public void setWorkflowTrackingService(RemoteServiceConfig workflowTrackingService) {
        this.workflowTrackingService = workflowTrackingService;
    }

    public RemoteServiceConfig getNlcdConfigService() {
        return nlcdConfigService;
    }

    public void setNlcdConfigService(RemoteServiceConfig nlcdConfigService) {
        this.nlcdConfigService = nlcdConfigService;
    }


    public RemoteServiceConfig getMetadataExtensionServiceConfig() {
        return metadataExtensionServiceConfig;
    }

    public void setMetadataExtensionServiceConfig(RemoteServiceConfig metadataExtensionServiceConfig) {
        this.metadataExtensionServiceConfig = metadataExtensionServiceConfig;
    }

    public RemoteServiceConfig getProgramScheduleServiceConfig() {
        return programScheduleServiceConfig;
    }

    public void setProgramScheduleServiceConfig(RemoteServiceConfig programScheduleServiceConfig) {
        this.programScheduleServiceConfig = programScheduleServiceConfig;
    }

    public RemoteServiceConfig getAlternateInventoryConfig() {
        return alternateInventoryConfig;
    }

    public void setAlternateInventoryConfig(RemoteServiceConfig alternateInventoryConfig) {
        this.alternateInventoryConfig = alternateInventoryConfig;
    }
}