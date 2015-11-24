package com.github.t1.ramlap.scanner;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.ApiModelProperty;

public class SchemaGenerator {
    private static final Logger log = LoggerFactory.getLogger(SchemaGenerator.class);

    /** we don't use the MediaType class, as that would require a dependency on e.g. the glassfish RI */
    public static String schema(Type type, String mediaType) {
        if (isMediaType(mediaType, "json"))
            return new JsonSchemaGenerator().generate(type);
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
    private static class JsonSchemaGenerator extends PropertiesToJsonGenerator {
        public JsonSchemaGenerator() {
            super(log);
        }

        @Override
        protected void writeStart(Type type) {
            log.debug("generate json schema for {}", type.getFullName());
            json.writeStartObject();
            json.write("$schema", "http://json-schema.org/schema#");
        }

        @Override
        protected void writeEnd() {
            json.writeEnd();
        }

        @Override
        protected void visit(Type type) {
            log.trace("write {}", type.getFullName());
            super.visit(type);
            log.trace("written {}", type.getFullName());
        }

        @Override
        protected void visitBoolean(Type type) {
            log.trace("write boolean");
            json.write("type", "boolean");
        }

        @Override
        protected void visitInteger(Type type) {
            log.trace("write integer");
            json.write("type", "integer");
            if (!type.isInteger())
                writeId(type);
        }

        @Override
        protected void visitFloating(Type type) {
            log.trace("write number");
            json.write("type", "number");
        }

        @Override
        protected void visitString(Type type) {
            log.trace("write string");
            json.write("type", "string");
            if (!type.isA(CharSequence.class))
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
            super.visitArray(type);
            json.writeEnd();
        }

        @Override
        protected void visitCollection(Type type) {
            log.trace("write collection");
            json.write("type", "array");
            json.writeStartObject("items");
            super.visitCollection(type);
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
            super.visitField(field);
            writeDescription(field);
            json.writeEnd();
        }

        private void writeId(Type type) {
            json.write("id", "urn:jsonschema:" + type.getFullName().replace('.', ':').replace('$', ':'));
        }

        private void writeDescription(Field field) {
            if (field.isAnnotated(JavaDoc.class))
                json.write("description", field.getAnnotation(JavaDoc.class).value());
            else if (field.isAnnotated(ApiModelProperty.class)) {
                String value = field.getAnnotation(ApiModelProperty.class).value();
                if (!value.isEmpty())
                    json.write("description", value);
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

