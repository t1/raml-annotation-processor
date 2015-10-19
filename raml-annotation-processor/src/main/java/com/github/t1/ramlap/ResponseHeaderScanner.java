package com.github.t1.ramlap;

import com.github.t1.exap.reflection.*;

public abstract class ResponseHeaderScanner {
    public static class ApiResponseHeaderScanner extends ResponseHeaderScanner {
        public ApiResponseHeaderScanner(AnnotationWrapper header) {
            super(header);
        }

        @Override
        public Type response() {
            return header.getTypeProperty("type");
        }
    }

    public static class SwaggerResponseHeaderScanner extends ResponseHeaderScanner {
        public SwaggerResponseHeaderScanner(AnnotationWrapper header) {
            super(header);
        }

        @Override
        public Type response() {
            return header.getTypeProperty("response");
        }
    }

    protected final AnnotationWrapper header;

    public ResponseHeaderScanner(AnnotationWrapper header) {
        this.header = header;
    }

    public String name() {
        return (String) header.getProperty("name");
    }

    public String description() {
        return (String) header.getProperty("description");
    }

    public abstract Type response();
}
