package com.github.t1.ramlap.scanner;

import org.raml.model.Raml;

public class XRaml extends Raml {
    private static final long serialVersionUID = 1L;

    @Override
    public String getBasePath() {
        // skip protocol separator "//"
        int start = getBaseUri().indexOf("//");
        start = (start == -1) ? 0 : start + 2;
        start = getBaseUri().indexOf("/", start);
        return getBaseUri().substring(start);
    }
}
