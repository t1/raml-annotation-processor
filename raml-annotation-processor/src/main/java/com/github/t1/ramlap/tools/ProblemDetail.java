package com.github.t1.ramlap.tools;

import static com.github.t1.ramlap.tools.StringTools.*;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.Family.*;
import static javax.xml.bind.annotation.XmlAccessType.*;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.t1.exap.JavaDoc;
import com.github.t1.ramlap.annotations.ApiResponse;
import com.github.t1.ramlap.scanner.StatusTypeXmlAdapter;

import io.swagger.annotations.ApiModelProperty;

/**
 * Defines a "problem detail" as a way to carry machine-readable details of errors in a HTTP response, to avoid the need
 * to invent new error response formats for HTTP APIs.
 * <p>
 * Best practice:
 *
 * <pre>
 * <code>
 * &#64;ApiResponse(status = NOT_FOUND)
 * public static class FooNotFound extends ProblemDetail {}
 * ...
 * &#64;GET
 * &#64;ApiResponse(FooNotFound.class)
 * public Response getFoo() {
 *     try {
 *         ...
 *     } catch (FileNotFoundException e) {
 *         throw new FooNotFound().toWebException().causedBy(e);
 *     }
 *     return new FooNotFound().detail("some detail").toResponse();
 * }
 * </code>
 * </pre>
 *
 * Some common convenience types are provided together with factory methods that return a {@link WebException}, e.g.
 * {@link #badRequest(String)}, as well a generic factory method {@link #webException(Status, String)}.
 * <p>
 * The {@link #toResponse()} is for use directly in the boundary, when you already return a Response.<br>
 * The {@link #toWebException()} is for nested validation code.<br>
 * These methods log the problem detail, so you can quickly find the {@link #instance()}. You may want to set the
 * {@link #LOGGER} factory, so your application log file is used. The log level is ERROR for 5xx status codes (includes
 * the stack trace), and INFO for all other codes.
 *
 * @see <a href="https://tools.ietf.org/html/draft-ietf-appsawg-http-problem-01">IETF: Problem Details for HTTP APIs</a>
 */
@XmlRootElement
@XmlAccessorType(NONE)
@JsonDeserialize(using = ProblemDetailJsonDeserializer.class)
public class ProblemDetail implements Cloneable {
    @ApiResponse(status = BAD_REQUEST)
    public static class BadRequest extends ProblemDetail {}

    public static WebApplicationException badRequest(String detail) {
        return new BadRequest().detail(detail).toWebException();
    }


    public static class ValidationFailed extends BadRequest {}

    public static WebApplicationException validationFailed(String detail) {
        return new ValidationFailed().detail(detail).toWebException();
    }


    @ApiResponse(status = NOT_FOUND)
    public static class NotFound extends ProblemDetail {}

    public static WebApplicationException notFound(String detail) {
        return new NotFound().detail(detail).toWebException();
    }


    @ApiResponse(status = UNAUTHORIZED)
    public static class Unauthorized extends ProblemDetail {}

    public static WebApplicationException unauthorized(String detail) {
        return new Unauthorized().detail(detail).toWebException();
    }


    @ApiResponse(status = INTERNAL_SERVER_ERROR)
    public static class InternalServerError extends ProblemDetail {}

    public static WebApplicationException internalServerError(String detail) {
        return new InternalServerError().detail(detail).toWebException();
    }


    public static WebApplicationException webException(Status status, String detail) {
        return new ProblemDetail().status(status).detail(detail).toWebException();
    }

    /** The prefix for problem media types to be completed by <code>+json</code>, etc. */
    public static final String APPLICATION_PROBLEM_TYPE_PREFIX = "application/problem";

    /** The String Content-Type for {@link ProblemDetail}s in JSON */
    public static final String APPLICATION_PROBLEM_JSON = APPLICATION_PROBLEM_TYPE_PREFIX + "+json";
    /** The {@link MediaType} Content-Type for {@link ProblemDetail}s in JSON */
    public static final MediaType APPLICATION_PROBLEM_JSON_TYPE = MediaType.valueOf(APPLICATION_PROBLEM_JSON);

