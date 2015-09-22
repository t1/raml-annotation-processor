package com.github.t1.ramlap;

import java.io.*;
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

    private static class JsonSchemaGenerator {
        private final Type type;
        private JsonGenerator json;
        private int depth;

        public JsonSchemaGenerator(Type type) {
            this.type = type;
        }

        public String generate() {
            StringWriter out = new StringWriter();
            try (JsonGenerator json = createJsonGenerator(out)) {
                this.json = json;
                json.writeStartObject();
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
            if (type.isBoolean()) {
                json.write("type", "boolean");
            } else if (type.isInteger()) {
                json.write("type", "integer");
            } else if (type.isDecimal()) {
                json.write("type", "number");
            } else if (type.isString()) {
                json.write("type", "string");
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
            } else if (type.isSubclassOf(List.class)) {
                json.write("type", "array");
                json.writeStartObject("items");
                generate(type.getTypeParameters().get(0).getBounds().get(0));
                json.writeEnd();
            } else {
                json.write("type", "object");
                if (depth == 0)
                    json.write("id", "urn:jsonschema:" + type.getQualifiedName().replace('.', ':'));
                json.writeStartObject("properties");
                for (Field field : type.getFields()) {
                    json.writeStartObject(field.getName());
                    depth++;
                    generate(field.getType());
                    depth--;
                    json.writeEnd();
                }
                json.writeEnd();
            }
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

