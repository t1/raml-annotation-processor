package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

import org.junit.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.Type;

public class SchemaGeneratorJsonTest {
    private static final String SCHEMA = "    \"$schema\":\"http://json-schema.org/schema#\",\n";

    private String jsonSchema(Class<?> type) {
        return SchemaGenerator.schema(Type.of(type), APPLICATION_JSON);
    }

    @Test
    public void shouldGenerateString() {
        String json = jsonSchema(String.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateEnum() {
        String json = jsonSchema(AccessMode.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"enum\":[\n" //
                + "        \"READ\",\n" //
                + "        \"WRITE\",\n" //
                + "        \"EXECUTE\"\n" //
                + "    ]\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateInteger() {
        String json = jsonSchema(Integer.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"integer\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateDouble() {
        String json = jsonSchema(Double.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"number\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateBoolean() {
        String json = jsonSchema(boolean.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"boolean\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateStringArray() {
        String json = jsonSchema(String[].class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"array\",\n" //
                + "    \"items\":{\n" //
                + "        \"type\":\"string\"\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateIntegerArray() {
        String json = jsonSchema(Integer[].class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"array\",\n" //
                + "    \"items\":{\n" //
                + "        \"type\":\"integer\"\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class Pojo {
        String value;
        Long integer;
        boolean bool;
    }

    @Test
    public void shouldGeneratePojo() {
        String json = jsonSchema(Pojo.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$Pojo\",\n" //
                + "    \"properties\":{\n" //
                + "        \"value\":{\n" //
                + "            \"type\":\"string\"\n" //
                + "        },\n" //
                + "        \"integer\":{\n" //
                + "            \"type\":\"integer\"\n" //
                + "        },\n" //
                + "        \"bool\":{\n" //
                + "            \"type\":\"boolean\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @JavaDoc("type-desc")
    static class PojoWithJavaDoc {
        /** s-doc */
        String s;
        /** i-doc */
        Long i;
        /** b-doc */
        boolean b;
    }

    @Test
    public void shouldGeneratePojoWithJavaDoc() {
        String json = jsonSchema(PojoWithJavaDoc.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithJavaDoc\",\n" //
        // + " \"description\":\"type-doc\",\n" //
                + "    \"properties\":{\n" //
                + "        \"s\":{\n" //
        // + " \"description\":\"s-doc\"\n" //
                + "            \"type\":\"string\"\n" //
                + "        },\n" //
                + "        \"i\":{\n" //
        // + " \"description\":\"i-doc\"\n" //
                + "            \"type\":\"integer\"\n" //
                + "        },\n" //
                + "        \"b\":{\n" //
        // + " \"description\":\"b-doc\"\n" //
                + "            \"type\":\"boolean\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class PojoWithList {
        Pojo pojo;
        List<String> list;
    }

    @Test
    public void shouldGeneratePojoWithList() {
        String json = jsonSchema(PojoWithList.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithList\",\n" //
                + "    \"properties\":{\n" //
                + "        \"pojo\":{\n" //
                + "            \"type\":\"object\",\n" //
                + "            \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$Pojo\",\n" //
                + "            \"properties\":{\n" //
                + "                \"value\":{\n" //
                + "                    \"type\":\"string\"\n" //
                + "                },\n" //
                + "                \"integer\":{\n" //
                + "                    \"type\":\"integer\"\n" //
                + "                },\n" //
                + "                \"bool\":{\n" //
                + "                    \"type\":\"boolean\"\n" //
                + "                }\n" //
                + "            }\n" //
                + "        },\n" //
                + "        \"list\":{\n" //
                + "            \"type\":\"array\",\n" //
                + "            \"items\":{\n" //
                + "                \"type\":\"string\"\n" //
                + "            }\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class PojoWithSet {
        Set<Pojo> set;
    }

    @Test
    public void shouldGeneratePojoWithSet() {
        String json = jsonSchema(PojoWithSet.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithSet\",\n" //
                + "    \"properties\":{\n" //
                + "        \"set\":{\n" //
                + "            \"type\":\"array\",\n" //
                + "            \"items\":{\n" //
                + "                \"type\":\"object\",\n" //
                + "                \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$Pojo\",\n" //
                + "                \"properties\":{\n" //
                + "                    \"value\":{\n" //
                + "                        \"type\":\"string\"\n" //
                + "                    },\n" //
                + "                    \"integer\":{\n" //
                + "                        \"type\":\"integer\"\n" //
                + "                    },\n" //
                + "                    \"bool\":{\n" //
                + "                        \"type\":\"boolean\"\n" //
                + "                    }\n" //
                + "                }\n" //
                + "            }\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @org.codehaus.jackson.map.annotate.JsonSerialize(using = org.codehaus.jackson.map.ser.std.ToStringSerializer.class)
    static class CodehausToStringPojo {
        long value;
    }

    @Test
    public void shouldGenerateCodehausToStringPojo() {
        String json = jsonSchema(CodehausToStringPojo.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$CodehausToStringPojo\"\n" //
                + "}\n", json);
    }

    @com.fasterxml.jackson.databind.annotation.JsonSerialize(
            using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    static class FasterXmlToStringPojo {
        long value;
    }

    @Test
    public void shouldGenerateFasterXmlToStringPojo() {
        String json = jsonSchema(FasterXmlToStringPojo.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$FasterXmlToStringPojo\"\n" //
                + "}\n", json);
    }

    static class RecursivePojo {
        RecursivePojo pojo;
    }

    @Test
    @Ignore("requires $ref from json-schema draft-4")
    public void shouldGenerateRecursivePojo() {
        String json = jsonSchema(RecursivePojo.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$RecursivePojo\",\n" //
                + "    \"properties\":{\n" //
                + "        \"pojo\":{\n" //
                + "            \"type\":\"object\",\n" //
                + "            \"properties\":{\n" //
                + "                \"value\":{\n" //
                + "                    \"type\":\"string\"\n" //
                + "                }\n" //
                + "            }\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateSchemaForClassWithToStringAndFromString() {
        String json = jsonSchema(UUID.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:java:util:UUID\"\n" //
                + "}\n", json);
    }

    static class PojoWithFromString {
        public static PojoWithFromString fromString(String value) {
            PojoWithFromString pojo = new PojoWithFromString();
            pojo.value = value;
            return pojo;
        }

        String value;
    }

    @Test
    public void shouldGeneratePojoSchemaForClassWithFromStringButNoToString() {
        String json = jsonSchema(PojoWithFromString.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithFromString\",\n" //
                + "    \"properties\":{\n" //
                + "        \"value\":{\n" //
                + "            \"type\":\"string\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class SuperPojoWithToString {
        @Override
        public String toString() {
            return "x";
        }
    }

    static class PojoWithInheritedToString extends SuperPojoWithToString {
        public static PojoWithInheritedToString fromString(String value) {
            PojoWithInheritedToString pojo = new PojoWithInheritedToString();
            pojo.value = value;
            return pojo;
        }

        String value;
    }

    @Test
    public void shouldGenerateSchemaForClassWithFromStringAndInheritedToString() {
        String json = jsonSchema(PojoWithInheritedToString.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithInheritedToString\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateSchemaForPath() {
        String json = jsonSchema(Path.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:java:nio:file:Path\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateSchemaForUri() {
        String json = jsonSchema(URI.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:java:net:URI\"\n" //
                + "}\n", json);
    }

    // TODO Map
}
