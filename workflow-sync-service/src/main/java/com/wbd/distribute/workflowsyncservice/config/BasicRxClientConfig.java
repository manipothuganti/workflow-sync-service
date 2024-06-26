package com.wbd.distribute.workflowsyncservice.config;

import com.discovery.foundry.sentry.SentryUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class BasicRxClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRxClientConfig.class);
    final HttpHeaders httpHeaders = new HttpHeaders();
    RemoteServiceConfig currentConfig;
    int defaultReadTimeout;
    int defaultConnectionTimeout;

    public BasicRxClientConfig(final RemoteServiceConfig currentConfig, final RemoteServicesConfig remoteServicesConfig,
                               final RestTemplate restTemplate) {

        this.currentConfig = currentConfig;

        defaultReadTimeout = getDefaultReadTimeout(currentConfig, remoteServicesConfig);
        defaultConnectionTimeout = getDefaultConnectionTimeout(currentConfig, remoteServicesConfig);

        LOGGER.info("Setting config {} with defaultReadTimeout {} and defaultConnectionTimeout {}", currentConfig,
                defaultReadTimeout, defaultConnectionTimeout);

        if (restTemplate != null) {
            // setting timeouts
            final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setReadTimeout(defaultReadTimeout);
            requestFactory.setConnectTimeout(defaultConnectionTimeout);
            // requestFactory.setOutputStreaming(false); // Deprecated, no longer needed

            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        }
        if (currentConfig.getUser() != null) {
            httpHeaders.add(SentryUser.USERNAME_HEADER, currentConfig.getUser());
        }

        if (currentConfig.getGroups() != null) {

            final String groupsString = currentConfig.getGroups();

            if (groupsString != null) {

                final List<String> groups = Arrays.asList(groupsString.split("\\s*,\\s*"));

                groups.forEach(group -> httpHeaders.add(SentryUser.GROUP_HEADER, group));
            }
        }

        if (currentConfig.getContentType() != null) {
            httpHeaders.setContentType(MediaType.valueOf(currentConfig.getContentType()));
        }
    }

    private int getDefaultConnectionTimeout(RemoteServiceConfig currentConfig,
                                            RemoteServicesConfig remoteServicesConfig) {
        return currentConfig.getConnectionTimeout() != null ? currentConfig.getConnectionTimeout() :
                remoteServicesConfig.getDefaultConnectionTimeout();
    }

    private int getDefaultReadTimeout(RemoteServiceConfig currentConfig, RemoteServicesConfig remoteServicesConfig) {
        return currentConfig.getReadTimeout() != null ? currentConfig.getReadTimeout() :
                remoteServicesConfig.getDefaultReadTimeout();
    }

    public boolean isConfigChanged(RemoteServiceConfig newConfig, final RemoteServicesConfig remoteServicesConfig) {

        return !newConfig.equals(currentConfig) ||
                getDefaultConnectionTimeout(newConfig, remoteServicesConfig) != defaultConnectionTimeout ||
                getDefaultReadTimeout(newConfig, remoteServicesConfig) != defaultReadTimeout;
    }


    public RemoteServiceConfig getCurrentConfig() {
        return currentConfig;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }
}