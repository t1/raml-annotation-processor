package com.github.t1.ramlap;

import static com.github.t1.ramlap.StringTools.*;

import java.util.regex.*;

import org.raml.model.*;
import org.raml.model.parameter.UriParameter;
import org.slf4j.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.*;
import io.swagger.annotations.SwaggerDefinition.Scheme;

public class RamlScanner {
    private static final Pattern VARS = Pattern.compile("\\{(.*?)\\}");
    private static final Logger log = LoggerFactory.getLogger(RamlScanner.class);

    private final Raml raml = new XRaml();

    public void scan(SwaggerDefinition swaggerDefinition) {
        // TODO raml.setHost(nonEmpty(swaggerDefinition.host()));
        String basePath = swaggerDefinition.basePath();
        if (!basePath.isEmpty()) {
            raml.setBaseUri(basePath);
            scanBasePathParams(basePath);
        }
        scan(swaggerDefinition.schemes());

        scan(swaggerDefinition.info());
        scan(swaggerDefinition.tags());
        scanConsumes(swaggerDefinition.consumes());
        scanProduces(swaggerDefinition.produces());
    }

    private void scanBasePathParams(String basePath) {
        Matcher matcher = VARS.matcher(basePath);
        while (matcher.find()) {
            String name = matcher.group(1);
            raml.getBaseUriParameters().put(name, new UriParameter(name));
        }
    }

    private void scan(Scheme[] schemes) {
        for (Scheme scheme : schemes)
            try {
                raml.getProtocols().add(Protocol.valueOf(scheme.name()));
            } catch (IllegalArgumentException e) {
                continue;
            }
    }

    private void scan(io.swagger.annotations.Info in) {
        raml.setTitle(in.title());
        raml.setVersion(in.version());
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

    public RamlScanner scanJaxRsType(Type type) {
        log.debug("scan type {}", type);

        scanBasic(type);

        type.accept(new TypeScanner() {
            @Override
            public void visit(Method method) {
                new MethodScanner(raml, method).scan();
            }
        });

        type.note("processed");
        return this;
    }

    private void scanBasic(Type type) {
        Resource resource = resource(type);
        resource.setDisplayName(displayName(type));
        resource.setDescription(description(type));
    }

    private Resource resource(Type type) {
        ResourcePath path = ResourcePath.of(type);
        Resource resource = raml.getResource(path.toString());
        if (resource == null) {
            resource = new Resource();
            path.setResource(raml, resource);
        }
        return resource;
    }

    private String displayName(Type type) {
        JavaDoc javaDoc = type.getAnnotation(JavaDoc.class);
        if (javaDoc != null)
            return javaDoc.summary();
        return camelCaseToWords(type.getSimpleName());
    }

    private String description(Type type) {
        JavaDoc javaDoc = type.getAnnotation(JavaDoc.class);
        if (javaDoc != null)
            return javaDoc.value();
        return null;
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
