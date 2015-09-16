package com.github.t1.ramlap;

import static org.assertj.core.api.Assertions.*;

import org.junit.*;
import org.raml.model.*;

public class ResourcePathTest {
    @Rule
    public JUnitSoftAssertions then = new JUnitSoftAssertions();

    @Test
    public void shouldBuildEmpty() {
        ResourcePath path = ResourcePath.of("");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/") //
                .hasToString("/") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/");
        assertThat(path.items()).containsExactly("/");
    }

    @Test
    public void shouldBuildSlashOnly() {
        ResourcePath path = ResourcePath.of("/");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/") //
                .hasToString("/") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/");
        assertThat(path.items()).containsExactly("/");
    }

    @Test
    public void shouldBuildOneItemWithoutSlash() {
        ResourcePath path = ResourcePath.of("foo");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/foo") //
                .hasToString("/foo") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildOneItemWithSlash() {
        ResourcePath path = ResourcePath.of("/foo");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/foo") //
                .hasToString("/foo") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildOneItemWhenConcatenatingFromEmpty() {
        ResourcePath path = ResourcePath.of("").and("/foo");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/foo") //
                .hasToString("/foo") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildOneItemWhenConcatenatingFromSlash() {
        ResourcePath path = ResourcePath.of("/").and("/foo");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/foo") //
                .hasToString("/foo") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo");
        assertThat(path.items()).containsExactly("/foo");
    }

    @Test
    public void shouldBuildTwoItems() {
        ResourcePath path = ResourcePath.of("/foo/bar");

        ResourcePathAssert.assertThat(path) //
                .hasLength(2) //
                .hasSimpleName("/bar") //
                .hasToString("/foo/bar") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar");
        assertThat(path.items()).containsExactly("/foo", "/bar");
    }

    @Test
    public void shouldBuildWithAnd() {
        ResourcePath path = ResourcePath.of("/foo").and("/bar");

        ResourcePathAssert.assertThat(path) //
                .hasLength(2) //
                .hasSimpleName("/bar") //
                .hasToString("/foo/bar") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar");
        assertThat(path.items()).containsExactly("/foo", "/bar");
    }

    @Test
    public void shouldBuildWithTwoAnd() {
        ResourcePath path = ResourcePath.of("/foo").and("/bar").and("/baz");

        ResourcePathAssert.assertThat(path) //
                .hasLength(3) //
                .hasSimpleName("/baz") //
                .hasToString("/foo/bar/baz") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldBuildWithAndContainingSlash() {
        ResourcePath path = ResourcePath.of("/foo").and("/bar/baz");

        ResourcePathAssert.assertThat(path) //
                .hasLength(3) //
                .hasSimpleName("/baz") //
                .hasToString("/foo/bar/baz") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldBuildFromSlashWithAndContainingSlash() {
        ResourcePath path = ResourcePath.of("/").and("/foo/bar/baz");

        ResourcePathAssert.assertThat(path) //
                .hasLength(3) //
                .hasSimpleName("/baz") //
                .hasToString("/foo/bar/baz") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldBuildThreeItems() {
        ResourcePath path = ResourcePath.of("/foo/bar/baz");

        ResourcePathAssert.assertThat(path) //
                .hasLength(3) //
                .hasSimpleName("/baz") //
                .hasToString("/foo/bar/baz") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/foo", "/bar", "/baz");
        assertThat(path.items()).containsExactly("/foo", "/bar", "/baz");
    }

    @Test
    public void shouldSetRamlForEmptyRootResource() {
        ResourcePath path = ResourcePath.of("/");
        Raml raml = new Raml();
        Resource resource = new Resource();

        path.setResource(raml, resource);

        assertThat(raml.getResources()).containsOnlyKeys("/");
        assertThat(resource.getResources()).isEmpty();
        then.assertThat(resource) //
                .hasUri("/") //
                .hasRelativeUri("/") //
                .hasParentResource(null) //
                .hasParentUri("") //
                .isSameAs(raml.getResource("/")) //
                ;
    }

    @Test
    public void shouldSetRamlForRootResourceReplacingOld() {
        ResourcePath path = ResourcePath.of("/foo");
        Raml raml = new Raml();
        Resource old = new Resource();
        raml.getResources().put("/foo", old);
        Resource resource = new Resource();

        path.setResource(raml, resource);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        assertThat(old.getResources()).isEmpty();
        assertThat(resource.getResources()).isEmpty();
        then.assertThat(resource) //
                .hasUri("/foo") //
                .hasRelativeUri("/foo") //
                .hasParentUri("") //
                .hasParentResource(null) //
                ;
        then.assertThat(raml.getResource("/foo")) //
                .isNotSameAs(old) //
                .isSameAs(resource) //
                ;
    }

    @Test
    public void shouldCreateNewRootResourceWhenSettingSubResource() {
        Raml raml = new Raml();
        ResourcePath path = ResourcePath.of("/foo/bar");
        Resource bar = new Resource();

        path.setResource(raml, bar);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        Resource foo = raml.getResource("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        assertThat(bar.getResources()).isEmpty();
        then.assertThat(foo) //
                .as("newly created root resource") //
                .isNotSameAs(bar) //
                .hasUri("/foo") //
                .hasRelativeUri("/foo") //
                .hasParentUri("") //
                .hasParentResource(null) //
                ;
        then.assertThat(bar) //
                .hasUri("/foo/bar") //
                .hasRelativeUri("/bar") //
                .hasParentUri("/foo") //
                .hasParentResource(foo) //
                .isSameAs(raml.getResource("/foo/bar")) //
                ;
    }

    @Test
    public void shouldReuseRootResourceWhenSettingSubResource() {
        Raml raml = new Raml();
        ResourcePath path = ResourcePath.of("/foo");
        Resource foo = new Resource();
        path.setResource(raml, foo);
        Resource bar = new Resource();

        path.and("/bar").setResource(raml, bar);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        assertThat(bar.getResources()).isEmpty();
        then.assertThat(bar) //
                .hasUri("/foo/bar") //
                .hasRelativeUri("/bar") //
                .hasParentUri("/foo") //
                .hasParentResource(foo) //
                .isSameAs(raml.getResource("/foo/bar")) //
                ;
        then.assertThat(raml.getResource("/foo")) //
                .isSameAs(foo) //
                .isNotSameAs(bar) //
                ;
    }

    @Test
    public void shouldCreateNewParentResourceWhenSettingSubSubResource() {
        Raml raml = new Raml();
        ResourcePath path = ResourcePath.of("/foo/bar/baz");
        Resource baz = new Resource();

        path.setResource(raml, baz);

        Resource foo = raml.getResource("/foo");
        Resource bar = raml.getResource("/foo/bar");
        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        assertThat(bar.getResources()).containsOnlyKeys("/baz");
        assertThat(baz.getResources()).isEmpty();
        then.assertThat(foo) //
                .as("newly created root resource") //
                .isNotSameAs(bar) //
                .isNotSameAs(baz) //
                .hasUri("/foo") //
                .hasRelativeUri("/foo") //
                .hasParentUri("") //
                .hasParentResource(null) //
                ;
        then.assertThat(bar) //
                .as("newly created intermediate resource") //
                .isNotSameAs(foo) //
                .isNotSameAs(baz) //
                .hasUri("/foo/bar") //
                .hasRelativeUri("/bar") //
                .hasParentUri("/foo") //
                .hasParentResource(foo) //
                ;
        then.assertThat(baz) //
                .isNotSameAs(foo) //
                .isNotSameAs(bar) //
                .hasUri("/foo/bar/baz") //
                .hasRelativeUri("/baz") //
                .hasParentUri("/foo/bar") //
                .hasParentResource(bar) //
                .isSameAs(raml.getResource("/foo/bar/baz")) //
                ;
    }

    @Test
    public void shouldAddParentResourceWithPattern() {
        Raml raml = new Raml();
        ResourcePath methodPath = ResourcePath.of("/{foo:a}/{bar}");
        Resource resource = new Resource();

        methodPath.setResource(raml, resource);

        Resource foo = raml.getResource("/{foo}");
        Resource bar = raml.getResource("/{foo}/{bar}");
        assertThat(raml.getResources()).containsOnlyKeys("/{foo}");
        assertThat(foo.getResources()).containsOnlyKeys("/{bar}");
        assertThat(bar.getResources()).isEmpty();
        then.assertThat(foo) //
                .as("newly created root resource") //
                .isNotSameAs(bar) //
                .hasUri("/{foo}") //
                .hasRelativeUri("/{foo}") //
                .hasParentUri("") //
                .hasParentResource(null) //
                ;
        then.assertThat(bar) //
                .as("newly created intermediate resource") //
                .isNotSameAs(foo) //
                .hasUri("/{foo}/{bar}") //
                .hasRelativeUri("/{bar}") //
                .hasParentUri("/{foo}") //
                .hasParentResource(foo) //
                ;
    }

    @Test
    public void shouldScanVariable() {
        ResourcePath path = ResourcePath.of("/{foo}");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/{foo}") //
                .hasToString("/{foo}") //
                ;
        assertThat(path.iterator()).extracting("name").containsExactly("/{foo}");
        assertThat(path.items()).containsExactly("/{foo}");
        assertThat(path.vars()).containsExactly(new ResourcePathVariable(null, "foo", null));
    }

    @Test
    public void shouldStripRegexFromName() {
        ResourcePath path = ResourcePath.of("/{foo:.*}");

        ResourcePathAssert.assertThat(path) //
                .hasLength(1) //
                .hasSimpleName("/{foo}") //
                .hasToString("/{foo:.*}") //
                ;
        assertThat(path.iterator()).containsExactly(ResourcePath.of("/{foo:.*}"));
        assertThat(path.items()).containsExactly("/{foo}");
        assertThat(path.vars()).containsExactly(new ResourcePathVariable(null, "foo", ".*"));
    }
}
