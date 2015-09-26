package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import java.nio.file.AccessMode;
import java.util.*;

import org.junit.*;

import com.github.t1.exap.reflection.Type;

public class SchemaGeneratorJsonTest {
    private static final String SCHEMA = "    \"$schema\":\"http://json-schema.org/schema#\",\n";

    private String json(Class<?> type) {
        return SchemaGenerator.schema(Type.of(type), APPLICATION_JSON);
    }

    @Test
    public void shouldGenerateString() {
        String json = json(String.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateEnum() {
        String json = json(AccessMode.class);

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
        String json = json(Integer.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"integer\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateDouble() {
        String json = json(Double.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"number\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateBoolean() {
        String json = json(boolean.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"boolean\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateStringArray() {
        String json = json(String[].class);

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
        String json = json(Integer[].class);

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
        String json = json(Pojo.class);

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

    static class PojoWithList {
        Pojo pojo;
        List<String> list;
    }

    @Test
    public void shouldGeneratePojoWithList() {
        String json = json(PojoWithList.class);

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
        String json = json(PojoWithSet.class);

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
        String json = json(CodehausToStringPojo.class);

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
        String json = json(FasterXmlToStringPojo.class);

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
        String json = json(RecursivePojo.class);

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
        String json = json(UUID.class);

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
        String json = json(PojoWithFromString.class);

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
        String json = json(PojoWithInheritedToString.class);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithInheritedToString\"\n" //
                + "}\n", json);
    }

    // TODO Map
    // TODO Path
}
