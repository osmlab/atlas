package org.openstreetmap.atlas.utilities.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.MultiPolygonTest;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.testing.TestAtlasHandler;

import com.google.gson.GsonBuilder;

/**
 * @author lcram
 * @author matthieun
 */
public class ConfiguredFilterTest
{
    private static final String NAMESAKE_JSON = ConfiguredFilterTest.class.getSimpleName()
            + FileSuffix.JSON;

    public Atlas getAtlasFrom(final String name, final Class<?> clazz)
    {
        return TestAtlasHandler.getAtlasFromJosmOsmResource(true,
                new InputStreamResource(() -> clazz.getResourceAsStream(name)), name);
    }

    @Test
    public void testFilterAndMatcher()
    {
        final ConfiguredFilter junctionRoundaboutFilter = get("junctionRoundaboutFilter");
        final ConfiguredFilter dummyFilter = get("dummyFilter");
        final ConfiguredFilter tagFilterOnly = get("tagFilterOnly");
        final ConfiguredFilter defaultFilter = get("I am not there");
        final ConfiguredFilter nothingGoesThroughFilter = get("nothingGoesThroughFilter");
        final ConfiguredFilter regexFilterOnly = get("regexFilterOnly");
        final ConfiguredFilter taggableMatcherOnly = get("taggableMatcherOnly");
        final ConfiguredFilter unsafePredicateFilter = get("unsafePredicateFilter");
        final ConfiguredFilter taggableMatcherAndFilter = get("taggableMatcherAndFilter");

        final Edge edge = new CompleteEdge(123L, null,
                Maps.hashMap("junction", "roundabout", "source", "illegal source"), null, null,
                null);
        final Node node = new CompleteNode(124L, null, Maps.hashMap("source", "local knowledge"),
                null, null, null);

        Assert.assertTrue(junctionRoundaboutFilter.test(edge));
        Assert.assertFalse(dummyFilter.test(edge));
        Assert.assertTrue(tagFilterOnly.test(edge));
        Assert.assertTrue(defaultFilter.test(edge));
        Assert.assertFalse(nothingGoesThroughFilter.test(edge));
        Assert.assertTrue(regexFilterOnly.test(edge));
        Assert.assertFalse(regexFilterOnly.test(node));
        Assert.assertTrue(unsafePredicateFilter.test(edge));
        Assert.assertFalse(taggableMatcherOnly.test(edge));
        Assert.assertTrue(taggableMatcherOnly.test(node));
        Assert.assertTrue(taggableMatcherAndFilter.test(edge));
    }

