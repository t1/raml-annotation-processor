package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.github.t1.exap.reflection.Type;

public class SchemaGeneratorXmlTest {
    @Test
    public void shouldGeneratePojo() {
        String xml = SchemaGenerator.schema(Type.of(Pojo.class), APPLICATION_XML);

        assertEquals("<?xml version=\"1.0\" standalone=\"yes\"?>\n" //
                + "<xs:schema version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" //
                + "\n" //
                + "  <xs:complexType name=\"pojo\">\n" //
                + "    <xs:sequence>\n" //
                + "      <xs:element name=\"value\" type=\"xs:string\" minOccurs=\"0\"/>\n" //
                + "    </xs:sequence>\n" //
                + "  </xs:complexType>\n" //
                + "</xs:schema>\n" //
                , xml);
    }
}
