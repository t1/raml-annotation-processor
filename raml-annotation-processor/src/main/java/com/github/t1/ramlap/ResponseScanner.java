package com.github.t1.ramlap;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import io.swagger.annotations.*;

import java.util.ArrayList;
import java.util.List;

import com.github.t1.exap.reflection.*;

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
        for (AnnotationWrapper apiResponseWrapper : method.getAnnotationWrappers(ApiResponses.class))
            list.add(new ApiResponseScanner(method, apiResponseWrapper.getAnnotationWrapper(ApiResponse.class)));
        return list;
    }

    public static class ResponseHeaderScanner {
        private final AnnotationWrapper header;

        public ResponseHeaderScanner(AnnotationWrapper header) {
            this.header = header;
        }

        public String name() {
            return (String) header.get("name");
        }

        public String description() {
            return (String) header.get("description");
        }

        public Type response() {
            return header.getAnnotationWrapper(ApiResponse.class).getType("response");
        }
    }

    private static class DefaultResponseScanner extends ResponseScanner {
        private final Method method;

        public DefaultResponseScanner(Method method) {
            this.method = method;
        }

        @Override
        public String status() {
            return "200";
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
        private final AnnotationWrapper apiResponses;

        public ApiResponseScanner(Method method, AnnotationWrapper apiResponses) {
            this.method = method;
            this.apiResponses = apiResponses;
        }

        @Override
        public String status() {
            return apiResponses.getString("code");
        }

        @Override
        public String description() {
            return apiResponses.getString("message");
        }

        @Override
        public List<ResponseHeaderScanner> responseHeaders() {
            List<ResponseHeaderScanner> list = new ArrayList<>();
            for (AnnotationWrapper header : apiResponses.getAnnotationWrappers(ResponseHeader.class))
                list.add(new ResponseHeaderScanner(header));
            return list;
        }

        @Override
        public Type responseType() {
            Type type = apiResponses.getType("response");
            if (type != null && type.isVoid())
                return method.getReturnType();
            return type;
        }
    }

    public abstract String status();

    public abstract String description();

    public abstract List<ResponseHeaderScanner> responseHeaders();

    public abstract Type responseType();
}
