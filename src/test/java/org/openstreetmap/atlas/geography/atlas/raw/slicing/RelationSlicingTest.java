package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * {@link RawAtlasRelationSlicer} unit tests for slicing {@link Relation}s.
 *
 * @author mgostintsev
 */
public class RelationSlicingTest
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
    public RelationSlicingTestRule setup = new RelationSlicingTestRule();

    // TODO - test with multiple inners across boundary

    @Test
    public void testMultiPolygonRelationSpanningTwoCountries()
    {
        // This relation is made up of three closed lines, each serving as an outer to a
        // multipolygon relation. Two of the outers span the border of two countries, while one is
        // entirely within a country.
        final Atlas rawAtlas = this.setup.getSimpleMultiPolygonAtlas();
        Assert.assertEquals(3, rawAtlas.numberOfLines());
        Assert.assertEquals(12, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        Assert.assertEquals(27, slicedAtlas.numberOfPoints());
        Assert.assertEquals(5, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());
    }

    @Test
    public void testMultiPolygonWithClosedLinesSpanningTwoCountries()
    {
        // This relation is made up of closed lines, tied together by a relation, to create a
        // MultiPolygon with the outer spanning two countries and the inner fully inside one
        // country.
        final Atlas rawAtlas = this.setup.getComplexMultiPolygonWithHoleUsingClosedLinesAtlas();

        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(9, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        Assert.assertEquals(3, slicedAtlas.numberOfLines());
        Assert.assertEquals(14, slicedAtlas.numberOfPoints());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());

        // Just for fun (and to validate the sliced multi-polygon validity) - create Complex
        // Entities and make sure they are valid.
        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(slicedAtlas, Finder::ignore);
        Assert.assertEquals(2, Iterables.size(waterEntities));
    }

    @Test
    public void testMultiPolygonWithOpenLinesSpanningTwoCountries()
    {
        // This relation is made up of open lines, tied together by a relation to create a
        // MultiPolygon with the outer spanning two countries and the inner fully inside one
        // country.
        final Atlas rawAtlas = this.setup.getComplexMultiPolygonWithHoleUsingOpenLinesAtlas();

        Assert.assertEquals(4, rawAtlas.numberOfLines());
        Assert.assertEquals(9, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        Assert.assertEquals(8, slicedAtlas.numberOfLines());
        Assert.assertEquals(14, slicedAtlas.numberOfPoints());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());

        // Just for fun (and to validate the sliced multi-polygon validity) - create Complex
        // Entities and make sure they are valid.
        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(slicedAtlas, Finder::ignore);
        Assert.assertEquals(2, Iterables.size(waterEntities));
    }

    @Test
    public void testSimpleMultiPolygonWithHoleSpanningTwoCountries()
    {
        // This relation is made up of two closed lines, forming a multi-polygon with one inner and
        // one outer, both spanning the boundary of two countries.
        final Atlas rawAtlas = this.setup.getSimpleMultiPolygonWithHoleAtlas();

        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(8, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);
        new ComplexBuildingFinder().find(slicedAtlas).forEach(System.out::println);

        Assert.assertEquals(23, slicedAtlas.numberOfPoints());
        Assert.assertEquals(2, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());
    }

    @Test
    public void testSingleOuterMadeOfOpenLinesSpanningTwoCountries()
    {
        // This relation is made up of two open lines, both crossing the country boundary and
        // forming a multi-polygon with one outer.
        final Atlas rawAtlas = this.setup.getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(9, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = RAW_ATLAS_SLICER.slice(rawAtlas);

        Assert.assertEquals(16, slicedAtlas.numberOfPoints());
        Assert.assertEquals(6, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());
    }
}
