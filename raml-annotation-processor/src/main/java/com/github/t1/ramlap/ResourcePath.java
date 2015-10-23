package com.github.t1.ramlap;

import java.util.*;
import java.util.regex.*;

import javax.ws.rs.Path;

import org.raml.model.*;

import com.github.t1.exap.reflection.Type;

// Immutable
// maybe not optimal implementation but okay given that paths are < 10 items in practice
public class ResourcePath implements Iterable<ResourcePath> {
    private static final Pattern VARS = Pattern.compile("\\{(?<path>.*?)(:(?<regex>.*?))?\\}");

    public static ResourcePath of(Type type) {
        return (type.isAnnotated(Path.class)) ? ResourcePath.of(type.getAnnotation(Path.class).value()) : null;
    }

    public static ResourcePath of(String path) {
        return build(path, null);
    }

    private static ResourcePath build(String path, ResourcePath resourcePath) {
        if (path.startsWith("/"))
            path = path.substring(1);
        for (String item : path.split("/"))
            resourcePath = new ResourcePath(resourcePath, "/" + item);
        return resourcePath;
    }

    private final ResourcePath parent;
    private final String name;

    private ResourcePath(ResourcePath parent, String name) {
        this.parent = parent;
        this.name = Objects.requireNonNull(name);
    }

    public ResourcePath and(String name) {
        if (!name.startsWith("/"))
            name = "/" + name;
        ResourcePath parent = (isEmpty()) ? this.parent : this;
        return build(name, parent);
    }

    public boolean isEmpty() {
        return this.name.isEmpty() || this.name.equals("/");
    }

    /**
     * The path element of this ResourcePath with the regex part of all variables (the colon and what comes thereafter)
     * stripped off, e.g. a path element <code>foo-{n:[0-9]+}</code> has the simple name <code>foo-{n}</code>.
     */
    public String getSimpleName() {
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARS.matcher(name);
        while (matcher.find())
            matcher.appendReplacement(result, "{" + matcher.group("path") + "}");
        matcher.appendTail(result);
        return result.toString();
    }

    public int getLength() {
        if (parent == null)
            return 1;
        return 1 + parent.getLength();
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
        if (parent == null) {
            resource.setParentResource(null);
            resource.setRelativeUri(getSimpleName());
            resource.setParentUri("");
            raml.getResources().put(this.getSimpleName(), resource);
        } else {
            Resource parentResource = parentResource(raml);
            resource.setParentResource(parentResource);
            resource.setRelativeUri(getSimpleName());
            resource.setParentUri(parentResource.getUri());
            parentResource.getResources().put(this.getSimpleName(), resource);
        }
    }

    private Resource parentResource(Raml raml) {
        Resource parentResource = raml.getResource(parent.toString());
        if (parentResource == null) {
            parentResource = new Resource();
            parent.setResource(raml, parentResource);
        }
        return parentResource;
    }

    public Optional<ResourcePathVariable> var(String name) {
        for (ResourcePath item : this) {
            Matcher matcher = VARS.matcher(item.name);
            while (matcher.find())
                if (name.equals(matcher.group("path")))
                    return Optional.of(new ResourcePathVariable(item, matcher.group("path"), matcher.group("regex")));
        }
        return Optional.empty();
    }

    public List<ResourcePathVariable> vars() {
        List<ResourcePathVariable> result = new ArrayList<>();
        Matcher matcher = VARS.matcher(toString());
        while (matcher.find())
            result.add(new ResourcePathVariable(null, matcher.group("path"), matcher.group("regex")));
        return result;
    }

    public List<String> items() {
        List<String> list = new ArrayList<>();
        for (ResourcePath item : this)
            list.add(item.getSimpleName());
        return list;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + ((parent == null) ? 0 : 31 * parent.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourcePath other = (ResourcePath) obj;
        if (!name.equals(other.name))
            return false;
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (parent == null) ? name : parent + name;
    }
}
