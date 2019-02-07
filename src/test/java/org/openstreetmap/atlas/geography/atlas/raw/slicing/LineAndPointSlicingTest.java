package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PointIdentifierFactory;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;

/**
 * {@link RawAtlasPointAndLineSlicer} unit tests for slicing Lines and Points.
 *
 * @author mgostintsev
 */
public class LineAndPointSlicingTest
{
    private static RawAtlasCountrySlicer RAW_ATLAS_SLICER;
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;
    private static Set<String> COUNTRIES;

    static
    {
        COUNTRIES = new HashSet<>();
        COUNTRIES.add("CIV");
        COUNTRIES.add("GIN");
        COUNTRIES.add("LBR");

        COUNTRY_BOUNDARY_MAP = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> LineAndPointSlicingTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        RAW_ATLAS_SLICER = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP);
    }

    @Rule
    public LineAndPointSlicingTestRule setup = new LineAndPointSlicingTestRule();

    @Test
    public void testClosedLineInsideSingleCountry()
    {
        final Atlas rawAtlas = this.setup.getClosedLineFullyInOneCountryAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        rawAtlas.lines().forEach(line -> Assert.assertTrue("The line is closed", line.isClosed()));
        Assert.assertEquals("The line was untouched", 1, slicedAtlas.numberOfLines());
        slicedAtlas.lines()
                .forEach(line -> Assert.assertTrue("The line is still closed", line.isClosed()));
        Assert.assertEquals("The line should have an Guinea country code", "GIN",
                slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Four points exist in the raw Atlas", 4, rawAtlas.numberOfPoints());
        Assert.assertEquals("The same 4 exist in the sliced Atlas", 4,
                slicedAtlas.numberOfPoints());

        for (final Point point : slicedAtlas.points())
        {
            Assert.assertEquals("Eeach point should have an Guinea country code", "GIN",
                    point.getTag(ISOCountryTag.KEY).get());
            Assert.assertFalse("None of the points should have a synthetic boundary tag",
                    point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
        }
    }

    @Test
    public void testClosedLineSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getClosedLineSpanningTwoCountriesAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        rawAtlas.lines().forEach(line -> Assert.assertTrue("The line is closed", line.isClosed()));
        Assert.assertEquals("The line was cut into 2 segments", 2, slicedAtlas.numberOfLines());
        slicedAtlas.lines()
                .forEach(line -> Assert.assertTrue("Both segments are closed", line.isClosed()));

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        Assert.assertEquals("Expect the first segment to have Ivory Coast country code", "CIV",
                slicedAtlas.line(lineIdentifierFactory.nextIdentifier()).getTag(ISOCountryTag.KEY)
                        .get());
        Assert.assertEquals("Expect the second segment to have Liberia country code", "LBR",
                slicedAtlas.line(lineIdentifierFactory.nextIdentifier()).getTag(ISOCountryTag.KEY)
                        .get());

        // Check Point correctness
        Assert.assertEquals("Four points exist in the raw Atlas", 4, rawAtlas.numberOfPoints());
        Assert.assertEquals("Twelve points exist in the sliced Atlas", 12,
                slicedAtlas.numberOfPoints());

        for (final Point point : slicedAtlas.points())
        {
            if (point.getIdentifier() == 1 || point.getIdentifier() == 2)
            {
                Assert.assertEquals("Expect two points to be fully inside Ivory Coast border",
                        "CIV", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertFalse("None of the points should have a synthetic boundary tag",
                        point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            }
            else if (point.getIdentifier() == 3 || point.getIdentifier() == 4)
            {
                Assert.assertEquals("Expect the other two points to be fully inside Liberia border",
                        "LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertFalse("None of the points should have a synthetic boundary tag",
                        point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            }
            else
            {
                Assert.assertEquals(
                        "Expect all new points to be on the country boundary and have both country codes",
                        "CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals("All new points are non-existing synthetic boundary nodes",
                        SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
            }
        }
    }

    @Test
    public void testCreatingExistingSyntheticBoundaryNode()
    {
        final Atlas rawAtlas = this.setup.getRoadAcrossTwoCountriesWithPointOnBorderAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        Assert.assertEquals("The line got cut into two pieces ", 2, slicedAtlas.numberOfLines());
        Assert.assertNull("Original line should be removed from the Atlas", slicedAtlas.line(1));

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Line firstCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check first segment exists", firstCreatedLine);
        Assert.assertEquals("Expect the first segment to be on the Ivory Coast side", "CIV",
                firstCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line secondCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check second segment exists", secondCreatedLine);
        Assert.assertEquals("Expect the second segment to be on the Liberia side", "LBR",
                secondCreatedLine.getTag(ISOCountryTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Three points exist in the raw Atlas", 3, rawAtlas.numberOfPoints());
        Assert.assertEquals("Three points exist in the sliced Atlas", 3,
                slicedAtlas.numberOfPoints());

        final long pointOnBoundaryIdentifier = 2;
        slicedAtlas.points().forEach(point ->
        {
            if (point.getIdentifier() == pointOnBoundaryIdentifier)
            {
                // Make specific checks for the boundary points
                Assert.assertNotNull("Check that new point exists in sliced Atlas", point);
                Assert.assertEquals("Expect the Point on the border to contain both country codes",
                        "CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals(
                        "Expect the Point to have the synethetic boundary node tag value of existing",
                        SyntheticBoundaryNodeTag.EXISTING.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
            }
            else
            {
                // The other points point should have country codes and nothing else
                Assert.assertTrue("Expect all existing points to have an iso country code tag",
                        point.getTag(ISOCountryTag.KEY).isPresent());
                Assert.assertFalse(
                        "None of the existing points should have a synthetic boundary tag",
                        point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            }
        });
    }

    @Test
    public void testCreatingNewSyntheticBoundaryNode()
    {
        final Atlas rawAtlas = this.setup.getRoadAcrossTwoCountriesAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        Assert.assertEquals("The line got cut into two pieces ", 2, slicedAtlas.numberOfLines());
        Assert.assertNull("Original line should be removed from the Atlas", slicedAtlas.line(1));

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final long firstLineId = lineIdentifierFactory.nextIdentifier();

        final Line firstCreatedLine = slicedAtlas.line(firstLineId);
        Assert.assertNotNull("Check new way addition", firstCreatedLine);
        Assert.assertEquals("Expect the first segment to be on the Ivory Coast side", "CIV",
                firstCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line secondCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", secondCreatedLine);
        Assert.assertEquals("Expect the second segment to be on the Liberia side", "LBR",
                secondCreatedLine.getTag(ISOCountryTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Two points exist in the raw Atlas", 2, rawAtlas.numberOfPoints());
        Assert.assertEquals("Three points exist in the sliced Atlas", 3,
                slicedAtlas.numberOfPoints());

        final PointIdentifierFactory pointIdentifierFactory = new PointIdentifierFactory(
                firstLineId);
        final long newPointIdentifier = pointIdentifierFactory.nextIdentifier();
        slicedAtlas.points().forEach(point ->
        {
            System.out.println(point);
            if (point.getIdentifier() == newPointIdentifier)
            {
                // Make specific checks for the new added point
                Assert.assertNotNull("Check that new point exists in sliced Atlas", point);
                Assert.assertEquals(
                        "Expect the new Point on the border to contain both country codes",
                        "CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals(
                        "Expect the new Point to have the synethetic boundary node tag value of yes",
                        SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
            }
            else
            {
                // The existing points should only have country code updates
                Assert.assertTrue("Expect all existing points to have an iso country code tag",
                        point.getTag(ISOCountryTag.KEY).isPresent());
                Assert.assertFalse(
                        "None of the existing points should have a synthetic boundary tag",
                        point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            }
        });
    }

    @Test
    public void testLineFullyInsideOneCountry()
    {
        final Atlas rawAtlas = this.setup.getRoadFullyInOneCountryAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        Assert.assertEquals("The line was untouched after cutting", 1, slicedAtlas.numberOfLines());
        Assert.assertEquals("Expect the line to have Ivory Coast country code", "CIV",
                slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Two points exist in the raw Atlas", 2, rawAtlas.numberOfPoints());
        Assert.assertEquals("Two points exist in the sliced Atlas", 2,
                slicedAtlas.numberOfPoints());

        slicedAtlas.points().forEach(point ->
        {
            Assert.assertEquals("Expect all points to be fully inside Ivory Coast border", "CIV",
                    point.getTag(ISOCountryTag.KEY).get());
            Assert.assertFalse("None of the points should have a synthetic boundary tag",
                    point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
        });
    }

    @Test
    public void testLineTouchingBoundary()
    {
        final Atlas rawAtlas = this.setup.getRoadTouchingBoundaryAtlas();
        final Atlas slicedAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        Assert.assertEquals("The line was untouched after cutting", 1, slicedAtlas.numberOfLines());
        Assert.assertEquals("Expect the line to have Ivory Coast country code", "CIV",
                slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Two points exist in the raw Atlas", 2, rawAtlas.numberOfPoints());
        Assert.assertEquals("Two points exist in the sliced Atlas", 2,
                slicedAtlas.numberOfPoints());

        final Point fullyInIvoryCoast = slicedAtlas.point(1);
        Assert.assertEquals("Expect point to be fully inside Ivory Coast border", "CIV",
                fullyInIvoryCoast.getTag(ISOCountryTag.KEY).get());
        Assert.assertFalse("Point should not have a synthetic boundary tag",
                fullyInIvoryCoast.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());

        final Point exactlyOnBorder = slicedAtlas.point(2);
        Assert.assertEquals("Expect point on border to have two country codes", "CIV,LBR",
                exactlyOnBorder.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("Point should have an existing synthetic boundary tag",
                SyntheticBoundaryNodeTag.EXISTING.toString(),
                exactlyOnBorder.getTag(SyntheticBoundaryNodeTag.KEY).get());
    }

    @Test
    public void testLineWeavingAcrossBoundary()
    {
        final Atlas rawAtlas = this.setup.getRoadWeavingAlongBoundaryAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        Assert.assertEquals("There should be 4 Lines as a result of 3 cuts", 4,
                slicedAtlas.numberOfLines());
        Assert.assertNull("Original line should be removed from the Atlas", slicedAtlas.line(1));

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Line firstCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", firstCreatedLine);
        Assert.assertEquals("Expect the first segment to be on the Ivory Coast side", "CIV",
                firstCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line secondCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", secondCreatedLine);
        Assert.assertEquals("Expect the second segment to be on the Ivory Coast side", "CIV",
                secondCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line thirdCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", thirdCreatedLine);
        Assert.assertEquals("Expect the third segment to be on the Liberia side", "LBR",
                thirdCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line fourthCreatedLine = slicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", fourthCreatedLine);
        Assert.assertEquals("Expect the fourth segment to be on the Liberia side", "LBR",
                fourthCreatedLine.getTag(ISOCountryTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Two points exist in the raw Atlas", 5, rawAtlas.numberOfPoints());
        Assert.assertEquals("Original 5 points plus the 3 new Points created at the boundary", 8,
                slicedAtlas.numberOfPoints());

        final CountrySlicingIdentifierFactory pointIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);
        long newPointIdentifier = pointIdentifierFactory.nextIdentifier();

        for (final Point point : slicedAtlas.points())
        {
            if (point.getIdentifier() == newPointIdentifier)
            {
                Assert.assertEquals(
                        "Expect all new points to be on the country boundary and have both country codes",
                        "CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals("All new points are non-existing synthetic boundary nodes",
                        SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                newPointIdentifier = pointIdentifierFactory.nextIdentifier();
            }
        }
    }

    @Test
    public void testNearestNeighborTagAssignment()
    {
        final Atlas rawAtlas = this.setup.getRoadOutsideAllBoundariesAtlas();
        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Check Line correctness
        Assert.assertEquals("A single line exists in the raw Atlas", 1, rawAtlas.numberOfLines());
        Assert.assertEquals("The line was untouched after cutting", 1, slicedAtlas.numberOfLines());
        Assert.assertEquals(
                "Expect the line to have GUINEA country code, as that's the closest boundary available",
                "GIN", slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("Expect a nearest neighbor country code tag to be present",
                SyntheticNearestNeighborCountryCodeTag.YES.toString(),
                slicedAtlas.line(1).getTag(SyntheticNearestNeighborCountryCodeTag.KEY).get());

        // Check Point correctness
        Assert.assertEquals("Two points exist in the raw Atlas", 2, rawAtlas.numberOfPoints());
        Assert.assertEquals("Two points exist in the sliced Atlas", 2,
                slicedAtlas.numberOfPoints());

        slicedAtlas.points().forEach(point ->
        {
            Assert.assertEquals("Expect all existing points to have GUINEA country code", "GIN",
                    point.getTag(ISOCountryTag.KEY).get());
            Assert.assertTrue(
                    "Expect a nearest neighbor country code tag to be present for all points",
                    point.getTag(SyntheticNearestNeighborCountryCodeTag.KEY).isPresent());
        });
    }
}
