package org.openstreetmap.atlas.geography.atlas.raw;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.WaySectionProcessor;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Tests OSM PBF --> Raw Atlas --> Sliced Raw Atlas flow.
 *
 * @author mgostintsev
 */
public class OsmPbfToSlicedRawAtlasTest
{
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;
    private static Set<IsoCountry> COUNTRIES;

    static
    {
        COUNTRIES = new HashSet<>();
        COUNTRIES.add(IsoCountry.forCountryCode("CIV").get());
        COUNTRIES.add(IsoCountry.forCountryCode("GIN").get());
        COUNTRIES.add(IsoCountry.forCountryCode("LBR").get());

        COUNTRY_BOUNDARY_MAP = new CountryBoundaryMap(
                new InputStreamResource(() -> OsmPbfToSlicedRawAtlasTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
    }

    @Test
    public void testPbfToCountrySlicedAndWaySectionedAtlas()
    {
        final String path = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);

        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        System.out.println(finalAtlas.summary());

        // TODO - compare the old atlas to the new atlas to see differences
    }

    @Test
    public void testPbfToSlicedAtlasWithExpansion()
    {
        // TODO
    }

    @Test
    public void testPbfToSlicedRawAtlas()
    {
        // This PBF file contains really interesting data. 1. MultiPolygon with multiple inners and
        // outers spanning 3 countries (http://www.openstreetmap.org/relation/3638082) 2. Multiple
        // nested relations (http://www.openstreetmap.org/relation/3314886)
        final String path = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        Assert.assertEquals(0, rawAtlas.numberOfNodes());
        Assert.assertEquals(0, rawAtlas.numberOfEdges());
        Assert.assertEquals(0, rawAtlas.numberOfAreas());
        Assert.assertEquals(646583, rawAtlas.numberOfPoints());
        Assert.assertEquals(57133, rawAtlas.numberOfLines());
        Assert.assertEquals(36, rawAtlas.numberOfRelations());

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);

        Assert.assertEquals(0, slicedRawAtlas.numberOfNodes());
        Assert.assertEquals(0, slicedRawAtlas.numberOfEdges());
        Assert.assertEquals(0, slicedRawAtlas.numberOfAreas());
        Assert.assertEquals(648704, slicedRawAtlas.numberOfPoints());
        Assert.assertEquals(57644, slicedRawAtlas.numberOfLines());
        Assert.assertEquals(44, slicedRawAtlas.numberOfRelations());

        // Assert all Raw Atlas Entities have a country code
        slicedRawAtlas.lines().forEach(line ->
        {
            Assert.assertTrue(Validators.hasValuesFor(line, ISOCountryTag.class));
        });
        slicedRawAtlas.points().forEach(point ->
        {
            Assert.assertTrue(Validators.hasValuesFor(point, ISOCountryTag.class));
        });
        slicedRawAtlas.relations().forEach(point ->
        {
            Assert.assertTrue(Validators.hasValuesFor(point, ISOCountryTag.class));
        });

        // Previous PBF-to-Atlas Implementation - let's compare the two results
        final String pbfPath = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
                .getPath();
        final OsmPbfLoader loader = new OsmPbfLoader(new File(pbfPath), AtlasLoadingOption
                .createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP).setWaySectioning(false));
        final Atlas oldSlicedAtlas = loader.read();

        Assert.assertTrue(
                "The original Atlas counts of (Lines + Master Edges + Areas), without way-sectioning, should be"
                        + "equal to the total number of all Lines in the Raw Atlas (+1 for relation handling).",
                Iterables.size(Iterables.filter(oldSlicedAtlas.edges(), Edge::isMasterEdge))
                        + oldSlicedAtlas.numberOfAreas()
                        + oldSlicedAtlas.numberOfLines() == slicedRawAtlas.numberOfLines() - 1);
    }
}
