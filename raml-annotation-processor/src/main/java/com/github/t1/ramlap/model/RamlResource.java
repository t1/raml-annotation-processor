package com.github.t1.ramlap.model;

import java.util.Map;

import lombok.*;

@Getter
@Setter
public class RamlResource {
    private String uri;
    private String displayName;
    private String description;
    private Map<ActionType, RamlAction> actions;
    private Map<ResourcePath2, RamlResource> resources;
    private Map<String, UriParameter> uriParameters;
}
