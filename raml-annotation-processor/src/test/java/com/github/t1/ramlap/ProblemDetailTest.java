package com.github.t1.ramlap;

import static com.github.t1.ramlap.ProblemDetail.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.URI;
import java.util.UUID;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;

@RunWith(MockitoJUnitRunner.class)
public class ProblemDetailTest {
    public static class FooProblem extends ProblemDetail {}

    private final ObjectMapper mapper = new ObjectMapper() //
            .setSerializationInclusion(Include.NON_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final URI instanceUrn = URI.create("dummy:" + UUID.randomUUID());

    @Mock
    Logger logger;

    // @Rule
    // public final MementoRule<Function<Class<? extends ProblemDetail>, URI>> typeUriFactoryMemento =
    // new MementoRule<>(() -> TYPE_URI_FACTORY, v -> TYPE_URI_FACTORY = v, t -> instanceUrn);

    @Rule
    public final MementoRule<Function<Class<? extends ProblemDetail>, URI>> instanceUriFactoryMemento =
            new MementoRule<>(() -> INSTANCE_URI_FACTORY, v -> INSTANCE_URI_FACTORY = v, t -> instanceUrn);

    @Rule
    public final MementoRule<Function<ProblemDetail, Logger>> loggerFactoryMemento =
            new MementoRule<>(() -> LOGGER, v -> LOGGER = v, t -> logger);

    private URI problemUrn(Class<? extends ProblemDetail> type) {
        return URI.create(URN_PROBLEM_JAVA_PREFIX + type.getName());
    }

    @Test
    public void shouldConstructFromNullTypeUri() {
        ProblemDetail problem = ProblemDetail.of((URI) null);

        assertThat(problem.type()).isEqualTo(problemUrn(ProblemDetail.class));
        assertThat(problem.title()).isNull();
        assertThat(problem.status()).isEqualTo(BAD_REQUEST);
        assertThat(problem.detail()).isNull();
        assertThat(problem.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldConstructFromHttpTypeUri() {
        URI uri = URI.create("http://example.org");

        ProblemDetail problem = ProblemDetail.of(uri);

        assertThat(problem.type()).isEqualTo(uri);
        assertThat(problem.title()).isNull();
        assertThat(problem.status()).isEqualTo(BAD_REQUEST);
        assertThat(problem.detail()).isNull();
        assertThat(problem.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldConstructFromUnknownJavaTypeUri() {
        URI uri = URI.create(URN_PROBLEM_JAVA_PREFIX + "undefined.Type");

        ProblemDetail problem = ProblemDetail.of(uri);

        assertThat(problem.type()).isEqualTo(uri);
        assertThat(problem.title()).isNull();
        assertThat(problem.status()).isEqualTo(BAD_REQUEST);
        assertThat(problem.detail()).isNull();
        assertThat(problem.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldConstructSubclassWithDefaultValues() {
        FooProblem problem = new FooProblem();

        assertThat(problem.type()).isEqualTo(problemUrn(FooProblem.class));
        assertThat(problem.title()).isEqualTo("foo problem");
        assertThat(problem.status()).isEqualTo(BAD_REQUEST);
        assertThat(problem.detail()).isNull();
        assertThat(problem.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldConstructSubclassWithApiResponseAnnotationWithoutTitle() {
        @ApiResponse(status = UNAUTHORIZED)
        class AnnotatedProblem extends ProblemDetail {}

        ProblemDetail problem = new AnnotatedProblem().detail("my detail");

        assertThat(problem.type()).isEqualTo(problemUrn(AnnotatedProblem.class));
        assertThat(problem.title()).isEqualTo("annotated problem");
        assertThat(problem.status()).isEqualTo(UNAUTHORIZED);
        assertThat(problem.detail()).isEqualTo("my detail");
        assertThat(problem.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldConstructSubclassWithApiResponseAnnotationWithTitle() {
        @ApiResponse(status = UNAUTHORIZED, title = "foo title")
        class AnnotatedProblem extends ProblemDetail {}

        ProblemDetail problem = new AnnotatedProblem().detail("my detail");

        assertThat(problem.type()).isEqualTo(problemUrn(AnnotatedProblem.class));
        assertThat(problem.title()).isEqualTo("foo title");
        assertThat(problem.status()).isEqualTo(UNAUTHORIZED);
        assertThat(problem.detail()).isEqualTo("my detail");
        assertThat(problem.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldExtractFromException() {
        FooProblem in = new FooProblem();
        WebApplicationException webException = new WebApplicationException(in.toResponse());

        ProblemDetail out = ProblemDetail.of(webException);

        assertThat(out.type()).isEqualTo(problemUrn(FooProblem.class));
        assertThat(out.title()).isEqualTo("foo problem");
        assertThat(out.status()).isEqualTo(BAD_REQUEST);
        assertThat(out.detail()).isNull();
        assertThat(out.instance()).isEqualTo(instanceUrn);
    }

    @Test
    public void shouldConvertToResponse() {
        FooProblem in = new FooProblem();

        Response response = in.toResponse();

        assertThat(response.getEntity()).isEqualTo(in);
        assertThat(response.getStatusInfo()).isEqualTo(BAD_REQUEST);
        assertThat(response.getMediaType()).isEqualTo(APPLICATION_PROBLEM_JSON_TYPE);
    }

    @Test
    public void shouldConvertServerErrorToWebApplicationException() {
        @ApiResponse(status = INTERNAL_SERVER_ERROR)
        class ServerProblem extends ProblemDetail {}
        ServerProblem problem = new ServerProblem();

        WebApplicationException webException = problem.toWebException();

        assertThat(webException).isNotInstanceOf(WebApplicationApplicationException.class);
    }

    @Test
    public void shouldConvertClientErrorToWebApplicationApplicationException() {
        @ApiResponse(status = UNAUTHORIZED)
        class ClientProblem extends ProblemDetail {}
        ClientProblem problem = new ClientProblem();

        WebApplicationException webException = problem.toWebException();

        assertThat(webException).isInstanceOf(WebApplicationException.class);
    }

    @Test
    public void shouldWriteToJson() throws Exception {
        FooProblem problem = new FooProblem();

        String json = mapper.writeValueAsString(problem);

        assertThat(json).isEqualTo(json(problem));
    }

    @Test
    public void shouldReadFromJson() throws Exception {
        FooProblem in = new FooProblem();

        ProblemDetail out = mapper.readValue(json(in), ProblemDetail.class);

        assertThat(out).isEqualTo(in);
    }

    private String json(FooProblem problem) {
        return "{" //
                + "\"type\":\"" + problemUrn(FooProblem.class) + "\"," //
                + "\"title\":\"foo problem\"," //
                + "\"status\":\"BAD_REQUEST\"," //
                + "\"instance\":\"" + problem.instance() + "\"" //
                + "}";
    }

    @Test
    public void shouldWriteToXml() {
        ProblemDetail problem = new ProblemDetail();
        StringWriter writer = new StringWriter();

        JAXB.marshal(problem, writer);

        assertThat(writer.toString()).isEqualTo(xml(problem));
    }

    @Test
    public void shouldReadFromXml() {
        ProblemDetail in = new ProblemDetail();

        ProblemDetail out = JAXB.unmarshal(new StringReader(xml(in)), ProblemDetail.class);

        assertThat(out).isEqualTo(in);
    }

    private String xml(ProblemDetail problem) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" //
                + "<problemDetail>\n" //
                + "    <type>" + problemUrn(ProblemDetail.class) + "</type>\n" //
                + "    <status>BAD_REQUEST</status>\n" //
                + "    <instance>" + problem.instance() + "</instance>\n" //
                + "</problemDetail>\n";
    }

    @Test
    public void shouldCoverToString() {
        new FooProblem().detail("foo").toString();
        new ProblemDetail().type(null).title(null).detail(null).status(null).instance(null).toString();
    }

    @Test
    public void shouldCoverHashCode() {
        new FooProblem().detail("foo").hashCode();
        new ProblemDetail().type(null).title(null).detail(null).status(null).instance(null).hashCode();
    }

    @Test
    public void shouldLogWhenBuildingResponse() {
        ProblemDetail problem = new FooProblem().detail("foo");

        problem.toResponse();

        verify(logger).info("{}", problem);
    }

    @Test
    public void shouldLogWhenBuildingResponseBuilder() {
        ProblemDetail problem = new FooProblem().detail("foo");

        problem.toResponseBuilder();

        verify(logger).info("{}", problem);
    }

    @Test
    public void shouldLogWhenBuildingWebException() {
        ProblemDetail problem = new FooProblem().detail("foo");

        problem.toWebException();

        verify(logger).info("{}", problem);
    }

    // TODO problem detail headers
    // TODO problem detail custom fields
}
