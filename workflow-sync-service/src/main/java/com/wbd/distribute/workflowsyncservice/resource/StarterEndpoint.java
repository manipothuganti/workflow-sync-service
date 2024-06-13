package com.wbd.distribute.workflowsyncservice.resource;

import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.wbd.distribute.workflowsyncservice.action.StarterMessageData;
import com.wbd.distribute.workflowsyncservice.api.StarterMessage;
import com.wbd.distribute.workflowsyncservice.api.StarterMessageList;

/**
 * This is a sample jax-rs Jersey ReST service that produces and consumes
 * simple JSON objects at /api/v1/messages. This example is for illustration
 * only. Please delete or repurpose this class early in development.
 */
@Named
@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
public class StarterEndpoint {

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

}
