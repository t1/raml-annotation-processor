package com.github.t1.ramlap;

import java.util.*;

import org.raml.model.Raml;
import org.slf4j.*;

import com.github.t1.exap.reflection.*;

import io.swagger.annotations.*;

public class RamlScanner {
    private static final Logger log = LoggerFactory.getLogger(RamlScanner.class);

    private final Raml raml = new Raml();

    public void addSwaggerDefinitions(List<Type> elements) {
        Type swaggerDefinition = firstSwaggerDefinition(elements);
        if (swaggerDefinition != null)
            addSwaggerDefinition(swaggerDefinition);
    }

    private Type firstSwaggerDefinition(List<Type> types) {
        Type result = null;
        for (Type type : types)
            if (type.isPublic())
                if (result == null)
                    result = type;
                else
                    type.error("conflicting @SwaggerDefinition found besides: " + result);
            else
                type.note("skipping non-public element");
        return result;
    }

    // visible for testing
    public RamlScanner addSwaggerDefinition(Type swaggerDefinitionElement) {
        SwaggerDefinition swaggerDefinition = swaggerDefinitionElement.getAnnotation(SwaggerDefinition.class);

        // TODO raml.setHost(nonEmpty(swaggerDefinition.host()));
        // TODO raml.setBasePath(nonEmpty(swaggerDefinition.basePath()));

        scan(swaggerDefinition.info());
        scan(swaggerDefinition.tags());
        scanConsumes(swaggerDefinition.consumes());
        scanProduces(swaggerDefinition.produces());

        swaggerDefinitionElement.note("processed");
        return this;
    }

    private void scan(io.swagger.annotations.Info in) {
        raml.setTitle(in.title());
        // TODO Info outInfo = new Info();
        // outInfo.title(in.title());
        // outInfo.version(in.version());
        // outInfo.description(nonEmpty(in.description()));
        // outInfo.termsOfService(nonEmpty(in.termsOfService()));
        //
        // raml.setInfo(outInfo);

        scanContact(in.contact());
        scanLicense(in.license());
        scanVendorExtensions(in.extensions());
    }

    private void scanContact(Contact in) {
        if (in.name().isEmpty())
            return;
        // TODO io.swagger.models.Contact outContact = new io.swagger.models.Contact();
        // outContact.setName(nonEmpty(in.name()));
        // outContact.setEmail(nonEmpty(in.email()));
        // outContact.setUrl(nonEmpty(in.url()));
        // raml.getInfo().setContact(outContact);
    }

    private void scanLicense(License in) {
        if (in.name().isEmpty())
            return;
        // TODO io.swagger.models.License outLicense = new io.swagger.models.License();
        // outLicense.setName(nonEmpty(in.name()));
        // outLicense.setUrl(nonEmpty(in.url()));
        // raml.getInfo().setLicense(outLicense);
    }

    private void scanVendorExtensions(Extension[] inExtensions) {
        if (inExtensions.length == 0 || inExtensions.length == 1 && inExtensions[0].name().isEmpty())
            return;
        // TODO @SuppressWarnings({ "unchecked", "rawtypes" })
        // Map<String, Map<String, String>> vendorExtensions = (Map) raml.getInfo().getVendorExtensions();
        // for (Extension inExtension : inExtensions) {
        // if (inExtension.name().isEmpty())
        // continue;
        // String name = name(inExtension.name());
        // if (!vendorExtensions.containsKey(name))
        // vendorExtensions.put(name, new HashMap<String, String>());
        // Map<String, String> extensionProperties = vendorExtensions.get(name);
        //
        // for (ExtensionProperty property : inExtension.properties()) {
        // String propertyName = name(property.name());
        // extensionProperties.put(propertyName, property.value());
        // }
        // }
    }

    private String name(String propertyName) {
        if (!propertyName.startsWith("x-"))
            propertyName = "x-" + propertyName;
        return propertyName;
    }

    private void scan(Tag[] tags) {
        // TODO for (Tag tag : tags)
        // if (!tag.name().isEmpty())
        // raml.tag(new io.swagger.models.Tag() //
        // .name(tag.name()) //
        // .description(tag.description()) //
        // );
    }

    private void scanConsumes(String[] mediaTypes) {
        // TODO for (String mediaType : mediaTypes)
        // if (!mediaType.isEmpty())
        // raml.consumes(mediaType);
    }

    private void scanProduces(String[] mediaTypes) {
        // TODO for (String mediaType : mediaTypes)
        // if (!mediaType.isEmpty())
        // raml.produces(mediaType);
    }

    public RamlScanner addJaxRsTypes(List<Type> types) {
        log.debug("addPathElements {}", types);
        for (Type type : types)
            addJaxRsType(type);
        return this;
    }

    private RamlScanner addJaxRsType(Type type) {
        Api api = type.getAnnotation(Api.class);
        final List<String> defaultTags = tags(api);
        final String typePath = type.getAnnotation(javax.ws.rs.Path.class).value();
        log.debug("scan path {} in {}", typePath, type);

        type.accept(new TypeScanner() {
            @Override
            public void visit(Method method) {
                // TODO new MethodScanner(method, raml, typePath, defaultTags).scan();
            }
        });

        type.note("processed");
        return this;
    }

    private List<String> tags(Api api) {
        if (api == null)
            return null;
        List<String> list = new ArrayList<>();
        for (String tag : api.tags())
            if (!tag.isEmpty())
                list.add(tag);
        if (list.isEmpty() && !api.value().isEmpty())
            list.add(api.value());
        return (list.isEmpty()) ? null : list;
    }

    public Raml getResult() {
        return raml;
    }

    public boolean isWorthWriting() {
        return true;
        // TODO return hasTitle() || hasPaths();
    }

    // private boolean hasTitle() {
    // return raml.getInfo() != null && raml.getInfo().getTitle() != null;
    // }
    //
    // private boolean hasPaths() {
    // return raml.getPaths() != null && !raml.getPaths().isEmpty();
    // }
}
