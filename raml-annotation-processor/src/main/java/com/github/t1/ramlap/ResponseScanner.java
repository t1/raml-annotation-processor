package com.github.t1.ramlap;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import com.github.t1.exap.reflection.*;

import io.swagger.annotations.*;

public abstract class ResponseScanner {
    public static List<ResponseScanner> responses(Method method) {
        if (method.isAnnotated(ApiResponses.class))
            return apiResponses(method);
        if (method.isAnnotated(ApiResponse.class))
            return asList(new ApiResponseScanner(method, method.getAnnotation(ApiResponse.class)));
        return asList(new DefaultResponseScanner(method));
    }

    private static List<ResponseScanner> apiResponses(Method method) {
        List<ResponseScanner> list = new ArrayList<>();
        for (ApiResponse apiResponse : method.getAnnotation(ApiResponses.class).value()) {
            list.add(new ApiResponseScanner(method, apiResponse));
        }
        return list;
    }

    public static class ResponseHeaderScanner {
        private final ResponseHeader header;

        public ResponseHeaderScanner(ResponseHeader header) {
            this.header = header;
        }

        public String name() {
            return header.name();
        }

        public String description() {
            return header.description();
        }

        public Class<?> response() {
            return header.response();
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
        private final ApiResponse apiResponse;

        public ApiResponseScanner(Method method, ApiResponse apiResponse) {
            this.method = method;
            this.apiResponse = apiResponse;
        }

        @Override
        public String status() {
            return Integer.toString(apiResponse.code());
        }

        @Override
        public String description() {
            return apiResponse.message();
        }

        @Override
        public List<ResponseHeaderScanner> responseHeaders() {
            List<ResponseHeaderScanner> list = new ArrayList<>();
            for (ResponseHeader header : apiResponse.responseHeaders()) {
                list.add(new ResponseHeaderScanner(header));
            }
            return list;
        }

        @Override
        public Type responseType() {
            if (apiResponse.response() == Void.class)
                return method.getReturnType();
            return new ReflectionType(null, apiResponse.response());
        }
    }

    public abstract String status();

    public abstract String description();

    public abstract List<ResponseHeaderScanner> responseHeaders();

    public abstract Type responseType();
}
