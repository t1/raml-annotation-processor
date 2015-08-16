package com.github.t1.ramlap;

import java.util.Iterator;

import org.raml.model.*;

// Immutable
public class ResourcePath implements Iterable<ResourcePath> {
    public static ResourcePath of(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        ResourcePath resourcePath = null;
        for (String item : path.split("/"))
            resourcePath = new ResourcePath(resourcePath, "/" + item);
        return resourcePath;
    }

    private final ResourcePath parent;
    private final String name;

    public ResourcePath(ResourcePath parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public ResourcePath and(String name) {
        if (!name.startsWith("/"))
            name = "/" + name;
        ResourcePath parent = (isEmpty()) ? this.parent : this;
        return new ResourcePath(parent, name);
    }

    public boolean isEmpty() {
        return this.name.isEmpty() || this.name.equals("/");
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        int i = 1;
        for (ResourcePath resourcePath = this; resourcePath.parent != null; resourcePath = resourcePath.parent)
            i++;
        return i;
    }

    @Override
    public Iterator<ResourcePath> iterator() {
        return new Iterator<ResourcePath>() {
            private int index = getLength();

            @Override
            public boolean hasNext() {
                return index > 0;
            }

            @Override
            public ResourcePath next() {
                --index;
                ResourcePath resourcePath = ResourcePath.this;
                for (int i = 0; i < index; i++)
                    resourcePath = resourcePath.parent;
                return resourcePath;
            }
        };
    }

    public void setResource(Raml raml, Resource resource) {
        resource.setParentResource(parentResource(raml));
        resource.setRelativeUri(getName());
        resource.setParentUri((parent == null) ? null : parent.toString());
        if (parent == null)
            raml.getResources().put(this.getName(), resource);
        else
            raml.getResource(parent.toString()).getResources().put(this.getName(), resource);
    }

    private Resource parentResource(Raml raml) {
        if (parent == null)
            return null;
        Resource parentResource = raml.getResource(parent.toString());
        if (parentResource == null) {
            parentResource = new Resource();
            parent.setResource(raml, parentResource);
        }
        return parentResource;
    }

    @Override
    public String toString() {
        if (parent == null)
            return name;
        return parent + name;
    }
}
