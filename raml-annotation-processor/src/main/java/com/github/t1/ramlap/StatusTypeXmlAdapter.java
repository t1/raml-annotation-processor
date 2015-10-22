package com.github.t1.ramlap;

import static com.github.t1.ramlap.ProblemDetailJsonDeserializer.*;

import javax.ws.rs.core.Response.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StatusTypeXmlAdapter extends XmlAdapter<String, StatusType> {
    @Override
    public StatusType unmarshal(String value) {
        return toStatus(value);
    }

    @Override
    public String marshal(StatusType value) {
        if (value instanceof Status)
            return ((Status) value).name();
        return value.toString();
    }
}
