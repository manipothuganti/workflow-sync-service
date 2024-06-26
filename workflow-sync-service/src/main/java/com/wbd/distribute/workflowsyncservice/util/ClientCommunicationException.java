package com.wbd.distribute.workflowsyncservice.util;

public class ClientCommunicationException extends RuntimeException {

    private final String clientName;

    private final String query;

    public ClientCommunicationException(final String clientName, final String msg, final String query) {
        super(msg);
        this.clientName = clientName;
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public String getClientName() {
        return clientName;
    }


    @Override
    public String getMessage() {
        return clientName + ":" + super.getMessage();
    }

    @Override
    public String toString() {
        return "ClientCommunicationException{" +
                "clientName='" + clientName + '\'' +
                ", query='" + query + '\'' +
                '}';
    }
}