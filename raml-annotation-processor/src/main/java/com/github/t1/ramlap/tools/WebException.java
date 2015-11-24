package com.github.t1.ramlap.tools;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A {@link WebApplicationException} with a more fluent {@link #initCause(Throwable)} named {@link #causedBy(Throwable)}
 * .
 */
public class WebException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    public WebException(Response response) {
        super(response);
    }

    /** More fluent alias to {@link #initCause(Throwable)} with non-checked type. */
    public WebException causedBy(Throwable cause) {
        super.initCause(cause);
        return this;
    }

    /** @deprecated use {@link #causedBy(Throwable)} instead. */
    @Override
    @Deprecated
    public synchronized WebException initCause(Throwable cause) {
        super.initCause(cause);
        return this;
    }
}
