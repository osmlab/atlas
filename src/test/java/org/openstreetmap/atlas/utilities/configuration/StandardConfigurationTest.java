package org.openstreetmap.atlas.utilities.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Test case for the StandardConfiguration implementation
 *
 * @author cstaylor
 * @author brian_l_davis
 * @author jklamer
 * @author cameron_frenette
 */
public class StandardConfigurationTest
{
    public static final String CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/application.json";
    public static final String KEYWORD_OVERRIDDEN_CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/keywordOverridingApplication.json";
    public static final String YAML_CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/application.yml";
    public static final String YAML_DOT_EXPANDED = "org/openstreetmap/atlas/utilities/configuration/yaml_dot_expanded.yml";
    public static final String YAML_DOT_COMPRESSED = "org/openstreetmap/atlas/utilities/configuration/yaml_dot_compressed.yml";
    public static final String YAML_KEYWORD_OVERRIDDEN_CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/keywordOverridingApplication.yml";

    @Test
    public void testConfigurationDataKeySetYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(YAML_CONFIGURATION))
        {
            testConfigurationDataKeySet(stream);
        }
    }

    @Test
    public void testConfigurationDataKeySetJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(CONFIGURATION))
        {
            testConfigurationDataKeySet(stream);
        }
    }

    private void testConfigurationDataKeySet(final InputStream stream)
    {
        final StandardConfiguration configuration = new StandardConfiguration(
                new InputStreamResource(stream));

        final Set<String> compareTo = new HashSet<>();
        compareTo.add("feature");
        Assert.assertEquals(configuration.configurationDataKeySet(), compareTo);
    }

    @Test
    public void testDefaults() throws IOException
    {
        try (InputStream stream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)))
        {
            final StandardConfiguration configuration = new StandardConfiguration(
                    new InputStreamResource(stream));
            final String minKey = "missing.range.min";
            final String maxKey = "missing.range.max";

            Assert.assertEquals(new Double(1.0), configuration.get(minKey, 1.0).value());
            Assert.assertEquals(new Double(100.0), configuration.get(maxKey, 100.0).value());
            Assert.assertEquals(Angle.degrees(1.0),
                    configuration.get(minKey, 1.0, Angle::degrees).value());
            Assert.assertEquals(Angle.degrees(100.0),
                    configuration.get(maxKey, 100.0, Angle::degrees).value());
        }
    }

    @Test
    public void testDotFormat() throws IOException
    {
        try (InputStream expandedSource = new ByteArrayInputStream(
                "{\"a\":{\"b\":{\"c\":0,\"d\":1}}}".getBytes(StandardCharsets.UTF_8));
                InputStream flattenedSource = new ByteArrayInputStream(
                        "{\"a.b.c\": 0, \"a.b.d\": 1}".getBytes(StandardCharsets.UTF_8)))
        {
            final StandardConfiguration expanded = new StandardConfiguration(
                    new InputStreamResource(expandedSource));
            final StandardConfiguration flattened = new StandardConfiguration(
                    new InputStreamResource(flattenedSource));

            Assert.assertEquals((long) expanded.get("a.b.c").value(),
                    (long) flattened.get("a.b.c").value());
            Assert.assertEquals((long) expanded.get("a.b.d").value(),
                    (long) flattened.get("a.b.d").value());
        }
    }

    @Test
    public void testDotFormatYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream expandedSource = loader.getResourceAsStream(YAML_DOT_EXPANDED);
                InputStream flattenedSource = loader.getResourceAsStream(YAML_DOT_COMPRESSED))
        {
            final StandardConfiguration expanded = new StandardConfiguration(
                    new InputStreamResource(expandedSource));
            final StandardConfiguration flattened = new StandardConfiguration(
                    new InputStreamResource(flattenedSource));

            Assert.assertEquals(0, (long) expanded.get("a.b.c").value());
            Assert.assertEquals(1, (long) expanded.get("a.b.d").value());
            Assert.assertEquals((long) expanded.get("a.b.c").value(),
                    (long) flattened.get("a.b.c").value());
            Assert.assertEquals((long) expanded.get("a.b.d").value(),
                    (long) flattened.get("a.b.d").value());
        }
    }

    @Test
    public void testSpecificFormatLoad() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream expandedSource1 = loader.getResourceAsStream(YAML_DOT_EXPANDED);
                InputStream flattenedSource1 = loader.getResourceAsStream(YAML_DOT_COMPRESSED);
                InputStream expandedSource2 = loader.getResourceAsStream(YAML_DOT_EXPANDED);
                InputStream flattenedSource2 = loader.getResourceAsStream(YAML_DOT_COMPRESSED))
        {
            final StandardConfiguration expanded1 = new StandardConfiguration(
                    new InputStreamResource(expandedSource1));
            final StandardConfiguration flattened1 = new StandardConfiguration(
                    new InputStreamResource(flattenedSource1));

            final StandardConfiguration expanded2 = new StandardConfiguration(
                    new InputStreamResource(expandedSource2),
                    StandardConfiguration.ConfigurationFormat.YAML);
            final StandardConfiguration flattened2 = new StandardConfiguration(
                    new InputStreamResource(flattenedSource2),
                    StandardConfiguration.ConfigurationFormat.YAML);

            Assert.assertEquals(0, (long) expanded1.get("a.b.c").value());
            Assert.assertEquals(1, (long) expanded1.get("a.b.d").value());
            Assert.assertEquals((long) expanded1.get("a.b.c").value(),
                    (long) flattened1.get("a.b.c").value());
            Assert.assertEquals((long) expanded1.get("a.b.d").value(),
                    (long) flattened1.get("a.b.d").value());

            Assert.assertEquals(0, (long) expanded2.get("a.b.c").value());
            Assert.assertEquals(1, (long) expanded2.get("a.b.d").value());
            Assert.assertEquals((long) expanded2.get("a.b.c").value(),
                    (long) flattened2.get("a.b.c").value());
            Assert.assertEquals((long) expanded2.get("a.b.d").value(),
                    (long) flattened2.get("a.b.d").value());
        }
    }

    @Test
    public void testInFileOverrideJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream(KEYWORD_OVERRIDDEN_CONFIGURATION))
        {
            testInFileOverride(stream);
        }
    }

    @Test
    public void testInFileOverrideYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream(YAML_KEYWORD_OVERRIDDEN_CONFIGURATION))
        {
            testInFileOverride(stream);
        }
    }

    private void testInFileOverride(final InputStream stream)
    {
        final String keyword1 = "ABC";
        final String keyword2 = "XYZ";
        final String notAnOverRiddenKeyword = "ZZZ";
        final StandardConfiguration configuration = new StandardConfiguration(
                new InputStreamResource(stream));
        final Configuration overriddenConfiguration1 = configuration
                .configurationForKeyword(keyword1);
        final Configuration overriddenConfiguration2 = configuration
                .configurationForKeyword(keyword2);
        final Configuration sameConfiguration = configuration
                .configurationForKeyword(notAnOverRiddenKeyword);
        final String overriddenKey = "feature.range.max";
        final String notOverriddenKey = "feature.range.min";
        final String overriddenListKey = "feature.list";

        // assert configuration is same for non-overridden keyword
        Assert.assertEquals(configuration, sameConfiguration);
        // assert override works for objects
        Assert.assertEquals(Arrays.asList("nodes"),
                overriddenConfiguration1.get(overriddenListKey).value());
        // assert different values for same key in different configurations based on keyword
        Assert.assertEquals(Angle.degrees(20.0),
                overriddenConfiguration1.get(overriddenKey, Angle::degrees).value());
        Assert.assertEquals(Angle.degrees(30.0),
                overriddenConfiguration2.get(overriddenKey, Angle::degrees).value());
        // assert same value for same key in different configurations based on keyword
        Assert.assertEquals(Angle.degrees(10.0),
                overriddenConfiguration1.get(notOverriddenKey, Angle::degrees).value());
        Assert.assertEquals(Angle.degrees(10.0),
                overriddenConfiguration2.get(notOverriddenKey, Angle::degrees).value());
    }

    @Test
    public void testMissing() throws IOException
    {
        try (InputStream stream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)))
        {
            final StandardConfiguration configuration = new StandardConfiguration(
                    new InputStreamResource(stream));
            final String minKey = "missing.range.min";
            final String maxKey = "missing.range.max";

            Assert.assertNull(configuration.get(minKey).value());
            Assert.assertNull(configuration.get(maxKey).value());
            Assert.assertNull(configuration.get(minKey, Angle::degrees).value());
            Assert.assertNull(configuration.get(maxKey, Angle::degrees).value());
        }
    }

    @Test
    public void testStandardConfigurationJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(CONFIGURATION))
        {
            testStandardConfiguration(stream);
        }
    }

    @Test
    public void testStandardConfigurationYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(YAML_CONFIGURATION))
        {
            testStandardConfiguration(stream);
        }
    }

    private void testStandardConfiguration(final InputStream stream)
    {
        final StandardConfiguration configuration = new StandardConfiguration(
                new InputStreamResource(stream));
        final String keyword = "ABC";
        final String key = String.format("feature.%s.range", keyword);

        final Optional<Map<String, Double>> range = configuration.get(key).valueOption();

        Assert.assertTrue(range.isPresent());
        range.ifPresent(enforcements ->
        {
            Assert.assertEquals(10.0, enforcements.get("min"), 0.1);
            Assert.assertEquals(20.0, enforcements.get("max"), 0.1);
        });
        Assert.assertFalse(configuration.get("foo").valueOption().isPresent());
    }

    @Test
    public void testTransformationJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(CONFIGURATION))
        {
            testTransformation(stream);
        }
    }

    @Test
    public void testTransformationYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(YAML_CONFIGURATION))
        {
            testTransformation(stream);
        }
    }

    private void testTransformation(final InputStream stream)
    {

        final StandardConfiguration configuration = new StandardConfiguration(
                new InputStreamResource(stream));
        final String keyword = "ABC";
        final String minKey = String.format("feature.%s.range.min", keyword);
        final String maxKey = String.format("feature.%s.range.max", keyword);

        Assert.assertEquals(Angle.degrees(10.0), configuration.get(minKey, Angle::degrees).value());
        Assert.assertEquals(Angle.degrees(20.0), configuration.get(maxKey, Angle::degrees).value());

    }
}