    /** The String Content-Type for {@link ProblemDetail}s in XML */
    public static final String APPLICATION_PROBLEM_XML = APPLICATION_PROBLEM_TYPE_PREFIX + "+xml";
    /** The {@link MediaType} Content-Type for {@link ProblemDetail}s in XML */
    public static final MediaType APPLICATION_PROBLEM_XML_TYPE = MediaType.valueOf(APPLICATION_PROBLEM_XML);

    /** The default {@link #type} URN scheme and namespace */
    public static final String URN_PROBLEM_PREFIX = "urn:problem:";
    /** The default {@link #type} URN scheme and namespace for java types */
    public static final String URN_PROBLEM_JAVA_PREFIX = URN_PROBLEM_PREFIX + "java:";
    /** The default {@link #instance} URN scheme and namespace */
    public static final String URN_PROBLEM_INSTANCE_PREFIX = "urn:problem-instance:";

    public static ProblemDetail of(URI uri) {
        if (uri == null)
            return new ProblemDetail();
        ProblemDetail problem = null;
        if (uri.toString().startsWith(URN_PROBLEM_JAVA_PREFIX))
            problem = newInstance(uri.toString().substring(URN_PROBLEM_JAVA_PREFIX.length()));
        return (problem == null) ? new ProblemDetail().type(uri) : problem;
    }

    private static ProblemDetail newInstance(String typeName) {
        try {
            @SuppressWarnings("unchecked")
            Class<ProblemDetail> typeClass = (Class<ProblemDetail>) Class.forName(typeName);
            return typeClass.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            return null; // no compatible subclass available -> fall back to ProblemDetail
        }
    }

    public static ProblemDetail of(WebApplicationException webException) {
        return (ProblemDetail) webException.getResponse().getEntity();
    }

    public static Function<Class<? extends ProblemDetail>, URI> TYPE_URI_FACTORY =
            (t -> URI.create(URN_PROBLEM_JAVA_PREFIX + t.getName()));

    public static Function<Class<? extends ProblemDetail>, URI> INSTANCE_URI_FACTORY =
            (t -> URI.create(URN_PROBLEM_INSTANCE_PREFIX + UUID.randomUUID()));

    public static Function<ProblemDetail, Logger> LOGGER = (t -> LoggerFactory.getLogger("problemdetail"));

    @JavaDoc("A URI reference [RFC3986] that identifies the problem type. When dereferenced, it is encouraged to provide "
            + "human-readable documentation for the problem type (e.g., using HTML [W3C.REC-html401-19991224])." //
            + "<p>" //
            + "Defaults to the {@link #URN_PROBLEM_PREFIX} + \"java:\" + fully qualified class name.")
    @ApiModelProperty(example = "https://example.com/probs/out-of-credit")
    @XmlElement
    @JsonProperty
    private URI type;

    @JavaDoc("A short, human-readable summary of the problem type. It SHOULD NOT change from occurrence "
            + "to occurrence of the problem, except for purposes of localization.")
    @ApiModelProperty(example = "You do not have enough credit.")
    @XmlElement
    @JsonProperty
    private String title;

    @JavaDoc("The HTTP status code ([RFC7231], Section 6) generated by the origin server for this occurrence of the problem.")
    @ApiModelProperty(example = "403")
    @XmlElement
    @XmlJavaTypeAdapter(StatusTypeXmlAdapter.class)
    @JsonProperty
    private StatusType status;

    @JavaDoc("The full, human-readable explanation specific to this occurrence of the problem. "
            + "It MAY change from occurrence to occurrence of the problem.")
    @ApiModelProperty(example = "Your current balance is 30, but that costs 50.")
    @XmlElement
    @JsonProperty
    private String detail;

    @JavaDoc("A URI reference that identifies the specific occurrence of the problem. "
            + "It may or may not yield further information if dereferenced.")
    @ApiModelProperty(example = URN_PROBLEM_INSTANCE_PREFIX + "233e7b05-0500-4b0d-a7d8-f4b90dbfa40e")
    @XmlElement
    @JsonProperty
    private URI instance;

