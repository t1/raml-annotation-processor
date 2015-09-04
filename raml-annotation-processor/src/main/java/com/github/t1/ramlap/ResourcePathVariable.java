package com.github.t1.ramlap;

public class ResourcePathVariable {
    private final ResourcePath resourcePath;
    private final String name;
    private final String pattern;

    public ResourcePathVariable(ResourcePath resourcePath, String name, String pattern) {
        this.resourcePath = resourcePath;
        this.name = name;
        this.pattern = pattern;
    }

    public ResourcePath getResourcePath() {
        return resourcePath;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourcePathVariable other = (ResourcePathVariable) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (pattern == null) {
            if (other.pattern != null) {
                return false;
            }
        } else if (!pattern.equals(other.pattern)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResourcePathVariable [" + name + ":" + pattern + "]";
    }
}