package com.wbd.distribute.workflowsyncservice.config;

import java.util.Objects;

public class RemoteServiceConfig {

    private String baseUrl;
    private String user;
    private String email;
    private String groups;
    private String contentType;
    private Integer connectionTimeout;
    private Integer readTimeout;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoteServiceConfig that = (RemoteServiceConfig) o;
        return Objects.equals(baseUrl, that.baseUrl) &&
                Objects.equals(user, that.user) &&
                Objects.equals(groups, that.groups) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout) &&
                Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, user, groups, contentType);
    }

}