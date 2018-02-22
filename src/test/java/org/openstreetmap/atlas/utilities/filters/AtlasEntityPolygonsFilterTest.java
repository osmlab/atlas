package org.openstreetmap.atlas.utilities.filters;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.converters.PolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.WkbPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

import com.vividsolutions.jts.io.WKBWriter;

/**
 * Tests for {@link AtlasEntityPolygonsFilter}
 *
 * @author jklamer
 */
public class AtlasEntityPolygonsFilterTest
{
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
                .constructFilter(includeConfigurationFormat, polygon1.toWkt());
        final AtlasEntityPolygonsFilter includePolygon2 = this
                .constructFilter(includeConfigurationFormat, polygon2.toWkt());
        final AtlasEntityPolygonsFilter includePolygon1And2 = this.constructFilter(
                includeConfigurationFormat,
                String.join("\",\"", polygon1.toWkt(), polygon2.toWkt()));
        final AtlasEntityPolygonsFilter excludePolygon1 = this
                .constructFilter(excludeConfigurationFormat, polygon1.toWkt());
        final AtlasEntityPolygonsFilter excludePolygon2 = this
                .constructFilter(excludeConfigurationFormat, polygon2.toWkt());
        final AtlasEntityPolygonsFilter excludePolygon1And2 = this.constructFilter(
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

    }

    /**
     * Tests every input form supported by the filter
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

        this.assertCounts(this.setup.getTestForm(),
                this.constructFilter(wktConfigurationStringFormat, includeBoundary.toWkt()), 2, 2,
                2, 0);
        this.assertCounts(this.setup.getTestForm(),
                this.constructFilter(wkbConfigurationStringFormat,
                        WKBWriter.toHex(new WkbPolygonConverter().convert(includeBoundary))),
                2, 2, 2, 0);
        this.assertCounts(this.setup.getTestForm(),
                this.constructFilter(atlasConfigurationStringFormat,
                        new PolygonStringConverter().backwardConvert(includeBoundary)),
                2, 2, 2, 0);
        this.assertCounts(this.setup.getTestForm(),
                this.constructFilter(geojsonConfigurationStringFormat,
                        includeBoundary.asGeoJson().toString().replaceAll("\"", "\\\\\"")),
                2, 2, 2, 0);
    }

    /**
     * Tests every arrangement of 1 include and 1 exclude polygon. Each situation the exclude
     * polygon is either dropped for overlapping or ignored because of the include dominance of the
     * filter. Each situation designed to have same count result.
     */
    @Test
    public void testIncludeDominant()
    {
        final int numberOfCases = 4;
        final Atlas includeExcludeArrangements = this.setup.getIncludeExcludeArrangements();
        final String configurationStringFormat = "{\"filter.polygons\":{\"include.wkt\":[\"%s\"],\"exclude.wkt\":[\"%s\"]}}";
        final Polygon includePolygon = this.getPolygonWithName(includeExcludeArrangements,
                "include");

        for (int caseNum = 1; caseNum <= numberOfCases; caseNum++)
        {
            final String caseString = String.format("case_%d", caseNum);
            final String excludePolygonName = "exclude_".concat(caseString);
            final Polygon excludePolygon = this.getPolygonWithName(includeExcludeArrangements,
                    excludePolygonName);
            final AtlasEntityPolygonsFilter filter = this.constructFilter(configurationStringFormat,
                    includePolygon.toWkt(), excludePolygon.toWkt());

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

    private AtlasEntityPolygonsFilter constructFilter(final String format,
            final Object... polygonStrings)
    {
        return new AtlasEntityPolygonsFilter(new StandardConfiguration(
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
