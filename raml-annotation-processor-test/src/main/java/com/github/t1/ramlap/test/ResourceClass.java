package com.github.t1.ramlap.test;

import static javax.ws.rs.core.Response.Status.*;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.ramlap.ApiResponse;
import com.github.t1.ramlap.ProblemDetail.ValidationFailed;

import io.swagger.annotations.ApiOperation;

/** p-resource. something about the p. */
@Path("/p")
public class ResourceClass {
    /** get action */
    @GET
    @Path("/{path-param}")
    @ApiOperation("get-op")
    @SuppressWarnings("unused")
    @ApiResponse(status = OK, title = "everything's fine")
    @ApiResponse(status = NOT_FOUND, title = "nothing there")
    @ApiResponse(type = ValidationFailed.class, title = "your data is no good")
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
