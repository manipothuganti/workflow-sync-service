package com.wbd.distribute.workflowsyncservice.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class holds the collection of StarterMessage objects so we aren't
 * returning a raw JSON array.
 */
public class StarterMessageList {
    public final List<StarterMessage> messages;

    public StarterMessageList(Collection<StarterMessage> messages) {
        this.messages = new ArrayList<>();
        this.messages.addAll(messages);
    }

    public List<StarterMessage> getMessages() {
        return messages;
    }

}
