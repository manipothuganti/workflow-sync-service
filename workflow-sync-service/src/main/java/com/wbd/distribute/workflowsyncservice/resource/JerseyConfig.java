package com.wbd.distribute.workflowsyncservice.resource;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

/**
 * This class is responsible for configuring Jersey ReST services and
 * features.
 */
@Named
@ApplicationPath("/api/v1")
public final class JerseyConfig extends ResourceConfig {

    @Inject
    public JerseyConfig(final StarterEndpoint endpoint) {
        property(ServerProperties.MOXY_JSON_FEATURE_DISABLE, true);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);

        register(RequestContextFilter.class);
        register(ErrorHeaderExceptionMapper.class);
        register(endpoint);
    }

}
