package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;

import com.google.common.collect.Iterables;

/**
 * {@link RawAtlasCountrySlicer} unit tests for slicing Relations.
 *
 * @author mgostintsev
 */
public class RawAtlasRelationSlicingTest
{
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;
    private static Set<IsoCountry> COUNTRIES;

    static
    {
        COUNTRIES = new HashSet<>();
        COUNTRIES.add(IsoCountry.forCountryCode("CIV").get());
        COUNTRIES.add(IsoCountry.forCountryCode("GIN").get());
        COUNTRIES.add(IsoCountry.forCountryCode("LBR").get());
    }

    @Rule
    public RelationSlicingTestRule setup = new RelationSlicingTestRule();

    @BeforeClass
    public static void setup()
    {
        COUNTRY_BOUNDARY_MAP = new CountryBoundaryMap(
                new InputStreamResource(() -> RawAtlasLineAndPointSlicingTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
    }

    @Test
    public void testComplexRelationInsideSingleCountry()
    {
    }

    @Test
    public void testComplexRelationSpanningTwoCountries()
    {

    }

    @Test
    public void testComplexRelationWithHoleInsideSingleCountry()
    {

    }

    // TODO - test testComplexRelationWithHoleSpanningTwoCountries with closed lines instead of open
    // TODO - test with multiple inners across boundary
    // TODO - test with relations of relations

    @Test
    public void testComplexRelationWithHoleSpanningTwoCountries()
    {
        // This relation is made up of non-closed lines, tied together by a relation to create a
        // MultiPolygon with the outer spanning two countries and the inner fully inside one
        // country.
        final Atlas rawAtlas = this.setup.getComplexMultiPolygonWithHoleAtlas();

        System.out.println(rawAtlas);

        Assert.assertEquals(4, rawAtlas.numberOfLines());
        Assert.assertEquals(9, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(rawAtlas, COUNTRIES,
                COUNTRY_BOUNDARY_MAP);
        final Atlas slicedAtlas = slicer.slice();

        System.out.println(slicedAtlas);

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
    public void testMultiPolygonRelationSpanningTwoCountries()
    {
        // This relation is made up of three closed lines, each serving as an outer to a
        // multipolygon relation. Two of the outers span the border of two countries, while one is
        // entirely within a country.
        final Atlas rawAtlas = this.setup.getSimpleMultiPolygonAtlas();
        Assert.assertEquals(3, rawAtlas.numberOfLines());
        Assert.assertEquals(12, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        System.out.println(rawAtlas);

        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(rawAtlas, COUNTRIES,
                COUNTRY_BOUNDARY_MAP);
        final Atlas slicedAtlas = slicer.slice();

        Assert.assertEquals(27, slicedAtlas.numberOfPoints());
        Assert.assertEquals(5, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());

        System.out.println(slicedAtlas);

    }

    @Test
    public void testSimpleRelationInsideSingleCountry()
    {

    }

    @Test
    public void testSimpleRelationWithHoleInsideSingleCountry()
    {

    }

    @Test
    public void testSimpleRelationWithHoleSpanningTwoCountries()
    {
        // This relation is made up of two closed lines, forming a multi-polygon with one inner and
        // one outer, both spanning the boundary of two countries.
        final Atlas rawAtlas = this.setup.getSimpleMultiPolygonWithHoleAtlas();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(8, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(rawAtlas, COUNTRIES,
                COUNTRY_BOUNDARY_MAP);
        final Atlas slicedAtlas = slicer.slice();

        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> System.out.println(building));

        Assert.assertFalse("", slicedAtlas.numberOfPoints() == 22);
        Assert.assertEquals(29, slicedAtlas.numberOfPoints());
        Assert.assertEquals(2, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());
    }
}
