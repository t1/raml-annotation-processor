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

}
