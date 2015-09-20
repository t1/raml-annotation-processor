package com.github.t1.ramlap;

import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.nio.file.AccessMode;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;
import org.raml.model.parameter.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.Api;

@RunWith(MockitoJUnitRunner.class)
public class ParameterScannerTest extends AbstractScannerTest {
    @Test
    public void shouldSkipContextParam() {
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void get(@Context UriInfo uriInfo) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams).isEmpty();
    }

    @Test
    public void shouldScanPathParams() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/{s}/{i}-{d}-{e}")
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("s") String s, //
                    @JavaDoc(summary = "i-name", value = "i-desc") @PathParam("i") int i, //
                    @JavaDoc(summary = "d-name", value = "d-desc") @PathParam("d") double d, //
                    @PathParam("e") AccessMode e //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo/{s}/{i}-{d}-{e}", GET);
        then(raml.getResource("/foo/{s}").getUriParameters().get("s")) //
                .hasDisplayName("s") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isRequired() //
        // TODO required=false : boolean
        // TODO repeat : boolean
        // TODO pattern : String
        // TODO minLength : Integer
        // TODO maxLength : Integer
        // TODO minimum : BigDecimal
        // TODO maximum : BigDecimal
        // TODO defaultValue : String
        // TODO example : String
        ;
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams.size()).isEqualTo(3);
        then(pathParams.get("i")) //
                .hasDisplayName("i-name") //
                .hasDescription("i-desc") //
                .hasType(INTEGER) //
                .isRequired() //
                ;
        then(pathParams.get("d")) //
                .hasDisplayName("d-name") //
                .hasDescription("d-desc") //
                .hasType(NUMBER) //
                .isRequired() //
                ;
        then(pathParams.get("e")) //
                .hasDisplayName("e") //
                .hasDescription(null) //
                .hasType(STRING) //
                .hasEnumeration("EXECUTE", "READ", "WRITE") //
                .isRequired() //
                ;
        // TODO Type: DATE
        // TODO Type: FILE
        // TODO Named Parameters With Multiple Types
    }

    @Test
    public void shouldStripRegexFromPathParam() {
        @Path("")
        class Dummy {
            @GET
            @Path("/{p:.*}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("p") String p) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/{p}", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams.size()).isEqualTo(1);
        then(pathParams.get("p")) //
                .hasDisplayName("p") //
                .hasType(STRING) //
                ;
    }

    @Test
    public void shouldScanSeparateSuperPathParams() {
        @Path("/{foo}")
        class Dummy {
            @GET
            @Path("/{bar}")
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("foo") String foo, //
                    @PathParam("bar") String bar //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThat(raml.getResources()).containsOnlyKeys("/{foo}");
        assertThat(raml.getResource("/{foo}").getUriParameters()).containsOnlyKeys("foo");
        assertThat(raml.getResource("/{foo}").getResources()).containsOnlyKeys("/{bar}");
        assertThat(raml.getResource("/{foo}/{bar}").getUriParameters()).containsOnlyKeys("bar");
    }

    @Test
    public void shouldScanCombinedSuperPathParams() {
        @Path("/{foo}/{bar}")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("foo") String foo, //
                    @PathParam("bar") String bar //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThat(raml.getResources()).containsOnlyKeys("/{foo}");
        assertThat(raml.getResource("/{foo}").getResources()).containsOnlyKeys("/{bar}");
        assertThat(raml.getResource("/{foo}").getUriParameters()).containsOnlyKeys("foo");
        assertThat(raml.getResource("/{foo}/{bar}").getUriParameters()).containsOnlyKeys("bar");
    }

    @Test
    public void shouldMarkWarningsWhenPathParamNameIsMissing() {
        @Path("/{foo}")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        RamlScanner scanner = new RamlScanner();
        ReflectionType type = env.type(Dummy.class);

        scanner.scanJaxRsType(type);

        ReflectionMethod method = type.getMethod("getMethod");
        assertWarning(method, "no path param annotated as 'foo' found, but required in GET of '/{foo}'");
    }

    @Test
    public void shouldMarkWarningsWhenPathParamNamesDontMatch() {
        @Path("")
        class Dummy {
            @GET
            @Path("/{foo}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("bar") String bar) {}
        }

        Raml raml = scanTypes(Dummy.class);
        assertEquals(asSet("bar"), raml.getResource("/{foo}").getUriParameters().keySet());

        ReflectionMethod method = env.type(Dummy.class).getMethod("getMethod");
        assertWarning(method, "no path param annotated as 'foo' found, but required in GET of '/{foo}'");
        assertWarning(method.getParameter(0), "annotated path param name 'bar' not defined in GET of '/{foo}'");
    }

    @Test
    public void shouldScanQueryParams() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod( //
                    @QueryParam("q0") String q0, //
                    @JavaDoc(summary = "q-name", value = "q-desc") @QueryParam("q1") long q1, //
                    @QueryParam("q2") AccessMode q2 //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, QueryParameter> queryParams = action.getQueryParameters();
        assertThat(queryParams.size()).isEqualTo(3);
        then(queryParams.get("q0")) //
                .hasDisplayName("q0") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isNotRequired() //
                ;
        then(queryParams.get("q1")) //
                .hasDisplayName("q-name") //
                .hasDescription("q-desc") //
                .hasType(INTEGER) //
                .isNotRequired() //
                ;
        then(queryParams.get("q2")) //
                .hasDisplayName("q2") //
                .hasDescription(null) //
                .hasType(STRING) //
                .hasEnumeration("EXECUTE", "READ", "WRITE") //
                .isNotRequired() //
                ;
    }

    @Test
    public void shouldScanHeaderParams() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod( //
                    @HeaderParam("h0") String h0, //
                    @JavaDoc(summary = "h-name", value = "h-desc") @HeaderParam("h1") long h1 //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, Header> headerParams = action.getHeaders();
        assertThat(headerParams.size()).isEqualTo(2);
        then(headerParams.get("h0")) //
                .hasDisplayName("h0") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isNotRequired() //
                ;
        then(headerParams.get("h1")) //
                .hasDisplayName("h-name") //
                .hasDescription("h-desc") //
                .hasType(INTEGER) //
                .isNotRequired() //
                ;
    }

    @Test
    public void shouldScanBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes(APPLICATION_JSON)
            public void getMethod(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(1);
        then(body.get(APPLICATION_JSON)) //
                .hasType("string") //
                .hasSchema(null) //
                .hasExample(null) // TODO example
                ;
        // TODO form params
    }

    @Test
    public void shouldScanWildcardBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body).containsOnlyKeys(APPLICATION_JSON);
    }

    @Test
    public void shouldScanMethodsWithTwoMimeTypes() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes({ APPLICATION_JSON, APPLICATION_XML })
            public void getMethod(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        assertThat(body).containsOnlyKeys(APPLICATION_JSON, APPLICATION_XML);
    }

    @Test
    public void shouldScanTwoMethodsWithDifferentMimeTypes() {
        @Api
        @SuppressWarnings("unused")
        @Path("/p")
        class Dummy {
            @GET
            @Consumes(APPLICATION_JSON)
            public void getJson(String body) {}

            @GET
            @Consumes(APPLICATION_XML)
            public void getXml(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        assertThat(body).containsOnlyKeys(APPLICATION_JSON, APPLICATION_XML);

        assertMessages(NOTE, "path not unique");
    }

    @Test
    public void shouldScanBodyParamWithSchemaType() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes({ APPLICATION_JSON, APPLICATION_XML })
            public void getMethod(Pojo body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        String className = Pojo.class.getName();
        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        then(body.get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema("!include " + className + ".json") //
                .hasExample(null) // TODO json example
                ;
        then(body.get(APPLICATION_XML)) //
                .hasType(null) //
                .hasSchema("!include " + className + ".xsd") //
                .hasExample(null) // TODO xml example
                ;
        // TODO form params
        assertThat(raml.getSchemas()).hasSize(0);
    }

    // TODO cookie-param
    // TODO form-param
    // TODO bean-param

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryAndPathParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @Path("/{q}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("q") @QueryParam("q") String q) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p/{q}", GET);
        assertThat(action.getResource().getUriParameters().size()).isEqualTo(1);
        assertThat(action.getQueryParameters().size()).isEqualTo(1);

        assertWarning(env.type(Dummy.class).getMethod("getMethod").getParameter(0),
                "method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryAndHeaderParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod(@QueryParam("q") @HeaderParam("q") String q) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        assertThat(action.getHeaders().size()).isEqualTo(1);
        assertThat(action.getQueryParameters().size()).isEqualTo(1);

        assertWarning(env.type(Dummy.class).getMethod("getMethod").getParameter(0),
                "method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryAndPathAndHeaderParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @Path("/{q}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("q") @QueryParam("q") @HeaderParam("q") String q) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p/{q}", GET);
        assertThat(action.getResource().getUriParameters().size()).isEqualTo(1);
        assertThat(action.getQueryParameters().size()).isEqualTo(1);
        assertThat(action.getHeaders().size()).isEqualTo(1);

        assertWarning(env.type(Dummy.class).getMethod("getMethod").getParameter(0),
                "method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    @Test
    public void testName() {
        System.out.println(new Random().nextInt(9999));
    }
}
