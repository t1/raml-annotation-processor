package com.github.t1.ramlap;

import static javax.ws.rs.core.Response.Status.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import javax.ws.rs.core.Response.StatusType;

import org.slf4j.*;

import com.github.t1.exap.reflection.*;

import io.swagger.annotations.ApiModelProperty;

public class ExampleGenerator {
    private static final Logger log = LoggerFactory.getLogger(ExampleGenerator.class);

    public static String example(Type type, String mediaType) {
        if (isMediaType(mediaType, "json"))
            return new JsonExampleGenerator().generate(type);
        // if (isMediaType(mediaType, "xml"))
        // return new XmlExampleGenerator().generate(type);
        return null;
    }

    private static boolean isMediaType(String mediaType, String extension) {
        return mediaType.equals("application/" + extension)
                || mediaType.startsWith("application/") && mediaType.endsWith("+" + extension);
    }

    private static class JsonExampleGenerator extends PropertiesToJsonGenerator {
        private abstract class ItemAdder {
            final Field field;

            ItemAdder(Field field) {
                this.field = field;
            }

            abstract void write();
        }

        private class ObjectItemAdder extends ItemAdder {

            public ObjectItemAdder(Field field) {
                super(field);
            }

            @Override
            public void write() {
                String exampleValue = exampleValue(field);
                try {
                    if (field.getType().isBoolean())
                        json.write(field.getName(), parseBoolean(exampleValue));
                    else if (field.getType().isInteger() || field.getType().isA(StatusType.class))
                        json.write(field.getName(), Integer.parseInt(exampleValue));
                    else if (field.getType().isFloating())
                        json.write(field.getName(), Double.parseDouble(exampleValue));
                    else
                        json.write(field.getName(), exampleValue);
                } catch (RuntimeException e) {
                    throw new RuntimeException("can't write example string", e);
                }
            }

            // Boolean.parseBoolean returns 'false' for anything not 'true'... we want an exception
            private boolean parseBoolean(String value) {
                if ("true".equals(value))
                    return true;
                if ("false".equals(value))
                    return false;
                throw new IllegalArgumentException("invalid boolean string: [" + value + "]");
            }
        }

        private class ArrayItemAdder extends ItemAdder {
            private Type type;

            public ArrayItemAdder(Type type, Field field) {
                super(field);
                this.type = type;
            }

            @Override
            public void write() {
                if (field == null)
                    json.write(generatedExample(type));
                else
                    json.write(exampleValue(field));
            }
        }

        private Stack<ItemAdder> fieldStack = new Stack<>();

        public JsonExampleGenerator() {
            super(log);
        }

        @Override
        protected void writeStart(Type type) {
            log.debug("generate example for {}", type.getFullName());
            if (type.isArray() || type.isA(Collection.class)) {
                json.writeStartArray();
            } else
                json.writeStartObject();
        }

        @Override
        protected void writeEnd() {
            json.writeEnd();
        }

        @Override
        protected void visitField(Field field) {
            this.fieldStack.push(new ObjectItemAdder(field));
            try {
                super.visitField(field);
            } finally {
                this.fieldStack.pop();
            }
        }

        @Override
        protected void visitScalar(Type type) {
            fieldStack.peek().write();
        }

        @Override
        protected void visitArray(Type type) {
            visitSequence(type, () -> super.visitArray(type));
        }

        @Override
        protected void visitCollection(Type type) {
            visitSequence(type, () -> super.visitCollection(type));
        }

        private void visitSequence(Type type, Runnable proceed) {
            String startName = fieldName();
            if (startName != null)
                json.writeStartArray(startName);
            this.fieldStack.push(new ArrayItemAdder(type, fieldStack.isEmpty() ? null : fieldStack.peek().field));
            try {
                proceed.run();
                if (startName != null)
                    json.writeEnd();
            } finally {
                this.fieldStack.pop();
            }
        }

        @Override
        protected void visitObject(Type type) {
            if (!fieldStack.isEmpty()) {
                Field field = fieldStack.peek().field;
                if (field == null)
                    json.writeStartObject();
                else
                    json.writeStartObject(field.getName());
                super.visitObject(type);
                json.writeEnd();
            } else {
                super.visitObject(type);
            }
        }

        private String fieldName() {
            if (fieldStack.isEmpty())
                return null;
            Field field = fieldStack.peek().field;
            return (field == null) ? null : field.getName();
        }

        private String exampleValue(Field field) {
            String example = null;
            if (field.isAnnotated(ApiExample.class))
                example = field.getAnnotation(ApiExample.class).value();
            else if (field.isAnnotated(ApiModelProperty.class))
                example = field.getAnnotation(ApiModelProperty.class).example();
            if (example == null || example.isEmpty())
                example = generatedExample(field.getType());
            return example;
        }

        private String generatedExample(Type type) {
            if (type.isA(Collection.class))
                return generatedExample(type.getTypeParameters().get(0));
            if (type.isArray())
                return generatedExample(type.elementType());
            if (type.isBoolean())
                return "false";
            if (type.isFloating())
                return "123.45";
            if (type.isInteger())
                return "12345";
            if (type.isString())
                return "foo";
            if (type.isA(StatusType.class))
                return Integer.toString(BAD_REQUEST.getStatusCode());
            if (type.isA(Path.class))
                return "/foo/bar";
            if (type.isA(URI.class))
                return "http://example.org/foo";
            if (type.isEnum())
                return type.getEnumValues().get(0);
            return "bar";
        }
    }
}
