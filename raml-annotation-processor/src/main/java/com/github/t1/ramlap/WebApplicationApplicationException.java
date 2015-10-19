package com.github.t1.ramlap;
import javax.ejb.ApplicationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A {@link WebApplicationException} for client errors, annotated as {@link ApplicationException},
 * so the EJB container doesn't wrap them unnecessarily.
 */
@ApplicationException
public class WebApplicationApplicationException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    public WebApplicationApplicationException(Response response) {
        super(response);
    }

    /** More fluent alias to {@link #initCause(Throwable)} */
    public WebApplicationApplicationException causedBy(Throwable cause) {
        initCause(cause);
        return this;
    }
}
