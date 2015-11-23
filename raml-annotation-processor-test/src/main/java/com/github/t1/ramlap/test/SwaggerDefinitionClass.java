package com.github.t1.ramlap.test;

import static javax.ws.rs.core.MediaType.*;

import io.swagger.annotations.*;

@SwaggerDefinition( //
        info = @Info(title = "test title", version = "test-version") , //
        produces = { APPLICATION_JSON, APPLICATION_XML } //
)
public class SwaggerDefinitionClass {}
