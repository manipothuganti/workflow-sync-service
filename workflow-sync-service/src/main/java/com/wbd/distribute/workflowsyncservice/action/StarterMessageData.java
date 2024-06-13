package com.wbd.distribute.workflowsyncservice.action;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wbd.distribute.workflowsyncservice.api.StarterMessage;

/**
 * A simple map based data-store for storing {@link StarterMessage}s
 */
public class StarterMessageData {

    private static final Logger LOGGER = LoggerFactory.getLogger(StarterMessageData.class);

    private final Map<String, StarterMessage> map = new HashMap<>();

    /**
     * @param id message identifier.
     * @return the {@link StarterMessage} for the given ID or null if one does not exist.
     */
    public StarterMessage read(String id) {
        return map.get(id);
    }

    /**
     * Saves the {@link StarterMessage} with the given ID overwriting any
     * that might exist. Does a simple validation check throwing an
     * {@code IllegalArgumentException} if there is a problem.
     *
     * @param id  message identifier.
     * @param msg message body.
     */
    public void write(String id, StarterMessage msg) {

        validate(id, msg);

        msg.setTimestamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")));
        msg.setId(id);
        msg.setReversed(new StringBuilder(msg.getMessage()).reverse().toString());

        map.put(id, msg);

        LOGGER.info("Saved message: {}", msg);
    }

    /**
     * Returns a list of all the {@link StarterMessage}s that have been saved.
     *
     * @return all starter messages
     */
    public Collection<StarterMessage> list() {
        return map.values();
    }

    private void validate(String id, StarterMessage msg) {
        if (null == msg.getId() || msg.getId().isEmpty() || null == id || id.isEmpty()) {
            throw new IllegalArgumentException("The path ID and the payload ID are required");
        }

        if (!msg.getId().equals(id)) {
            String error = "Path ID [" + id +
                    "] and payload ID [" + msg.getId() +
                    "] do not match.";
            throw new IllegalArgumentException(error);
        }
    }
}