    @Test
    public void testGeometryFilter1()
    {
        final ConfiguredFilter geometryFilter = get("geometryFilter1");
        final Atlas atlas = getAtlasFrom("geometryFilter.josm.osm");

        // Make sure the MultiPolygon is the same in the Atlas Relation (origin of the test WKT and
        // WKB # 2, and in the filter).
        final MultiPolygon multiPolygon = getGeometryFrom("geometryFilter.josm.osm");
        Assert.assertEquals(multiPolygon, geometryFilter.getGeometryBasedFilters().get(1));

        final Edge motorway = atlas.edges(entity -> HighwayTag.MOTORWAY.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertTrue(geometryFilter.test(motorway));
        final Edge trunk = atlas.edges(entity -> HighwayTag.TRUNK.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertFalse(geometryFilter.test(trunk));
        final Edge primary = atlas.edges(entity -> HighwayTag.PRIMARY.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertTrue(geometryFilter.test(primary));
        final Edge secondary = atlas
                .edges(entity -> HighwayTag.SECONDARY.equals(entity.highwayTag())).iterator()
                .next();
        Assert.assertTrue(geometryFilter.test(secondary));
    }

    @Test
    public void testGeometryFilter2()
    {
        final ConfiguredFilter geometryFilter = get("geometryFilter2");
        final Atlas atlas = getAtlasFrom("geometryFilter.josm.osm");

        final Edge motorway = atlas.edges(entity -> HighwayTag.MOTORWAY.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertTrue(geometryFilter.test(motorway));
        final Edge trunk = atlas.edges(entity -> HighwayTag.TRUNK.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertFalse(geometryFilter.test(trunk));
        final Edge primary = atlas.edges(entity -> HighwayTag.PRIMARY.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertTrue(geometryFilter.test(primary));
        final Edge secondary = atlas
                .edges(entity -> HighwayTag.SECONDARY.equals(entity.highwayTag())).iterator()
                .next();
        Assert.assertTrue(geometryFilter.test(secondary));
    }

    @Test
    public void testGeometryFilter3()
    {
        final ConfiguredFilter geometryFilter = get("my.root", "geometryFilter3",
                "ConfiguredFilterTestOtherRoot.json");
        final Atlas atlas = getAtlasFrom("geometryFilter.josm.osm");

        final Edge motorway = atlas.edges(entity -> HighwayTag.MOTORWAY.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertTrue(geometryFilter.test(motorway));
        final Edge trunk = atlas.edges(entity -> HighwayTag.TRUNK.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertFalse(geometryFilter.test(trunk));
        final Edge primary = atlas.edges(entity -> HighwayTag.PRIMARY.equals(entity.highwayTag()))
                .iterator().next();
        Assert.assertTrue(geometryFilter.test(primary));
        final Edge secondary = atlas
                .edges(entity -> HighwayTag.SECONDARY.equals(entity.highwayTag())).iterator()
                .next();
        Assert.assertFalse(geometryFilter.test(secondary));
    }

    @Test
    public void testIsNoExpansion()
    {
        Assert.assertTrue(get("nothingGoesThroughFilter").isNoExpansion());
    }

    @Test
    public void testToJson()
    {
        final ConfiguredFilter tagFilterOnly = get("tagFilterOnly");
        final String jsonString1 = new GsonBuilder().disableHtmlEscaping().create()
                .toJson(tagFilterOnly.toJson());
        Assert.assertEquals(
                "{\"type\":\"_filter\",\"name\":\"tagFilterOnly\",\"taggableFilter\":\"junction->roundabout\",\"noExpansion\":false}",
                jsonString1);

        final ConfiguredFilter unsafePredicateFilter = get("unsafePredicateFilter");
        final String jsonString2 = new GsonBuilder().disableHtmlEscaping().create()
                .toJson(unsafePredicateFilter.toJson());
        Assert.assertEquals(
                "{\"type\":\"_filter\",\"name\":\"unsafePredicateFilter\",\"unsafePredicate\":\"e instanceof Edge\",\"imports\":[\"org.openstreetmap.atlas.geography.atlas.items\"],\"noExpansion\":false}",
                jsonString2);

        final ConfiguredFilter dummyFilter = get("dummyFilter");
        final String jsonString3 = new GsonBuilder().disableHtmlEscaping().create()
                .toJson(dummyFilter.toJson());
        Assert.assertEquals(
                "{\"type\":\"_filter\",\"name\":\"dummyFilter\",\"predicate\":\"\\\"yes\\\".equals(e.getTag(\\\"dummy\\\"))\",\"imports\":[\"org.openstreetmap.atlas.geography.atlas.items\"],\"noExpansion\":false}",
                jsonString3);

        final ConfiguredFilter regexFilterOnly = get("regexFilterOnly");
        final String jsonString4 = new GsonBuilder().disableHtmlEscaping().create()
                .toJson(regexFilterOnly.toJson());
        Assert.assertEquals(
                "{\"type\":\"_filter\",\"name\":\"regexFilterOnly\",\"regexTaggableFilter\":\"source,highway|.*illegal.*,.*secondary.*\",\"noExpansion\":false}",
                jsonString4);
    }

    private ConfiguredFilter get(final String name)
    {
        return get(ConfiguredFilter.CONFIGURATION_ROOT, name, NAMESAKE_JSON);
    }

    private ConfiguredFilter get(final String root, final String name, final String resourceName)
    {
        final Configuration configuration = new StandardConfiguration(new InputStreamResource(
                () -> ConfiguredFilterTest.class.getResourceAsStream(resourceName)));
        return ConfiguredFilter.from(root, name, configuration);
    }

    private Atlas getAtlasFrom(final String name)
    {
        return getAtlasFrom(name, ConfiguredFilterTest.class);
    }

    private MultiPolygon getGeometryFrom(final String name)
    {
        return MultiPolygonTest.getFrom(name, ConfiguredFilterTest.class);
    }
}
