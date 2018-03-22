package org.openstreetmap.atlas.utilities.filters;

import static org.openstreetmap.atlas.utilities.filters.IntersectionPolicy.DEFAULT_INTERSECTION_POLICY;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.converters.MultiPolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.PolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.WkbMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WkbPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WktMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.testing.FreezeDryFunction;

import com.vividsolutions.jts.io.WKBWriter;

/**
 * Tests for {@link AtlasEntityPolygonsFilter}
 *
 * @author jklamer
 */
public class AtlasEntityPolygonsFilterTest
{
    private static final IntersectionPolicy FULL_GEOMETRIC_ENCLOSING = new IntersectionPolicy()
    {
        @Override
        public boolean multiPolygonEntityIntersecting(final MultiPolygon multiPolygon,
                final AtlasEntity entity)
        {

            if (entity instanceof LineItem)
            {
                return multiPolygon.fullyGeometricallyEncloses(((LineItem) entity).asPolyLine());
            }
            if (entity instanceof LocationItem)
            {
                return multiPolygon
                        .fullyGeometricallyEncloses(((LocationItem) entity).getLocation());
            }
            if (entity instanceof Area)
            {
                return multiPolygon.fullyGeometricallyEncloses(((Area) entity).asPolygon());
            }
            if (entity instanceof Relation)
            {
                return ((Relation) entity).members().stream().map(RelationMember::getEntity)
                        .anyMatch(relationEntity -> this
                                .multiPolygonEntityIntersecting(multiPolygon, relationEntity));
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean polygonEntityIntersecting(final Polygon polygon, final AtlasEntity entity)
        {

            if (entity instanceof LineItem)
            {
                return polygon.fullyGeometricallyEncloses(((LineItem) entity).asPolyLine());
            }
            if (entity instanceof LocationItem)
            {
                return polygon.fullyGeometricallyEncloses(((LocationItem) entity).getLocation());
            }
            if (entity instanceof Area)
            {
                return polygon.fullyGeometricallyEncloses(((Area) entity).asPolygon());
            }
            if (entity instanceof Relation)
            {
                return ((Relation) entity).members().stream().map(RelationMember::getEntity)
                        .anyMatch(relationEntity -> this.polygonEntityIntersecting(polygon,
                                relationEntity));
            }
            else
            {
                return false;
            }
        }

        // note this is the only one now used by the filter
        @Override
        public boolean geometricSurfaceEntityIntersecting(final GeometricSurface geometricSurface,
                final AtlasEntity entity)
        {
            if (entity instanceof LineItem)
            {
                return geometricSurface
                        .fullyGeometricallyEncloses(((LineItem) entity).asPolyLine());
            }
            if (entity instanceof LocationItem)
            {
                return geometricSurface
                        .fullyGeometricallyEncloses(((LocationItem) entity).getLocation());
            }
            if (entity instanceof Area)
            {
                return geometricSurface.fullyGeometricallyEncloses(((Area) entity).asPolygon());
            }
            if (entity instanceof Relation)
            {
                return ((Relation) entity).members().stream().map(RelationMember::getEntity)
                        .anyMatch(relationEntity -> this.geometricSurfaceEntityIntersecting(
                                geometricSurface, relationEntity));
            }
            else
            {
                return false;
            }
        }
    };

    @Rule
    public final AtlasEntityPolygonsFilterTestRule setup = new AtlasEntityPolygonsFilterTestRule();

    /**
     * Test Standard use cases by validating counts and asserting symmetry of include vs. exclude
     */
    @Test
    public void testCounts()
    {
        final Atlas testCountsAtlas = this.setup.getTestCounts();
        final String includeConfigurationFormat = "{\"filter.polygons\":{\"include.wkt\":[\"%s\"]}}";
        final String excludeConfigurationFormat = "{\"filter.polygons\":{\"exclude.wkt\":[\"%s\"]}}";
        final Polygon polygon1 = this.getPolygonWithName(testCountsAtlas, "polygon1");
        final Polygon polygon2 = this.getPolygonWithName(testCountsAtlas, "polygon2");

        final long totalPointCount = testCountsAtlas.numberOfPoints();
        final long totalLineCount = testCountsAtlas.numberOfLines();
        final long totalAreaCount = testCountsAtlas.numberOfAreas();
        final long totalRelationCount = testCountsAtlas.numberOfRelations();
        final AtlasEntityPolygonsFilter includePolygon1 = this
                .constructConfiguredFilter(includeConfigurationFormat, polygon1.toWkt());
        final AtlasEntityPolygonsFilter includePolygon2 = this
                .constructConfiguredFilter(includeConfigurationFormat, polygon2.toWkt());
        final AtlasEntityPolygonsFilter includePolygon1And2 = this.constructConfiguredFilter(
                includeConfigurationFormat,
                String.join("\",\"", polygon1.toWkt(), polygon2.toWkt()));
        final AtlasEntityPolygonsFilter excludePolygon1 = this
                .constructConfiguredFilter(excludeConfigurationFormat, polygon1.toWkt());
        final AtlasEntityPolygonsFilter excludePolygon2 = this
                .constructConfiguredFilter(excludeConfigurationFormat, polygon2.toWkt());
        final AtlasEntityPolygonsFilter excludePolygon1And2 = this.constructConfiguredFilter(
                excludeConfigurationFormat,
                String.join("\",\"", polygon1.toWkt(), polygon2.toWkt()));

        // testing polygon1
        this.assertCounts(testCountsAtlas, includePolygon1, 2L, 3L, 1L, 0L);
        this.assertCounts(testCountsAtlas, excludePolygon1, totalPointCount - 2L,
                totalLineCount - 3L, totalAreaCount - 1L, totalRelationCount);
        this.assertCounts(testCountsAtlas, includePolygon1.negate(), totalPointCount - 2L,
                totalLineCount - 3L, totalAreaCount - 1L, totalRelationCount);

        // testing polygon2
        this.assertCounts(testCountsAtlas, includePolygon2, 0L, 2L, 1L, 1L);
        this.assertCounts(testCountsAtlas, excludePolygon2, totalPointCount, totalLineCount - 2L,
                totalAreaCount - 1L, totalRelationCount - 1L);
        this.assertCounts(testCountsAtlas, excludePolygon2.negate(), 0L, 2L, 1L, 1L);

        // testing tandem filter
        this.assertCounts(testCountsAtlas, includePolygon1And2, totalPointCount - 1L,
                totalLineCount - 2L, totalAreaCount, totalRelationCount);
        this.assertCounts(testCountsAtlas, excludePolygon1And2, 1L, 2L, 0L, 0L);
        this.assertCounts(testCountsAtlas, includePolygon1And2.negate(), 1L, 2L, 0L, 0L);

        // testing simple filter construction
        this.assertCounts(testCountsAtlas,
                AtlasEntityPolygonsFilter.Type.INCLUDE.polygons(Collections.singleton(polygon1)),
                2L, 3L, 1L, 0L);
        this.assertCounts(testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .geometricSurfaces(Collections.singleton(polygon1)), 2L, 3L, 1L, 0L);
        this.assertCounts(testCountsAtlas,
                AtlasEntityPolygonsFilter.Type.EXCLUDE.polygons(Collections.singleton(polygon1)),
                totalPointCount - 2L, totalLineCount - 3L, totalAreaCount - 1L, totalRelationCount);
        this.assertCounts(testCountsAtlas,
                AtlasEntityPolygonsFilter.Type.EXCLUDE
                        .geometricSurfaces(Collections.singleton(polygon1)),
                totalPointCount - 2L, totalLineCount - 3L, totalAreaCount - 1L, totalRelationCount);
        this.assertCounts(testCountsAtlas,
                AtlasEntityPolygonsFilter.Type.INCLUDE.polygons(Arrays.asList(polygon1, polygon2)),
                totalPointCount - 1L, totalLineCount - 2L, totalAreaCount, totalRelationCount);
    }

    /**
     * Tests every polygon input form supported by the filter
     */
    @Test
    public void testForms()
    {
        final Atlas testFormsAtlas = this.setup.getTestForm();
        final String wktConfigurationStringFormat = "{\"filter.polygons\":{\"include.wkt\":[\"%s\"]}}";
        final String wkbConfigurationStringFormat = "{\"filter.polygons\":{\"include.wkb\":[\"%s\"]}}";
        final String atlasConfigurationStringFormat = "{\"filter.polygons\":{\"include.atlas\":[\"%s\"]}}";
        final String geojsonConfigurationStringFormat = "{\"filter.polygons\":{\"include.geojson\":[\"%s\"]}}";
        final Polygon includeBoundary = this.getPolygonWithName(testFormsAtlas, "include");

        this.assertCounts(this.setup.getTestForm(), this.constructConfiguredFilter(
                wktConfigurationStringFormat, includeBoundary.toWkt()), 2, 2, 2, 0);
        this.assertCounts(this.setup.getTestForm(),
                this.constructConfiguredFilter(wkbConfigurationStringFormat,
                        WKBWriter.toHex(new WkbPolygonConverter().convert(includeBoundary))),
                2, 2, 2, 0);
        this.assertCounts(this.setup.getTestForm(),
                this.constructConfiguredFilter(atlasConfigurationStringFormat,
                        new PolygonStringConverter().backwardConvert(includeBoundary)),
                2, 2, 2, 0);
        this.assertCounts(this.setup.getTestForm(),
                this.constructConfiguredFilter(geojsonConfigurationStringFormat,
                        includeBoundary.asGeoJson().toString().replaceAll("\"", "\\\\\"")),
                2, 2, 2, 0);
    }

    /**
     * Tests every arrangement of 1 include and 1 exclude polygon in a configuration. Each situation
     * the include dominance of the configured filter means that exclude polygon is ignored. Each
     * situation designed to have same count result.
     */
    @Test
    public void testIncludeDominant()
    {
        final int numberOfCases = 4;
        final Atlas includeExcludeArrangements = this.setup.getIncludeExcludeArrangements();
        final String configurationStringFormat = "{\"filter.polygons\":{\"include.wkt\":[\"%s\"],\"exclude.wkt\":[\"%s\"]}}";
        final Polygon includePolygon = this.getPolygonWithName(includeExcludeArrangements,
                "include");

        for (int caseNumber = 1; caseNumber <= numberOfCases; caseNumber++)
        {
            final String caseString = String.format("case_%d", caseNumber);
            final String excludePolygonName = "exclude_".concat(caseString);
            final Polygon excludePolygon = this.getPolygonWithName(includeExcludeArrangements,
                    excludePolygonName);
            final AtlasEntityPolygonsFilter filter = this.constructConfiguredFilter(
                    configurationStringFormat, includePolygon.toWkt(), excludePolygon.toWkt());

            Assert.assertEquals(2L, StreamSupport
                    .stream(includeExcludeArrangements
                            .lines(line -> line.getTag("name")
                                    .filter(value -> value.contains(caseString)).isPresent())
                            .spliterator(), false)
                    .filter(filter).map(line -> line.getTag("name")).filter(Optional::isPresent)
                    .map(Optional::get).peek(name -> Assert.assertTrue(name.contains("included")))
                    .count());

            Assert.assertEquals(1L, StreamSupport
                    .stream(includeExcludeArrangements
                            .lines(line -> line.getTag("name")
                                    .filter(value -> value.contains(caseString)).isPresent())
                            .spliterator(), false)
                    .filter(filter.negate()).map(line -> line.getTag("name"))
                    .filter(Optional::isPresent).map(Optional::get)
                    .peek(name -> Assert.assertTrue(name.contains("excluded"))).count());
        }
    }

    @Test
    public void testIntersectionPolicy()
    {

        final Atlas testCountsAtlas = this.setup.getTestCounts();
        final Polygon polygon1 = this.getPolygonWithName(testCountsAtlas, "polygon1");
        final Polygon polygon2 = this.getPolygonWithName(testCountsAtlas, "polygon2");
        final long totalPointCount = testCountsAtlas.numberOfPoints();
        final long totalLineCount = testCountsAtlas.numberOfLines();
        final long totalAreaCount = testCountsAtlas.numberOfAreas();
        final long totalRelationCount = testCountsAtlas.numberOfRelations();
        final IntersectionPolicy dudIntersectionPolicy = new IntersectionPolicy()
        {
            // uses all false defaults
        };

        // Test all three policies on the first polygon
        this.assertCounts(
                testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                        .polygons(DEFAULT_INTERSECTION_POLICY, Collections.singleton(polygon1)),
                2L, 3L, 1L, 0L);
        this.assertCounts(testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .polygons(dudIntersectionPolicy, Collections.singleton(polygon1)), 0L, 0L, 0L, 0L);
        this.assertCounts(testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE.polygons(
                FULL_GEOMETRIC_ENCLOSING, Collections.singleton(polygon1)), 2L, 1L, 0L, 0L);
        this.assertCounts(testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE.geometricSurfaces(
                FULL_GEOMETRIC_ENCLOSING, Collections.singleton(polygon1)), 2L, 1L, 0L, 0L);

        // Test all three decision makers on two polygon filter
        this.assertCounts(testCountsAtlas,
                AtlasEntityPolygonsFilter.Type.INCLUDE.polygons(DEFAULT_INTERSECTION_POLICY,
                        Arrays.asList(polygon1, polygon2)),
                totalPointCount - 1L, totalLineCount - 2L, totalAreaCount, totalRelationCount);
        this.assertCounts(testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .polygons(dudIntersectionPolicy, Arrays.asList(polygon1, polygon2)), 0, 0, 0, 0);
        this.assertCounts(testCountsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .polygons(FULL_GEOMETRIC_ENCLOSING, Arrays.asList(polygon1, polygon2)), 2, 1, 0, 0);

        // Test Serializability
        this.assertCounts(testCountsAtlas,
                new FreezeDryFunction<AtlasEntityPolygonsFilter>().apply(
                        AtlasEntityPolygonsFilter.Type.INCLUDE.polygons(FULL_GEOMETRIC_ENCLOSING,
                                Arrays.asList(polygon1, polygon2))),
                2, 1, 0, 0);
    }

    /**
     * Test Multipolygon Functionality
     */
    @Test
    public void testMultiPolygon()
    {

        final Atlas testMultiPolygonFilterAtlas = this.setup.getMultiPolygons();
        final long totalPointCount = testMultiPolygonFilterAtlas.numberOfPoints();
        final long totalLineCount = testMultiPolygonFilterAtlas.numberOfLines();
        final long totalAreaCount = testMultiPolygonFilterAtlas.numberOfAreas();
        final long totalRelationCount = testMultiPolygonFilterAtlas.numberOfRelations();
        final Polygon multiPolygon1Outer1 = this.getPolygonWithName(testMultiPolygonFilterAtlas,
                "multipolygon1-outer1");
        final Polygon multiPolygon1Inner1 = this.getPolygonWithName(testMultiPolygonFilterAtlas,
                "multipolygon1-inner1");
        final Polygon multiPolygon1Inner2 = this.getPolygonWithName(testMultiPolygonFilterAtlas,
                "multipolygon1-inner2");
        final Polygon multiPolygon2Outer1 = this.getPolygonWithName(testMultiPolygonFilterAtlas,
                "multipolygon2-outer1");
        final Polygon multiPolygon2Inner1 = this.getPolygonWithName(testMultiPolygonFilterAtlas,
                "multipolygon2-inner1");
        final MultiMap<Polygon, Polygon> multiPolygonMap1 = new MultiMap<>();
        multiPolygonMap1.add(multiPolygon1Outer1, multiPolygon1Inner1);
        multiPolygonMap1.add(multiPolygon1Outer1, multiPolygon1Inner2);
        final MultiMap<Polygon, Polygon> multiPolygonMap2 = new MultiMap<>();
        multiPolygonMap2.add(multiPolygon2Outer1, multiPolygon2Inner1);
        final MultiPolygon multiPolygon1 = new MultiPolygon(multiPolygonMap1);
        final MultiPolygon multiPolygon2 = new MultiPolygon(multiPolygonMap2);
        final Polygon polygon1 = this.getPolygonWithName(testMultiPolygonFilterAtlas, "polygon1");

        // Multipolygon 1 and 2 include counts
        this.assertCounts(testMultiPolygonFilterAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .multiPolygons(Collections.singleton(multiPolygon1)), 3, 1, 5, 1);
        this.assertCounts(testMultiPolygonFilterAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .multiPolygons(Collections.singleton(multiPolygon2)), 2, 1, 3, 1);

        // Multipolygon 1 and 2 exclude counts
        this.assertCounts(testMultiPolygonFilterAtlas,
                AtlasEntityPolygonsFilter.Type.EXCLUDE
                        .multiPolygons(Collections.singleton(multiPolygon1)),
                totalPointCount - 3, totalLineCount - 1, totalAreaCount - 5,
                totalRelationCount - 1);
        this.assertCounts(testMultiPolygonFilterAtlas,
                AtlasEntityPolygonsFilter.Type.EXCLUDE
                        .multiPolygons(Collections.singleton(multiPolygon2)),
                totalPointCount - 2, totalLineCount - 1, totalAreaCount - 3,
                totalRelationCount - 1);

        // Merged MultiPolygon include is sum of above (except relation is the same between the two)
        this.assertCounts(testMultiPolygonFilterAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .multiPolygons(Arrays.asList(multiPolygon1, multiPolygon2)), 3 + 2, 1 + 1, 5 + 3,
                1);
        this.assertCounts(testMultiPolygonFilterAtlas,
                AtlasEntityPolygonsFilter.Type.INCLUDE
                        .multiPolygons(Collections.singleton(multiPolygon1.merge(multiPolygon2))),
                3 + 2, 1 + 1, 5 + 3, 1);

        // both polygons and multipolygons
        this.assertCounts(testMultiPolygonFilterAtlas,
                AtlasEntityPolygonsFilter.Type.INCLUDE.polygonsAndMultiPolygons(
                        Collections.singleton(polygon1),
                        Arrays.asList(multiPolygon1, multiPolygon2)),
                totalPointCount - 2, totalLineCount - 2, totalAreaCount - 1, totalRelationCount);

    }

    /**
     * Tests the various forms of configurable multipolygons
     */
    @Test
    public void testMultiPolygonForms()
    {
        final Atlas testMultiPolygonFormsAtlas = this.setup.getMultiPolygons();
        final Polygon outerPolygon = this.getPolygonWithName(testMultiPolygonFormsAtlas,
                "multipolygon1-outer1");
        final Polygon innerPolygon1 = this.getPolygonWithName(testMultiPolygonFormsAtlas,
                "multipolygon1-inner1");
        final Polygon innerPolygon2 = this.getPolygonWithName(testMultiPolygonFormsAtlas,
                "multipolygon1-inner2");
        final MultiMap<Polygon, Polygon> multiPolygonMap = new MultiMap<>();
        multiPolygonMap.add(outerPolygon, innerPolygon1);
        multiPolygonMap.add(outerPolygon, innerPolygon2);
        final MultiPolygon multiPolygon1 = new MultiPolygon(multiPolygonMap);

        final String wktConfigurationStringFormat = "{\"filter.multipolygons\":{\"include.wkt\":[\"%s\"]}}";
        final String wkbConfigurationStringFormat = "{\"filter.multipolygons\":{\"include.wkb\":[\"%s\"]}}";
        final String atlasConfigurationStringFormat = "{\"filter.multipolygons\":{\"include.atlas\":[\"%s\"]}}";
        final String geojsonConfigurationStringFormat = "{\"filter.multipolygons\":{\"include.geojson\":[\"%s\"]}}";

        this.assertCounts(testMultiPolygonFormsAtlas,
                this.constructConfiguredFilter(wkbConfigurationStringFormat,
                        WKBWriter.toHex(new WkbMultiPolygonConverter().convert(multiPolygon1))),
                3, 1, -1, -1);
        this.assertCounts(testMultiPolygonFormsAtlas,
                this.constructConfiguredFilter(wktConfigurationStringFormat,
                        new WktMultiPolygonConverter().convert(multiPolygon1)),
                3, 1, -1, -1);
        this.assertCounts(testMultiPolygonFormsAtlas,
                this.constructConfiguredFilter(atlasConfigurationStringFormat,
                        new MultiPolygonStringConverter().backwardConvert(multiPolygon1)),
                3, 1, -1, -1);
        this.assertCounts(testMultiPolygonFormsAtlas,
                this.constructConfiguredFilter(geojsonConfigurationStringFormat, multiPolygon1
                        .asGeoJsonFeatureCollection().toString().replaceAll("\"", "\\\\\"")),
                3, 1, -1, -1);
    }

    @Test
    public void testOverlappingPolygons()
    {
        final Atlas testOverlappingPolygonsAtlas = this.setup.getOverlappingPolygons();
        final Polygon polygon1 = this.getPolygonWithName(testOverlappingPolygonsAtlas, "polygon1");
        final Polygon polygon2 = this.getPolygonWithName(testOverlappingPolygonsAtlas, "polygon2");
        final Polygon polygon3 = this.getPolygonWithName(testOverlappingPolygonsAtlas, "polygon3");
        final Polygon polygon4 = this.getPolygonWithName(testOverlappingPolygonsAtlas, "polygon4");
        final Polygon polygon5 = this.getPolygonWithName(testOverlappingPolygonsAtlas, "polygon5");
        final MultiMap<Polygon, Polygon> multiPolygonMap1 = new MultiMap<>();
        final MultiMap<Polygon, Polygon> multiPolygonMap2 = new MultiMap<>();
        multiPolygonMap1.add(polygon3, polygon4);
        multiPolygonMap2.add(polygon5, polygon2);

        final MultiPolygon multiPolygon1 = new MultiPolygon(multiPolygonMap1);
        final MultiPolygon multiPolygon2 = new MultiPolygon(multiPolygonMap2);

        // Test overlapping polygons
        this.assertCounts(testOverlappingPolygonsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .polygons(Arrays.asList(polygon1, polygon2, polygon3)), 2, 4, -1, -1);
        this.assertCounts(testOverlappingPolygonsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .polygons(Arrays.asList(polygon2, polygon1, polygon3)), 4, 5, -1, -1);
        this.assertCounts(testOverlappingPolygonsAtlas,
                AtlasEntityPolygonsFilter.Type.INCLUDE.polygonsAndMultiPolygons(
                        Arrays.asList(polygon1, polygon2), Collections.singleton(multiPolygon1)),
                2, 4, -1, -1);

        // Test overlapping multipolygons
        this.assertCounts(testOverlappingPolygonsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .multiPolygons(Arrays.asList(multiPolygon1, multiPolygon2)), 0, 3, -1, -1);
        this.assertCounts(testOverlappingPolygonsAtlas, AtlasEntityPolygonsFilter.Type.INCLUDE
                .multiPolygons(Arrays.asList(multiPolygon2, multiPolygon1)), 1, 6, -1, -1);
    }

    private void assertCounts(final Atlas atlas, final Predicate<AtlasEntity> filter,
            final long expectedPointCount, final long expectedLineCount,
            final long expectedAreaCount, final long expectedRelationCount)
    {
        if (expectedPointCount >= 0)
        {
            Assert.assertEquals(expectedPointCount, Iterables.size(atlas.points(filter::test)));
        }
        if (expectedLineCount >= 0)
        {
            Assert.assertEquals(expectedLineCount, Iterables.size(atlas.lines(filter::test)));
        }
        if (expectedAreaCount >= 0)
        {
            Assert.assertEquals(expectedAreaCount, Iterables.size(atlas.areas(filter::test)));
        }
        if (expectedRelationCount >= 0)
        {
            Assert.assertEquals(expectedRelationCount,
                    Iterables.size(atlas.relations(filter::test)));
        }
    }

    private AtlasEntityPolygonsFilter constructConfiguredFilter(final String format,
            final Object... polygonStrings)
    {
        return AtlasEntityPolygonsFilter.forConfiguration(new StandardConfiguration(
                new StringResource(String.format(format, polygonStrings))));
    }

    private Polygon getPolygonWithName(final Atlas atlas, final String name)
    {
        return StreamSupport
                .stream(atlas.areas(area -> area.getTag("name")
                        .filter(string -> string.equals(name)).isPresent()).spliterator(), false)
                .findFirst().get().asPolygon();
    }
}
