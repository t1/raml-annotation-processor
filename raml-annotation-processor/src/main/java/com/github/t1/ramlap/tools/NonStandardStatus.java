package com.github.t1.ramlap.tools;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public class NonStandardStatus implements StatusType {
    private final int code;

    public NonStandardStatus(int code) {
        this.code = code;
    }

    @Override
    public int getStatusCode() {
        return code;
    }

    @Override
    public Family getFamily() {
        return Family.familyOf(code);
    }

    @Override
    public String getReasonPhrase() {
        return "?";
    }
}
