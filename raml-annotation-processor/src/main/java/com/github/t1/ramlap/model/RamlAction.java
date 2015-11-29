package com.github.t1.ramlap.model;

import lombok.*;

@Getter
@Setter
public class RamlAction {
    private RamlResource resource;

    private String displayName;
    private String description;
    private ActionType type;
}