    public ProblemDetail() {
        Class<? extends ProblemDetail> type = getClass();
        this.type = TYPE_URI_FACTORY.apply(type);
        this.title = title(type);
        this.status = status(type);
        this.detail = null;
        this.instance = INSTANCE_URI_FACTORY.apply(type);
    }

    private String title(Class<? extends ProblemDetail> type) {
        if (type.isAnnotationPresent(ApiResponse.class) //
                && !type.getAnnotation(ApiResponse.class).title().isEmpty()) //
            return type.getAnnotation(ApiResponse.class).title();
        if (ProblemDetail.class == type)
            return null; // nothing of interest
        return camelCaseToWords(type.getSimpleName());
    }

    private StatusType status(Class<? extends ProblemDetail> type) {
        return (type.isAnnotationPresent(ApiResponse.class)) //
                ? type.getAnnotation(ApiResponse.class).status() //
                : ApiResponse.DEFAULT_STATUS;
    }

    public URI type() {
        return type;
    }

    public String title() {
        return title;
    }

    public StatusType status() {
        return status;
    }

    public String detail() {
        return detail;
    }

    public URI instance() {
        return instance;
    }

    @JsonIgnore
    public boolean isServerError() {
        return status.getFamily() == SERVER_ERROR;
    }


    @Override
    protected ProblemDetail clone() {
        try {
            return (ProblemDetail) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public ProblemDetail type(URI type) {
        ProblemDetail clone = clone();
        clone.type = type;
        return clone;
    }

    public ProblemDetail title(String title) {
        ProblemDetail clone = clone();
        clone.title = title;
        return clone;
    }

    public ProblemDetail status(StatusType status) {
        ProblemDetail clone = clone();
        clone.status = status;
        return clone;
    }

    public ProblemDetail detail(String detail) {
        ProblemDetail clone = clone();
        clone.detail = detail;
        return clone;
    }

    public ProblemDetail instance(URI instance) {
        ProblemDetail clone = clone();
        clone.instance = instance;
        return clone;
    }

    public Response toResponse() {
        return toResponseBuilder().build();
    }

    public ResponseBuilder toResponseBuilder() {
        log();
        return Response.status(status) //
                .entity(this) //
                .type(APPLICATION_PROBLEM_JSON_TYPE) // TODO support xml/yaml/etc.
                ;
    }

    public WebException toWebException() {
        Response response = toResponse();
        if (isServerError())
            return new WebException(response);
        return new WebApplicationApplicationException(response);
    }

    public void log() {
        Logger logger = LOGGER.apply(this);
        if (isServerError())
            logger.error("{}", this);
        else
            logger.info("{}", this);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((detail == null) ? 0 : detail.hashCode());
        result = prime * result + ((instance == null) ? 0 : instance.hashCode());
        result = prime * result + ((status == null) ? 0 : status.getStatusCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProblemDetail that = (ProblemDetail) obj;
        if (detail == null) {
            if (that.detail != null)
                return false;
        } else if (!detail.equals(that.detail))
            return false;
        if (instance == null) {
            if (that.instance != null)
                return false;
        } else if (!instance.equals(that.instance))
            return false;
        if (status == null) {
            if (that.status != null)
                return false;
        } else if (status.getStatusCode() != that.status.getStatusCode())
            return false;
        if (title == null) {
            if (that.title != null)
                return false;
        } else if (!title.equals(that.title))
            return false;
        if (type == null) {
            if (that.type != null)
                return false;
        } else if (!type.equals(that.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" //
                + ((type == null) ? "" : type) //
                + ((title == null) ? "" : (" \"" + title + "\"")) //
                + ((status == null) ? "" : (" " + status.getStatusCode() + " " + status.getReasonPhrase())) //
                + ((detail == null) ? "" : ": \"" + detail + "\"") //
                + ((instance == null) ? "" : (" [" + instance + "]")) //
                ;
    }
}
