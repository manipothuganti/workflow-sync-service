package com.wbd.distribute.workflowsyncservice;

public class WorkflowSyncServiceDataException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -1135488396347279568L;

    private final Integer errorCode;

    private final String message;

    public WorkflowSyncServiceDataException(String message, Integer errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }

    public WorkflowSyncServiceDataException(String message, Exception e, Integer errorCode) {
        super(message, e);
        this.message = message;
        this.errorCode = errorCode;
    }

    public WorkflowSyncServiceDataException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.errorCode = null;
    }

    public WorkflowSyncServiceDataException(String message) {
        super(message);
        this.message = message;
        this.errorCode = null;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

}