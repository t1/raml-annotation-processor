package com.github.t1.ramlap.model;

import java.util.Map;

import lombok.*;

@Getter
@Setter
public class Raml {
    private String title;
    private String version;

    private Map<ResourcePath2, RamlResource> resources;
    private Map<String, String> schemas;
}
