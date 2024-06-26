package com.wbd.distribute.workflowsyncservice.util;

import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.stream.Stream;

import static com.discovery.foundry.sentry.SentryUser.GROUP_HEADER;
import static com.discovery.foundry.sentry.SentryUser.USERNAME_HEADER;

@Named
public class CredsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredsUtils.class);

    public synchronized HttpHeaders headersWithCreds(final ContainerRequestContext requestContext,
                                                     final HttpHeaders httpHeaders,
                                                     final Map<String, String> extraHeaders) {
        headersWithCreds(requestContext, httpHeaders);
        extraHeaders.forEach(httpHeaders::addIfAbsent);
        return httpHeaders;
    }

    public synchronized HttpHeaders headersWithCreds(final ContainerRequestContext requestContext,
                                                     final HttpHeaders httpHeaders) {

        if (httpHeaders.get(USERNAME_HEADER) == null) {
            final String user = getUser(requestContext);
            httpHeaders.add(USERNAME_HEADER, user);
        }


        final List<String> contextGroups = getGroups(requestContext);

        for (String contextGroup : contextGroups) {

            final List<String> groupsFromHeader = getGroups(httpHeaders);

            if (groupsFromHeader != null && !groupsFromHeader.contains(contextGroup)) {
                httpHeaders.add(GROUP_HEADER, contextGroup);
            }
        }

        return httpHeaders;
    }

    public synchronized List<String> headersForIntegrationTesting(final ContainerRequestContext requestContext,
                                                                  final String param) {

        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        var intTestParamValue = headers.get(param);

        if (intTestParamValue != null) {
            return new ArrayList<>(Arrays.asList(headers.get(param).get(0).split(",")));
        } else {
            return List.of();
        }
    }

    public synchronized MultivaluedMap<String, String> testHeadersWithCreds() {

        MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
        httpHeaders.put(USERNAME_HEADER, List.of("nlcd-user"));
        httpHeaders.put(GROUP_HEADER, List.of("SN_NLCD_PLANNING"));

        return httpHeaders;

    }

    // --
    public synchronized String getUser(final ContainerRequestContext requestContext) {

        if (requestContext != null) {

            final MultivaluedMap<String, String> headers = requestContext.getHeaders();

            return headers.get(USERNAME_HEADER) != null ?
                    headers.get(USERNAME_HEADER).get(0) : null;

        } else {
            return null;
        }

    }

    public synchronized List<String> getGroups(final ContainerRequestContext requestContext) {

        return getHeaders(GROUP_HEADER, requestContext);
    }

    public synchronized List<String> getHeader(String key, final ContainerRequestContext requestContext) {
        return getHeaders(key, requestContext);
    }

    private synchronized List<String> getGroups(final HttpHeaders headers) {

        return headers.get(GROUP_HEADER) != null ? headers.get(GROUP_HEADER) : new ArrayList<>();
    }

    // --

    private List<String> getHeaders(final String headerName, final ContainerRequestContext requestContext) {

        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        LOGGER.info("all headers: {}", headers);

        final String groups = getHeaderString(headers, headerName);
        if ((null == groups) || (groups.trim().length() == 0)) {
            return Collections.emptyList();
        }
        return Stream
                .of(groups.split("\\s*,\\s*"))
                .map(String::trim)
                .toList();
    }

    private String getHeaderString(final MultivaluedMap<String, String> headers, final String name) {

        final List<String> values = headers.get(name);
        if (values == null) {
            return null;
        }
        if (values.isEmpty()) {
            return "";
        }

        final Iterator<String> valuesIterator = values.iterator();
        StringBuilder buffer = new StringBuilder(valuesIterator.next());
        while (valuesIterator.hasNext()) {
            buffer.append(',').append(valuesIterator.next());
        }

        return buffer.toString();
    }
}