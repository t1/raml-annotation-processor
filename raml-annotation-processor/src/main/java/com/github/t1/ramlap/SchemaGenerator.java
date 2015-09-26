package com.github.t1.ramlap;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;

import javax.json.Json;
import javax.json.stream.*;
import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.github.t1.exap.reflection.*;

public class SchemaGenerator {
    public static String schema(Type type, String mediaType) {
        if (isMediaType(mediaType, "json"))
            return new JsonSchemaGenerator(type).generate();
        if (isMediaType(mediaType, "xml"))
            return new XmlSchemaGenerator(type).generate();
        return null;
    }

    private static boolean isMediaType(String mediaType, String extension) {
        return mediaType.equals("application/" + extension)
                || mediaType.startsWith("application/") && mediaType.endsWith("+" + extension);
    }

    /**
     * @see <a href="http://spacetelescope.github.io/understanding-json-schema/index.html">web</a>
     */
    private static class JsonSchemaGenerator {
        private final Type type;
        private JsonGenerator json;

        public JsonSchemaGenerator(Type type) {
            this.type = type;
        }

        public String generate() {
            StringWriter out = new StringWriter();
            try (JsonGenerator json = createJsonGenerator(out)) {
                this.json = json;
                json.writeStartObject();
                json.write("$schema", "http://json-schema.org/schema#");
                generate(type);
                json.writeEnd();
            }
            return out.toString().trim() + "\n";
        }

        private JsonGenerator createJsonGenerator(StringWriter out) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
            return factory.createGenerator(out);
        }

        public void generate(Type type) {
            try {
                if (type.isBoolean()) {
                    json.write("type", "boolean");
                } else if (type.isInteger()) {
                    json.write("type", "integer");
                } else if (type.isDecimal()) {
                    json.write("type", "number");
                } else if (type.isString()) {
                    json.write("type", "string");
                } else if (isJacksonToString(type)) {
                    json.write("type", "string");
                    writeId(type);
                } else if (type.isEnum()) {
                    json.write("type", "string");
                    json.writeStartArray("enum");
                    for (String enumValue : type.getEnumValues())
                        json.write(enumValue);
                    json.writeEnd();
                } else if (type.isArray()) {
                    json.write("type", "array");
                    json.writeStartObject("items");
                    generate(type.elementType());
                    json.writeEnd();
                } else if (type.isAssignableTo(Collection.class)) {
                    json.write("type", "array");
                    json.writeStartObject("items");
                    generate(type.getTypeParameters().get(0).getBounds().get(0));
                    json.writeEnd();
                } else {
                    json.write("type", "object");
                    writeId(type);
                    json.writeStartObject("properties");
                    for (Field field : type.getFields()) {
                        if (field.isStatic() || field.isTransient())
                            continue;
                        json.writeStartObject(field.getName());
                        generate(field.getType());
                        json.writeEnd();
                    }
                    json.writeEnd();
                }
            } catch (RuntimeException e) {
                throw new RuntimeException("while generating json schema for " + type, e);
            } catch (Error e) {
                throw new Error("while generating json schema for " + type, e);
            }
        }

        private boolean isJacksonToString(Type type) {
            return isUsing(type, org.codehaus.jackson.map.annotate.JsonSerialize.class,
                    org.codehaus.jackson.map.ser.std.ToStringSerializer.class)
                    || isUsing(type, com.fasterxml.jackson.databind.annotation.JsonSerialize.class,
                            com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class);
        }

        private boolean isUsing(Type type, Class<? extends Annotation> annotation, Class<?> serializer) {
            return type.isAnnotated(annotation)
                    && serializer.getName().equals(type.getAnnotationClassAttribute(annotation, "using"));
        }

        private void writeId(Type type) {
            json.write("id", "urn:jsonschema:" + type.getQualifiedName().replace('.', ':'));
        }
    }

    private static class XmlSchemaGenerator {
        private final Type type;

        public XmlSchemaGenerator(Type type) {
            this.type = type;
        }

        public String generate() {
            final StringWriter string = new StringWriter();

            try {
                Class<?> javaType = Class.forName(type.getQualifiedName());
                JAXBContext context = JAXBContext.newInstance(javaType);
                context.generateSchema(new SchemaOutputResolver() {
                    @Override
                    public Result createOutput(String namespaceUri, String suggestedFileName) {
                        StreamResult result = new StreamResult(string);
                        result.setSystemId("dummy");
                        return result;
                    }
                });
            } catch (ClassNotFoundException | JAXBException | IOException e) {
                throw new RuntimeException(e);
            }
            return (string.toString().isEmpty()) ? null : (string.toString().trim() + "\n");
        }
    }
}

