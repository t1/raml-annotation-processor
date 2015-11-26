package com.github.t1.ramlap.generator;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/root")
public class RootResource {
    @GET
    public Response getString() {
        return null;
    }

    @PUT
    public Response putString() {
        return null;
    }

}
