package com.github.t1.ramlap.generator;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/** root resource description */
@Path("/root")
public interface RootResource {
    /**
     * root get description
     * with multiple lines
     */
    @GET
    public Response getString();

    @PUT
    public Response putString();

    @GET
    @Path("/sub")
    public Response subGet();

    @GET
    @Path("/sub/sub")
    public Response subSubGet();

    @GET
    @Path("/sub/sub/sub")
    public Response subSubSubGet();

    @GET
    @Path("/othersub")
    public Response othersubGet();

    @GET
    @Path("/{path-param}")
    public Response getWithPathParam(@PathParam("path-param") String pathParam);

}
