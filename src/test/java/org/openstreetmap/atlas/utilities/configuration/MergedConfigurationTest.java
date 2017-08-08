package org.openstreetmap.atlas.utilities.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.StringInputStream;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;

/**
 * @author brian_l_davis
 */
public class MergedConfigurationTest
{

    private static final String BASE_CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/application.json";
    private static final String OVERRIDE_CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/development.json";
    private static final String PARTIAL_CONFIGURATION = "org/openstreetmap/atlas/utilities/configuration/feature.json";

    @Test
    public void testDotFormat() throws IOException
    {
        final String expandedSource = "{\"a\":{\"b\":{\"c\":0,\"d\":1}}}";
        final String expandedOverrideSource = "{\"a\":{\"b\":{\"c\":2}}}";
        final String flattenedSource = "{\"a.b.c\": 0, \"a.b.d\": 1}";
        final String flattenedOverrideSource = "{\"a.b.c\": 2}";

        validateSame(expandedSource, expandedOverrideSource, flattenedSource,
                flattenedOverrideSource);
        validateSame(expandedSource, flattenedOverrideSource, flattenedSource,
                expandedOverrideSource);
    }

    @Test
    public void testLayered() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION))
        {
            final Configuration configuration = new MergedConfiguration(
                    new InputStreamResource(base), new InputStreamResource(override));

            final String country = "AIA";
            final String key = String.format("feature.%s.range", country);

            final Optional<Map<String, Double>> rangeOption = configuration.get(key).valueOption();

            Assert.assertTrue(rangeOption.isPresent());
            rangeOption.ifPresent(range ->
            {
                Assert.assertEquals(5.0, range.get("min"), 0.1);
                Assert.assertEquals(30.0, range.get("max"), 0.1);
            });

            final String baseCountry = "AIA";
            final String baseKey = String.format("feature.%s.range", baseCountry);
            final Double max = configuration.get(baseKey + ".min", Double.NaN).value();
            Assert.assertNotEquals(Double.NaN, max);

            Assert.assertFalse(configuration.get("foo").valueOption().isPresent());
        }
    }

    @Test
    public void testPartial() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION);
                InputStream partial = loader.getResourceAsStream(PARTIAL_CONFIGURATION))
        {
            final Configuration configuration = new MergedConfiguration(
                    new InputStreamResource(base), new InputStreamResource(partial));

            final String country = "CYM";
            final String key = String.format("feature.%s.range", country);

            final Optional<Map<String, Double>> rangeOption = configuration.get(key).valueOption();

            Assert.assertTrue(rangeOption.isPresent());
            rangeOption.ifPresent(range ->
            {
                Assert.assertEquals(20.0, range.get("min"), 0.1);
                Assert.assertEquals(40.0, range.get("max"), 0.1);
            });
            Assert.assertFalse(configuration.get("foo").valueOption().isPresent());
        }
    }

    private void validateSame(final String leftBase, final String leftOverride,
            final String rightBase, final String rightOverride) throws IOException
    {
        try (InputStream leftBaseStream = new StringInputStream(leftBase);
                InputStream leftOverrideStream = new StringInputStream(leftOverride);
                InputStream rightBaseStream = new StringInputStream(rightBase);
                InputStream rightOverrideStream = new StringInputStream(rightOverride))
        {
            final Configuration left = new MergedConfiguration(
                    new InputStreamResource(leftBaseStream),
                    new InputStreamResource(leftOverrideStream));
            final Configuration right = new MergedConfiguration(
                    new InputStreamResource(rightBaseStream),
                    new InputStreamResource(rightOverrideStream));

            Assert.assertEquals(2L, (long) left.get("a.b.c").value());
            Assert.assertEquals(1L, (long) left.get("a.b.d").value());
            Assert.assertEquals((long) left.get("a.b.c").value(),
                    (long) right.get("a.b.c").value());
            Assert.assertEquals((long) left.get("a.b.d").value(),
                    (long) right.get("a.b.d").value());

            final Map<String, Object> leftMap = left.get("a.b").value();
            final Map<String, Object> rightMap = right.get("a.b").value();

            Assert.assertEquals(2L, leftMap.get("c"));
            Assert.assertEquals(2L, rightMap.get("c"));
            Assert.assertEquals(1L, leftMap.get("d"));
            Assert.assertEquals(1L, rightMap.get("d"));
        }
    }
}
