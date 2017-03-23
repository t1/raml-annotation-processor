package com.github.t1.ramlap.parsed;

import io.swagger.annotations.*;

import static javax.ws.rs.core.MediaType.*;

@SwaggerDefinition(
        info = @Info(title = "test title", version = "test-version") ,
        produces = { APPLICATION_JSON, APPLICATION_XML }
)
public class SwaggerDefinitionClass {}
