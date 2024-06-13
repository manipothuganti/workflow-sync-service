package com.wbd.distribute.workflowsyncservice.api;

import java.time.ZonedDateTime;

/**
 * This class is a simple struct-type data transfer object that is used by
 * this sample API.
 */
public class StarterMessage {
    private String id;
    private String message;
    private String reversed;
    private String userName;
    private ZonedDateTime timestamp;

    public String getId() {
        return id;
    }

    public StarterMessage setId(String id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StarterMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getReversed() {
        return reversed;
    }

    public StarterMessage setReversed(String reversed) {
        this.reversed = reversed;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public StarterMessage setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public StarterMessage setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return
                "StarterMessage [id=" + id +
                        ", message=" + message +
                        ", reversed=" + reversed +
                        ", username=" + userName +
                        ", timestamp=" + timestamp +
                        "]";
    }

}
