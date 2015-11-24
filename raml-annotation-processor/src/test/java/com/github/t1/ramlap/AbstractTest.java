package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.BddAssertions.*;

import java.util.*;

import javax.tools.Diagnostic;

import org.junit.*;
import org.raml.model.*;
import org.raml.model.Resource;

import com.github.t1.exap.reflection.*;
import com.github.t1.ramlap.scanner.RamlScanner;

import io.swagger.annotations.SwaggerDefinition;

public abstract class AbstractTest {
    @Rule
    public final JUnitSoftAssertions then = new JUnitSoftAssertions();

    protected Raml scanTypes(Class<?>... containers) {
        RamlScanner scanner = new RamlScanner();
        for (Class<?> container : containers) {
            Type type = ENV.type(container);
            if (type.isAnnotated(SwaggerDefinition.class))
                scanner.scan(type.getAnnotation(SwaggerDefinition.class));
            scanner.scanJaxRsType(type);
        }
        return scanner.getResult();
    }

    protected Action action(Raml raml, String path, ActionType type) {
        Resource resource = raml.getResource(path);
        then(resource).as("resource " + path).isNotNull();
        Action action = resource.getAction(type);
        then(action).as("action " + type).isNotNull();
        return action;
    }

    public static <T> Set<T> asSet(@SuppressWarnings("unchecked") T... items) {
        return new LinkedHashSet<>(asList(items));
    }

    private int expectedMessageCount = 0;

    protected void assertMessage(Diagnostic.Kind kind, Elemental target, String message) {
        assertThat(ENV.getMessages(target, kind)).contains(message);
        expectedMessageCount++;
    }

    @After
    public void assertNoUnexpectedMessages() {
        List<Message> messages = ENV.getMessages();
        try {
            assertThat(messages).hasSize(expectedMessageCount);
        } finally {
            messages.clear();
        }
    }
}
