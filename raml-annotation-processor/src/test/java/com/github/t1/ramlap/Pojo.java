package com.github.t1.ramlap;

public class Pojo {
    public static final String POJO_JSON_SCHEMA = "{\n" //
            + "    \"type\":\"object\",\n" //
            + "    \"id\":\"urn:jsonschema:" + Pojo.class.getName().replace(".", ":") + "\",\n" //
            + "    \"properties\":{\n" //
            + "        \"value\":{\n" //
            + "            \"type\":\"string\"\n" //
            + "        }\n" //
            + "    }\n" //
            + "}\n";
    public static final String POJO_XML_SCHEMA = "<?xml version=\"1.0\" standalone=\"yes\"?>\n" //
            + "<xs:schema version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" //
            + "\n" //
            + "  <xs:complexType name=\"pojo\">\n" //
            + "    <xs:sequence>\n" //
            + "      <xs:element name=\"value\" type=\"xs:string\" minOccurs=\"0\"/>\n" //
            + "    </xs:sequence>\n" //
            + "  </xs:complexType>\n" //
            + "</xs:schema>\n" //
            ;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
