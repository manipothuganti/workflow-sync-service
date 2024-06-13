package com.wbd.distribute.workflowsyncservice.resource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.wbd.distribute.workflowsyncservice.api.StarterMessage;
import com.wbd.distribute.workflowsyncservice.api.StarterMessageList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StarterEndpointTest {
    @Test
    void testNotFound() {
        StarterEndpoint endpoint = new StarterEndpoint();
        Response r = endpoint.get("hello");

        Assertions.assertEquals(404, r.getStatus());
    }

    @Test
    void testCreateThenRead() {
        StarterEndpoint endpoint = new StarterEndpoint();

        StarterMessage m = new StarterMessage();
        m.setId("1");
        m.setMessage("Hello");
        m.setUserName("World");

        StarterMessage resp = (StarterMessage) endpoint.put("1", m).getEntity();

        Assertions.assertEquals("1", resp.getId());

        StarterMessage retrieved = (StarterMessage) endpoint.get("1").getEntity();

        Assertions.assertNotNull(retrieved);
        Assertions.assertEquals(resp, retrieved);
    }

    @Test
    void testAllWithNoMessages() {
        StarterEndpoint endpoint = new StarterEndpoint();
        StarterMessageList list = (StarterMessageList) endpoint.all().getEntity();

        Assertions.assertEquals(0, list.getMessages().size());
    }

    @Test
    void testAllWithMessages() {
        StarterEndpoint endpoint = new StarterEndpoint();

        StarterMessage m1 = new StarterMessage();
        m1.setId("1");
        m1.setMessage("Hello");
        endpoint.put("1", m1);

        StarterMessage m2 = new StarterMessage();
        m2.setId("2");
        m2.setMessage("greeting");
        endpoint.put("2", m2);

        StarterMessageList list = (StarterMessageList) endpoint.all().getEntity();
        Assertions.assertEquals(2, list.getMessages().size());
    }

    //A mismatch in payload and path ids or if either the path id or msg id is null/empty should result in a 400
    @ParameterizedTest
    @CsvSource(value = {"1,2", "null,2", ",2", "1,null", "1,", "null,null"}, nullValues = {"null"})
    void TestValidation(String msgId, String pathId) {
        StarterEndpoint endpoint = new StarterEndpoint();

        StarterMessage m = new StarterMessage();

        m.setId(msgId);

        WebApplicationException e = Assertions.assertThrows(WebApplicationException.class,
                () -> endpoint.put(pathId, m));

        Assertions.assertEquals(400, e.getResponse().getStatus());
    }
}