package com.github.t1.ramlap;

import javax.ejb.ApplicationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A {@link WebApplicationException} for non-server errors, annotated as {@link ApplicationException}, so the EJB
 * container doesn't handle them, i.e. wraps them into an EJBException, rollback transactions, destroy beans, etc..
 */
@ApplicationException
public class WebApplicationApplicationException extends WebException {
    private static final long serialVersionUID = 1L;

    public WebApplicationApplicationException(Response response) {
        super(response);
    }
}
