package com.wbd.distribute.workflowsyncservice.resource;

import com.wbd.distribute.playlisteventgenerator.api.PlannerEvent;
import com.wbd.distribute.workflowsyncservice.action.StarterMessageData;
import com.wbd.distribute.workflowsyncservice.api.StarterMessage;
import com.wbd.distribute.workflowsyncservice.api.StarterMessageList;
import com.wbd.distribute.workflowsyncservice.service.PlaylistEventReader;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is a sample jax-rs Jersey ReST service that produces and consumes
 * simple JSON objects at /api/v1/messages. This example is for illustration
 * only. Please delete or repurpose this class early in development.
 */
@Named
@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
public class StarterEndpoint {

    @Autowired
    PlaylistEventReader playlistEventReader;

    StarterMessageData data = new StarterMessageData();

    @GET
    public Response all() {
        return Response.ok(new StarterMessageList(data.list())).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        StarterMessage msg = data.read(id);

        if (null == msg) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok().entity(msg).build();
    }

    @PUT
    @Path("/{id}")
    public Response put(@PathParam("id") String id, StarterMessage msg) {
        try {
            data.write(id, msg);

            return Response.ok().entity(msg).build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(iae.getMessage(), iae, Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("/wss/test")
    public void pegMock(@RequestBody PlannerEvent plannerEvent) {
        playlistEventReader.process(plannerEvent);
    }
}
