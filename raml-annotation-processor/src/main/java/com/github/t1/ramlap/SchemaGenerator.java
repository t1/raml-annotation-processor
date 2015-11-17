package com.github.t1.ramlap;

import java.io.*;
import java.util.*;

import javax.json.Json;
import javax.json.stream.*;
import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.*;

import com.github.t1.exap.*;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.ApiModelProperty;

public class SchemaGenerator {
    private static final Logger log = LoggerFactory.getLogger(SchemaGenerator.class);

    /** we don't use the MediaType class, as that would require a dependency on e.g. the glassfish RI */
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
    private static class JsonSchemaGenerator extends PropertyVisitor {
        private final Type type;
        private JsonGenerator json;

        public JsonSchemaGenerator(Type type) {
            this.type = type;
        }

        public String generate() {
            log.debug("generate json schema for {}", type.getFullName());
            StringWriter out = new StringWriter();
            try (JsonGenerator json = createJsonGenerator(out)) {
                this.json = json;
                json.writeStartObject();
                json.write("$schema", "http://json-schema.org/schema#");
                visit(type);
                json.writeEnd();
            }
            return out.toString().trim() + "\n";
        }

        @SuppressWarnings("resource") // the caller closes
        private JsonGenerator createJsonGenerator(StringWriter out) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
            JsonGenerator generator = factory.createGenerator(out);
            if (log.isTraceEnabled())
                generator = LoggingJsonGenerator.of(log, generator);
            return generator;
        }

        @Override
        protected void visit(Type type) {
            log.trace("write {}", type.getFullName());
            super.visit(type);
            log.trace("written {}", type.getFullName());
        }

        @Override
        protected void visitBoolean() {
            log.trace("write boolean");
            json.write("type", "boolean");
        }

        @Override
        protected void visitInteger() {
            log.trace("write integer");
            json.write("type", "integer");
        }

        @Override
        protected void visitFloating() {
            log.trace("write number");
            json.write("type", "number");
        }

        @Override
        protected void visitString(Type type) {
            log.trace("write string");
            json.write("type", "string");
            if (!type.isString())
                writeId(type);
        }

        @Override
        protected void visitEnum(Type type) {
            log.trace("write enum");
            json.write("type", "string");
            json.writeStartArray("enum");
            for (String enumValue : type.getEnumValues())
                json.write(enumValue);
            json.writeEnd();
        }

        @Override
        protected void visitArray(Type type) {
            log.trace("write array");
            json.write("type", "array");
            json.writeStartObject("items");
            visit(type.elementType());
            json.writeEnd();
        }

        @Override
        protected void visitCollection(Type type) {
            log.trace("write collection");
            json.write("type", "array");
            json.writeStartObject("items");
            visit(type.getTypeParameters().get(0));
            json.writeEnd();
        }

        @Override
        protected void visitObject(Type type) {
            log.trace("write object");
            json.write("type", "object");
            writeId(type);
            if (type.isAnnotated(JavaDoc.class))
                json.write("description", type.getAnnotation(JavaDoc.class).value());
            json.writeStartObject("properties");
            super.visitObject(type);
            json.writeEnd();
        }

        @Override
        protected void visitField(Field field) {
            json.writeStartObject(field.getName());
            visit(field.getType());
            writeDescription(field);
            json.writeEnd();
        }

        private void writeId(Type type) {
            json.write("id", "urn:jsonschema:" + type.getFullName().replace('.', ':'));
        }

        private void writeDescription(Field field) {
            if (field.isAnnotated(JavaDoc.class))
                json.write("description", field.getAnnotation(JavaDoc.class).value());
            else if (field.isAnnotated(ApiModelProperty.class))
                json.write("description", field.getAnnotation(ApiModelProperty.class).value());
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
                Class<?> javaType = Class.forName(type.getFullName());
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

