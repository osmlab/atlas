package org.openstreetmap.atlas.utilities.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Test case for the StandardConfiguration implementation
 *
 * @author cstaylor
 * @author brian_l_davis
 */
public class StandardConfigurationTest
{
    public static final String CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/application.json";

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
    public void testStandardConfiguration() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(CONFIGURATION))
        {
            final StandardConfiguration configuration = new StandardConfiguration(
                    new InputStreamResource(stream));
            final String country = "AIA";
            final String key = String.format("feature.%s.range", country);

            final Optional<Map<String, Double>> range = configuration.get(key).valueOption();

            Assert.assertTrue(range.isPresent());
            range.ifPresent(enforcements ->
            {
                Assert.assertEquals(10.0, enforcements.get("min"), 0.1);
                Assert.assertEquals(20.0, enforcements.get("max"), 0.1);
            });
            Assert.assertFalse(configuration.get("foo").valueOption().isPresent());
        }
    }

    @Test
    public void testTransformation() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream stream = loader.getResourceAsStream(CONFIGURATION))
        {
            final StandardConfiguration configuration = new StandardConfiguration(
                    new InputStreamResource(stream));
            final String country = "AIA";
            final String minKey = String.format("feature.%s.range.min", country);
            final String maxKey = String.format("feature.%s.range.max", country);

            Assert.assertEquals(Angle.degrees(10.0),
                    configuration.get(minKey, Angle::degrees).value());
            Assert.assertEquals(Angle.degrees(20.0),
                    configuration.get(maxKey, Angle::degrees).value());
        }
    }
}
