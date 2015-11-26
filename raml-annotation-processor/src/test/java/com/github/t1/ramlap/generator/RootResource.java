package com.github.t1.ramlap.generator;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/root")
public interface RootResource {
    @GET
    public Response getString();

    @PUT
    public Response putString();

}
