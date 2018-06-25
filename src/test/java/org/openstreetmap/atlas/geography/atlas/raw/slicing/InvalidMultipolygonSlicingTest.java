package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;

/**
 * {@link RawAtlasRelationSlicer} unit tests for slicing non-happy path multipolygon relations.
 *
 * @author mgostintsev
 */
public class InvalidMultipolygonSlicingTest
{
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;
    private static Set<String> COUNTRIES;
    private static RawAtlasCountrySlicer RAW_ATLAS_SLICER;

    static
    {
        COUNTRIES = new HashSet<>();
        COUNTRIES.add("CIV");
        COUNTRIES.add("GIN");
        COUNTRIES.add("LBR");

        COUNTRY_BOUNDARY_MAP = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> InvalidMultipolygonSlicingTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        RAW_ATLAS_SLICER = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP);
    }

    @Rule
    public InvalidRelationSlicingTestRule setup = new InvalidRelationSlicingTestRule();

    @Test
    public void testInnerIntersectingOuterRelation()
    {
        // This relation is made up of two lines. Both are closed and on the Liberia side. The two
        // closed lines polygons are overlapping. We expect only country code assignment, no slicing
        // and no merging to take place.
        final Atlas rawAtlas = this.setup.getIntersectingInnerAndOuterMembersAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(8, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        Assert.assertEquals(rawAtlas.numberOfPoints(), slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());
    }

    @Test
    public void testInnerOutsideOuterRelation()
    {
        // This relation is made up two closed lines, both on the Liberia side. However, the inner
        // and outer roles are reversed, causing an invalid multipolygon. We expect that outside of
        // country code assignment, the geometry for the features remains unchanged.
        final Atlas rawAtlas = this.setup.getInnerOutsideOuterRelationAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(8, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        // Nothing should have been sliced, verify identical counts
        Assert.assertEquals(rawAtlas.numberOfPoints(), slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());
    }

    @Test
    public void testInnerWithDifferentCountryCodeThanOuterRelation()
    {
        // TODO consider using enclaves here: http://www.openstreetmap.org/#map=15/51.4394/4.9301
    }

    @Test
    public void testInnerWithoutOuterAcrossBoundary()
    {
        // This relation is made up of a single closed line straddling the Liberia/Ivory Coast
        // country boundary, which is the sole member in the relation with inner role. This is an
        // invalid multipolygon, since it doesn't have an outer. We expect slicing to cut the line
        // into two pieces and create two relations, each containing the newly sliced line as the
        // inner. We then try to build the complex entity and verify that we cannot.
        final Atlas rawAtlas = this.setup.getInnerWithoutOuterAcrossBoundaryAtlas();
        Assert.assertEquals(1, rawAtlas.numberOfLines());
        Assert.assertEquals(4, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        Assert.assertEquals(23, slicedAtlas.numberOfPoints());

        // Line was cut into two pieces, and each relation contains the piece as an inner
        Assert.assertEquals(2, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());
    }

    @Test
    public void testInnerWithoutOuterRelationInOneCountry()
    {
        // This relation is made up of a single closed line inside Liberia, which
        // is the sole member in the relation with inner role. This is an invalid multipolygon,
        // since it doesn't have an outer. We expect slicing to add country codes to all
        // points/lines/relations, but leave the geometry and roles unchanged.
        final Atlas rawAtlas = this.setup.getInnerWithoutOuterInOneCountryAtlas();
        Assert.assertEquals(1, rawAtlas.numberOfLines());
        Assert.assertEquals(4, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        // Nothing should have been sliced, verify identical counts
        Assert.assertEquals(rawAtlas.numberOfPoints(), slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());
    }

    @Test
    public void testOpenMultiPolygonRelationAcrossBoundary()
    {
        // This relation is made up of a single open line straddling the country boundary, which
        // is the sole member in the relation with outer role. This is an invalid multipolygon,
        // since it isn't closed. We expect slicing to add a new point, on the boundary and create a
        // relation for each country, holding a piece of the sliced line in each one.
        final Atlas rawAtlas = this.setup.getOpenMultiPolygonAcrossBoundaryAtlas();
        Assert.assertEquals(1, rawAtlas.numberOfLines());
        Assert.assertEquals(4, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        // We'll have one new point on the boundary and one line and relation on each side of the
        // boundary
        Assert.assertEquals(rawAtlas.numberOfPoints() + 1, slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines() * 2, slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations() * 2, slicedAtlas.numberOfRelations());
    }

    @Test
    public void testOpenMultiPolygonRelationInOneCountry()
    {
        // This relation is made up of a single open line inside Liberia, which
        // is the sole member in the relation with outer role. This is an invalid multipolygon,
        // since it isn't closed. We expect slicing to add country codes to all
        // points/lines/relations, but leave the geometry and roles unchanged.
        final Atlas rawAtlas = this.setup.getOpenMultiPolygonInOneCountryAtlas();
        Assert.assertEquals(1, rawAtlas.numberOfLines());
        Assert.assertEquals(4, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        // Nothing should have been sliced, verify identical counts
        Assert.assertEquals(rawAtlas.numberOfPoints(), slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());
    }

    @Test
    public void testRelationWithOneClosedAndOpenMember()
    {
        // This relation is made up of two lines. The first one is a closed line on the Liberia
        // side. The second is an open line spanning the boundary of Liberia and Ivory Coast. Both
        // lines are outer members in a relation. We expect slicing to leave the closed line and to
        // cut the open line as well as create a new relation on the Ivory Coast side with a piece
        // of the outer open line as a single member.
        final Atlas rawAtlas = this.setup.getRelationWithOneClosedAndOneOpenMemberAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(8, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        Assert.assertEquals(rawAtlas.numberOfPoints() + 1, slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines() + 1, slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations() * 2, slicedAtlas.numberOfRelations());
    }

    @Test
    public void testSelfIntersectingOuterRelationAcrossBoundary()
    {
        // This relation is made up of two lines. The first is a closed line, with an inner role,
        // fully on the Liberia side. The second is a self-intersecting closed outer, stretching
        // across the boundary. We expect the outer to get sliced, the inner to remain unchanged and
        // no merges to take place.
        final Atlas rawAtlas = this.setup
                .getSelfIntersectingOuterMemberRelationAcrossBoundaryAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(10, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we CAN build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertFalse(building.getError().isPresent()));

        Assert.assertEquals(29, slicedAtlas.numberOfPoints());
        Assert.assertEquals(8, slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations() * 2, slicedAtlas.numberOfRelations());
    }

    @Test
    public void testSelfIntersectingOuterRelationInOneCountry()
    {
        // This relation is made up of two lines. Both lines are on the Liberia side. The first is a
        // closed line, with an inner role. The second is a self-intersecting closed outer. We
        // expect no slicing or merging to take place, other than country code assignment.
        final Atlas rawAtlas = this.setup.getSelfIntersectingOuterMemberRelationAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(10, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        // Assert that we CAN build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertFalse(building.getError().isPresent()));

        Assert.assertEquals(rawAtlas.numberOfPoints(), slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());
    }
}
