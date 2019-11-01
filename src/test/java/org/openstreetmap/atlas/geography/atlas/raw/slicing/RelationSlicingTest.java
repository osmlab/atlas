package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * {@link RawAtlasRelationSlicer} unit tests for slicing {@link Relation}s.
 *
 * @author mgostintsev
 */
public class RelationSlicingTest
{
    private static RawAtlasCountrySlicer rawAtlasSlicer;

    private static AtlasLoadingOption loadingOption;

    static
    {
        loadingOption = AtlasLoadingOption.createOptionWithAllEnabled(CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> LineAndPointSlicingTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP)));
        loadingOption.setAdditionalCountryCodes("CIV", "GIN", "LBR");
        rawAtlasSlicer = new RawAtlasCountrySlicer(loadingOption);

    }

    @Rule
    public RelationSlicingTestRule setup = new RelationSlicingTestRule();

    @Test
    public void testDynamicSlicingForMultipleCountries()
    {
        final Map<Shard, Atlas> store;
        store = new HashMap<>();
        store.put(new SlippyTile(15624, 15756, 15), rawAtlasSlicer
                .sliceLines(this.setup.getSingleOuterMultiPolygonSpanningTwoAtlases1()));
        store.put(new SlippyTile(15625, 15756, 15), rawAtlasSlicer
                .sliceLines(this.setup.getSingleOuterMultiPolygonSpanningTwoAtlases2()));

        final RawAtlasCountrySlicer dynamicSlicer = new RawAtlasCountrySlicer(loadingOption,
                new SlippyTileSharding(15), shard ->
                {
                    if (store.containsKey(shard))
                    {
                        return Optional.of(store.get(shard));
                    }
                    else
                    {
                        return Optional.empty();
                    }
                });
        final Atlas slicedAtlas = dynamicSlicer
                .sliceRelations(SlippyTile.forName("15-15625-15756"));

        // check that the original multipolygon relation was removed
        Assert.assertNull(slicedAtlas.relation(214805000000L));
        // check that the original multipolygon relation was sliced into different relations for
        // each country
        Assert.assertEquals(3, slicedAtlas.numberOfRelations());
        Assert.assertEquals(1, Iterables.size(slicedAtlas.relations(relation ->
        {
            return relation.isMultiPolygon()
                    && Validators.isOfType(relation, NaturalTag.class, NaturalTag.WATER)
                    && ISOCountryTag.isIn(IsoCountry.forCountryCode("LBR").get()).test(relation);
        })));
        Assert.assertEquals(1, Iterables.size(slicedAtlas.relations(relation ->
        {
            return relation.isMultiPolygon()
                    && Validators.isOfType(relation, NaturalTag.class, NaturalTag.WATER)
                    && ISOCountryTag.isIn(IsoCountry.forCountryCode("CIV").get()).test(relation);
        })));

        // check that these relations close
        Assert.assertEquals(2,
                Iterables.size(new ComplexWaterEntityFinder().find(slicedAtlas, Finder::ignore)));

        Assert.assertEquals("CIV",
                slicedAtlas.relation(214805001000L).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("LBR",
                slicedAtlas.relation(214805002000L).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("-4281585566310780626",
                slicedAtlas.relation(214805001000L).getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertEquals("4302789678153283194",
                slicedAtlas.relation(214805002000L).getTag(SyntheticRelationMemberAdded.KEY).get());

        // check the non multipolygon relation and make sure it was left intact and tagged with both
        // country codes
        Assert.assertNotNull(slicedAtlas.relation(214806000000L));
        Assert.assertTrue(ISOCountryTag.isIn(IsoCountry.forCountryCode("CIV").get())
                .test(slicedAtlas.relation(214806000000L)));
        Assert.assertTrue(ISOCountryTag.isIn(IsoCountry.forCountryCode("LBR").get())
                .test(slicedAtlas.relation(214806000000L)));
    }

    @Test
    public void testDynamicSlicingForSingleCountry()
    {
        final AtlasLoadingOption civLoadingOption = AtlasLoadingOption.createOptionWithAllEnabled(
                CountryBoundaryMap.fromPlainText(new InputStreamResource(
                        () -> LineAndPointSlicingTest.class.getResourceAsStream(
                                "CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                        .withDecompressor(Decompressor.GZIP)));
        civLoadingOption.setAdditionalCountryCodes("CIV");
        final RawAtlasCountrySlicer civRawAtlasSlicer = new RawAtlasCountrySlicer(civLoadingOption);
        final AtlasLoadingOption lbrLoadingOption = AtlasLoadingOption.createOptionWithAllEnabled(
                CountryBoundaryMap.fromPlainText(new InputStreamResource(
                        () -> LineAndPointSlicingTest.class.getResourceAsStream(
                                "CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                        .withDecompressor(Decompressor.GZIP)));
        lbrLoadingOption.setAdditionalCountryCodes("LBR");
        final RawAtlasCountrySlicer lbrRawAtlasSlicer = new RawAtlasCountrySlicer(lbrLoadingOption);
        final Map<Shard, Atlas> store;
        final Atlas multiAtlas1 = new MultiAtlas(
                civRawAtlasSlicer
                        .sliceLines(this.setup.getSingleOuterMultiPolygonSpanningTwoAtlases1()),
                lbrRawAtlasSlicer
                        .sliceLines(this.setup.getSingleOuterMultiPolygonSpanningTwoAtlases1()));

        final Atlas multiAtlas2 = new MultiAtlas(
                civRawAtlasSlicer
                        .sliceLines(this.setup.getSingleOuterMultiPolygonSpanningTwoAtlases2()),
                lbrRawAtlasSlicer
                        .sliceLines(this.setup.getSingleOuterMultiPolygonSpanningTwoAtlases2()));

        store = new HashMap<>();
        store.put(new SlippyTile(15624, 15756, 15), multiAtlas1);
        store.put(new SlippyTile(15625, 15756, 15), multiAtlas2);

        final RawAtlasCountrySlicer dynamicSlicer = new RawAtlasCountrySlicer(lbrLoadingOption,
                new SlippyTileSharding(15), shard ->
                {
                    if (store.containsKey(shard))
                    {
                        return Optional.of(store.get(shard));
                    }
                    else
                    {
                        return Optional.empty();
                    }
                });
        final Atlas slicedAtlas = dynamicSlicer
                .sliceRelations(SlippyTile.forName("15-15625-15756"));

        // check that the original multipolygon relation was removed
        Assert.assertNull(slicedAtlas.relation(214805000000L));
        // check that the original relation was sliced into different relations for each country
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());
        Assert.assertEquals(1, Iterables.size(slicedAtlas.relations(relation ->
        {
            return relation.isMultiPolygon()
                    && Validators.isOfType(relation, NaturalTag.class, NaturalTag.WATER)
                    && ISOCountryTag.isIn(IsoCountry.forCountryCode("LBR").get()).test(relation);
        })));

        // check that these relations close
        Assert.assertEquals(1,
                Iterables.size(new ComplexWaterEntityFinder().find(slicedAtlas, Finder::ignore)));

        Assert.assertEquals("LBR",
                slicedAtlas.relation(214805002000L).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("4302789678153283194",
                slicedAtlas.relation(214805002000L).getTag(SyntheticRelationMemberAdded.KEY).get());

        // check the non multipolygon relation and make sure it was left intact and tagged with only
        // the country we're slicing
        Assert.assertNotNull(slicedAtlas.relation(214806000000L));
        Assert.assertFalse(ISOCountryTag.isIn(IsoCountry.forCountryCode("CIV").get())
                .test(slicedAtlas.relation(214806000000L)));
        Assert.assertTrue(ISOCountryTag.isIn(IsoCountry.forCountryCode("LBR").get())
                .test(slicedAtlas.relation(214806000000L)));
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

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);

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

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);
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

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);

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

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);

        Assert.assertEquals(31, slicedAtlas.numberOfPoints());
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

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);

        Assert.assertEquals(16, slicedAtlas.numberOfPoints());
        Assert.assertEquals(6, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());

    }

    @Test
    public void testSingleOuterMadeOfOpenLinesSpanningTwoCountriesWithDuplicatePoints()
    {
        // This relation is made up of two open lines, both crossing the country boundary and
        // forming a multi-polygon with one outer.
        final Atlas rawAtlas = this.setup
                .getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlasWithDuplicatePoints();
        Assert.assertEquals(2, rawAtlas.numberOfLines());
        Assert.assertEquals(11, rawAtlas.numberOfPoints());
        Assert.assertEquals(1, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);

        Assert.assertEquals(17, slicedAtlas.numberOfPoints());
        Assert.assertEquals(6, slicedAtlas.numberOfLines());
        Assert.assertEquals(2, slicedAtlas.numberOfRelations());

        slicedAtlas.lines().forEach(line ->
        {
            final Iterator<Location> lineLocations = line.iterator();
            Location previous = lineLocations.next();
            while (lineLocations.hasNext())
            {
                final Location current = lineLocations.next();
                Assert.assertFalse(current.equals(previous));
                previous = current;
            }
        });
    }

    @Test
    public void testSlicingOnRelationWithOnlyRelationsAsMembers()
    {
        final Atlas rawAtlas = this.setup.getRelationWithOnlyRelationsAsMembers();

        Assert.assertEquals(2, rawAtlas.numberOfPoints());
        Assert.assertEquals(3, rawAtlas.numberOfRelations());

        final Atlas slicedAtlas = rawAtlasSlicer.slice(rawAtlas);
        for (final Relation relation : slicedAtlas.relations())
        {
            final Map<String, String> tags = relation.getTags();

            Assert.assertTrue(tags.containsKey(ISOCountryTag.KEY));
            Assert.assertNotEquals(ISOCountryTag.COUNTRY_MISSING, tags.get(ISOCountryTag.KEY));
        }
    }
}
