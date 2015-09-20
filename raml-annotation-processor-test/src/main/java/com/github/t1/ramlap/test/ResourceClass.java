package com.github.t1.ramlap.test;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.github.t1.exap.JavaDoc;

import io.swagger.annotations.*;

/** p-resource. something about the p. */
@Api
@Path("/p")
public class ResourceClass {
    /** get action */
    @GET
    @Path("/{path-param}")
    @ApiOperation("get-op")
    @SuppressWarnings("unused")
    public SomeEnum getEnum( //
            @Context UriInfo uriInfo //
            , //
            @JavaDoc(summary = "p", value = "p-param-descr") //
            @PathParam("path-param") int pathParam //
            , //
            @JavaDoc(summary = "h", value = "h-param-descr") //
            @HeaderParam("header-param") String headerParam //
            , //
            @JavaDoc(summary = "q", value = "q-param-descr") //
            @QueryParam("query-param") SomeEnum queryParam //
            , //
            @MatrixParam("matrix-param-0") String matrixParam0 //
            , //
            @MatrixParam("matrix-param-1") String matrixParam1 //
            , //
            Pojo body//
    ) {
        return SomeEnum.A;
    }
}
