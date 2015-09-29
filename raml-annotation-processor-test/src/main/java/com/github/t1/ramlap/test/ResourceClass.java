package com.github.t1.ramlap.test;

import io.swagger.annotations.*;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.github.t1.exap.JavaDoc;

/** p-resource. something about the p. */
@Api
@Path("/p")
public class ResourceClass {
    /** get action */
    @GET
    @Path("/{path-param}")
    @ApiOperation("get-op")
    @SuppressWarnings("unused")
    @ApiResponse(code = 400, message = "you did it wrong")
    public List<Pojo> doGet( //
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
            @MatrixParam("matrix-param-1") Boolean matrixParam1 //
            , //
            Pojo body//
    ) {
        return null;
    }
}
