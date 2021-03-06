package com.github.t1.ramlap.tools;

import org.junit.*;
import org.raml.model.*;

import static org.assertj.core.api.Assertions.*;

public class ResourcePathTest {
    @Rule
    public JUnitSoftAssertions then = new JUnitSoftAssertions();

    @Test
    public void shouldBuildEmpty() {
        ResourcePath path = ResourcePath.of("");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/")
                .hasToString("/")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/");
        assertThat(path.items()).containsExactly("/");
    }

    @Test
    public void shouldBuildSlashOnly() {
        ResourcePath path = ResourcePath.of("/");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/")
                .hasToString("/")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/");
        assertThat(path.items()).containsExactly("/");
    }

    @Test
    public void shouldBuildOneItemWithoutSlash() {
        ResourcePath path = ResourcePath.of("foo");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/foo")
                .hasToString("/foo")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildOneItemWithSlash() {
        ResourcePath path = ResourcePath.of("/foo");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/foo")
                .hasToString("/foo")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildOneItemWhenConcatenatingFromEmpty() {
        ResourcePath path = ResourcePath.of("").and("/foo");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/foo")
                .hasToString("/foo")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildOneItemWhenConcatenatingFromSlash() {
        ResourcePath path = ResourcePath.of("/").and("/foo");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/foo")
                .hasToString("/foo")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildTwoItems() {
        ResourcePath path = ResourcePath.of("/foo/bar");

        ResourcePathAssert.assertThat(path)
                .hasLength(2)
                .hasSimpleName("/bar")
                .hasToString("/foo/bar")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar");
        assertThat(path.items()).containsExactly("/foo", "/bar");
    }

    @Test
    public void shouldBuildWithAnd() {
        ResourcePath path = ResourcePath.of("/foo").and("/bar");

        ResourcePathAssert.assertThat(path)
                .hasLength(2)
                .hasSimpleName("/bar")
                .hasToString("/foo/bar")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar");
        assertThat(path.items()).containsExactly("/foo", "/bar");
    }

    @Test
    public void shouldBuildWithTwoAnd() {
        ResourcePath path = ResourcePath.of("/foo").and("/bar").and("/baz");

        ResourcePathAssert.assertThat(path)
                .hasLength(3)
                .hasSimpleName("/baz")
                .hasToString("/foo/bar/baz")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldBuildWithAndContainingSlash() {
        ResourcePath path = ResourcePath.of("/foo").and("/bar/baz");

        ResourcePathAssert.assertThat(path)
                .hasLength(3)
                .hasSimpleName("/baz")
                .hasToString("/foo/bar/baz")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldBuildFromSlashWithAndContainingSlash() {
        ResourcePath path = ResourcePath.of("/").and("/foo/bar/baz");

        ResourcePathAssert.assertThat(path)
                .hasLength(3)
                .hasSimpleName("/baz")
                .hasToString("/foo/bar/baz")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldBuildThreeItems() {
        ResourcePath path = ResourcePath.of("/foo/bar/baz");

        ResourcePathAssert.assertThat(path)
                .hasLength(3)
                .hasSimpleName("/baz")
                .hasToString("/foo/bar/baz")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldSetRamlForEmptyRootResource() {
        ResourcePath path = ResourcePath.of("/");
        Raml raml = new Raml();

        Resource resource = path.resource(raml);

        assertThat(raml.getResources()).containsOnlyKeys("/");
        assertThat(resource.getResources()).isEmpty();
        then.assertThat(resource)
                .hasUri("/")
                .hasRelativeUri("/")
                .hasParentResource(null)
                .hasParentUri("")
                .isSameAs(raml.getResource("/"))
                ;
    }

    @Test
    public void shouldCreateNewRootResourceWhenSettingSubResource() {
        Raml raml = new Raml();
        ResourcePath path = ResourcePath.of("/foo/bar");

        Resource bar = path.resource(raml);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        Resource foo = raml.getResource("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        assertThat(bar.getResources()).isEmpty();
        then.assertThat(foo)
                .as("newly created root resource")
                .isNotSameAs(bar)
                .hasUri("/foo")
                .hasRelativeUri("/foo")
                .hasParentUri("")
                .hasParentResource(null)
                ;
        then.assertThat(bar)
                .hasUri("/foo/bar")
                .hasRelativeUri("/bar")
                .hasParentUri("/foo")
                .hasParentResource(foo)
                .isSameAs(raml.getResource("/foo/bar"))
                ;
    }

    @Test
    public void shouldReuseRootResourceWhenSettingSubResource() {
        Raml raml = new Raml();
        ResourcePath path = ResourcePath.of("/foo");
        Resource foo = path.resource(raml);

        Resource bar = path.and("/bar").resource(raml);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        assertThat(bar.getResources()).isEmpty();
        then.assertThat(bar)
                .hasUri("/foo/bar")
                .hasRelativeUri("/bar")
                .hasParentUri("/foo")
                .hasParentResource(foo)
                .isSameAs(raml.getResource("/foo/bar"))
                ;
        then.assertThat(raml.getResource("/foo"))
                .isSameAs(foo)
                .isNotSameAs(bar)
                ;
    }

    @Test
    public void shouldCreateNewParentResourceWhenSettingSubSubResource() {
        Raml raml = new Raml();
        ResourcePath path = ResourcePath.of("/foo/bar/baz");

        Resource baz = path.resource(raml);

        Resource foo = raml.getResource("/foo");
        Resource bar = raml.getResource("/foo/bar");
        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        assertThat(bar.getResources()).containsOnlyKeys("/baz");
        assertThat(baz.getResources()).isEmpty();
        then.assertThat(foo)
                .as("newly created root resource")
                .isNotSameAs(bar)
                .isNotSameAs(baz)
                .hasUri("/foo")
                .hasRelativeUri("/foo")
                .hasParentUri("")
                .hasParentResource(null)
                ;
        then.assertThat(bar)
                .as("newly created intermediate resource")
                .isNotSameAs(foo)
                .isNotSameAs(baz)
                .hasUri("/foo/bar")
                .hasRelativeUri("/bar")
                .hasParentUri("/foo")
                .hasParentResource(foo)
                ;
        then.assertThat(baz)
                .isNotSameAs(foo)
                .isNotSameAs(bar)
                .hasUri("/foo/bar/baz")
                .hasRelativeUri("/baz")
                .hasParentUri("/foo/bar")
                .hasParentResource(bar)
                .isSameAs(raml.getResource("/foo/bar/baz"))
                ;
    }

    @Test
    public void shouldReuseSubResourceWhenSettingSubSubResource() {
        Raml raml = new Raml();

        ResourcePath.of("/foo/bar/baz").resource(raml);
        ResourcePath.of("/foo/bar/bib").resource(raml);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        Resource foo = raml.getResource("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        Resource bar = raml.getResource("/foo/bar");
        assertThat(bar.getResources()).containsOnlyKeys("/baz", "/bib");
        Resource baz = raml.getResource("/foo/bar/baz");
        assertThat(baz.getResources()).isEmpty();
        Resource bib = raml.getResource("/foo/bar/bib");
        assertThat(bib.getResources()).isEmpty();

        then.assertThat(foo)
                .as("newly created root resource")
                .isNotSameAs(bar)
                .isNotSameAs(baz)
                .isNotSameAs(bib)
                .hasUri("/foo")
                .hasRelativeUri("/foo")
                .hasParentUri("")
                .hasParentResource(null)
                ;
        then.assertThat(bar)
                .as("newly created intermediate resource")
                .isNotSameAs(foo)
                .isNotSameAs(baz)
                .isNotSameAs(bib)
                .hasUri("/foo/bar")
                .hasRelativeUri("/bar")
                .hasParentUri("/foo")
                .hasParentResource(foo)
                ;
        then.assertThat(baz)
                .isNotSameAs(foo)
                .isNotSameAs(bar)
                .isNotSameAs(bib)
                .hasUri("/foo/bar/baz")
                .hasRelativeUri("/baz")
                .hasParentUri("/foo/bar")
                .hasParentResource(bar)
                ;
        then.assertThat(bib)
                .isNotSameAs(foo)
                .isNotSameAs(bar)
                .isNotSameAs(baz)
                .hasUri("/foo/bar/bib")
                .hasRelativeUri("/bib")
                .hasParentUri("/foo/bar")
                .hasParentResource(bar)
                ;
    }

    @Test
    public void shouldAddParentResourceWithPattern() {
        Raml raml = new Raml();

        ResourcePath.of("/{foo:a}/{bar}").resource(raml);

        Resource foo = raml.getResource("/{foo}");
        Resource bar = raml.getResource("/{foo}/{bar}");
        assertThat(raml.getResources()).containsOnlyKeys("/{foo}");
        assertThat(foo.getResources()).containsOnlyKeys("/{bar}");
        assertThat(bar.getResources()).isEmpty();
        then.assertThat(foo)
                .as("newly created root resource")
                .isNotSameAs(bar)
                .hasUri("/{foo}")
                .hasRelativeUri("/{foo}")
                .hasParentUri("")
                .hasParentResource(null)
                ;
        then.assertThat(bar)
                .as("newly created intermediate resource")
                .isNotSameAs(foo)
                .hasUri("/{foo}/{bar}")
                .hasRelativeUri("/{bar}")
                .hasParentUri("/{foo}")
                .hasParentResource(foo)
                ;
    }

    @Test
    public void shouldScanVariable() {
        ResourcePath path = ResourcePath.of("/{foo}");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/{foo}")
                .hasToString("/{foo}")
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/{foo}");
        assertThat(path.items()).containsExactly("/{foo}");
        assertThat(path.vars()).containsExactly(new ResourcePathVariable(null, "foo", null));
    }

    @Test
    public void shouldStripRegexFromName() {
        ResourcePath path = ResourcePath.of("/{foo:.*}");

        ResourcePathAssert.assertThat(path)
                .hasLength(1)
                .hasSimpleName("/{foo}")
                .hasToString("/{foo:.*}")
                ;
        assertThat(path.iterator()).containsExactly(ResourcePath.of("/{foo:.*}"));
        assertThat(path.items()).containsExactly("/{foo}");
        assertThat(path.vars()).containsExactly(new ResourcePathVariable(null, "foo", ".*"));
    }
}
