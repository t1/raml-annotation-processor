package com.github.t1.ramlap;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.*;

import com.github.t1.exap.reflection.*;

import io.swagger.annotations.*;

public abstract class ResponseScanner {
    public static List<ResponseScanner> responses(Method method) {
        if (method.isAnnotated(ApiResponses.class))
            return apiResponses(method);
        if (method.isAnnotated(ApiResponse.class)) // ApiResponse(s) is not yet @Repeatable
            return asList(new ApiResponseScanner(method, method.getAnnotationWrapper(ApiResponse.class)));
        return asList(new DefaultResponseScanner(method));
    }

    private static List<ResponseScanner> apiResponses(Method method) {
        List<ResponseScanner> list = new ArrayList<>();
        for (AnnotationWrapper responses : method.getAnnotationWrappers(ApiResponses.class))
            for (AnnotationWrapper response : responses.getAnnotationsValue("value"))
                list.add(new ApiResponseScanner(method, response));
        return list;
    }

    public static class ResponseHeaderScanner {
        private final AnnotationWrapper header;

        public ResponseHeaderScanner(AnnotationWrapper header) {
            this.header = header;
        }

        public String name() {
            return (String) header.getValue("name");
        }

        public String description() {
            return (String) header.getValue("description");
        }

        public Type response() {
            return header.getTypeValue("response");
        }
    }

    private static class DefaultResponseScanner extends ResponseScanner {
        private final Method method;

        public DefaultResponseScanner(Method method) {
            this.method = method;
        }

        @Override
        public Status status() {
            return OK;
        }

        @Override
        public String description() {
            return "Success";
        }

        @Override
        public List<ResponseHeaderScanner> responseHeaders() {
            return emptyList();
        }

        @Override
        public Type responseType() {
            return method.getReturnType();
        }
    }

    private static class ApiResponseScanner extends ResponseScanner {
        private final Method method;
        private final AnnotationWrapper apiResponse;

        public ApiResponseScanner(Method method, AnnotationWrapper apiResponses) {
            this.method = method;
            this.apiResponse = apiResponses;
        }

        @Override
        public Status status() {
            return Status.fromStatusCode(apiResponse.getIntValue("code"));
        }

        @Override
        public String description() {
            return apiResponse.getStringValue("message");
        }

        @Override
        public List<ResponseHeaderScanner> responseHeaders() {
            List<ResponseHeaderScanner> list = new ArrayList<>();
            for (AnnotationWrapper header : apiResponse.getAnnotationsValue("responseHeaders"))
                if (!isDefaultResponseHeader(header))
                    list.add(new ResponseHeaderScanner(header));
            return list;
        }

        private boolean isDefaultResponseHeader(AnnotationWrapper header) {
            return header.getStringValue("name").isEmpty();
        }

        @Override
        public Type responseType() {
            Type type = apiResponse.getTypeValue("response");
            if (type == null)
                return null;
            if (!type.isVoid())
                return type;
            return method.getReturnType();
        }
    }

    public abstract StatusType status();

    public abstract String description();

    public abstract List<ResponseHeaderScanner> responseHeaders();

    public abstract Type responseType();
}
