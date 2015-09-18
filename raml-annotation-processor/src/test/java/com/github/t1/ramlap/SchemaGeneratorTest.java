package com.github.t1.ramlap;

import static org.junit.Assert.*;

import org.junit.Test;

public class SchemaGeneratorTest {
    public static class Pojo {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private final SchemaGenerator generator = new SchemaGenerator(Pojo.class.getName());

    @Test
    public void shouldGenerateJsonSchema() {
        String json = generator.jsonSchema();

        assertEquals("" //
                + "{\n" //
                + "  \"type\" : \"object\",\n" //
                + "  \"id\" : \"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorTest:Pojo\",\n" //
                + "  \"properties\" : {\n" //
                + "    \"value\" : {\n" //
                + "      \"type\" : \"string\"\n" //
                + "    }\n" //
                + "  }\n" //
                + "}", json);
    }

    @Test
    public void shouldGenerateXmlSchema() {
        String xml = generator.xmlSchema();

        assertEquals("<?xml version=\"1.0\" standalone=\"yes\"?>\n" //
                + "<xs:schema version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" //
                + "\n" //
                + "  <xs:complexType name=\"pojo\">\n" //
                + "    <xs:sequence>\n" //
                + "      <xs:element name=\"value\" type=\"xs:string\" minOccurs=\"0\"/>\n" //
                + "    </xs:sequence>\n" //
                + "  </xs:complexType>\n" //
                + "</xs:schema>\n" //
                + "\n", xml);
    }
}
