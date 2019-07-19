package org.openstreetmap.atlas.geography.boundary;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.ConfiguredTaggableFilter;
import org.openstreetmap.atlas.test.TestUtility;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link CountryBoundaryMap}.
 *
 * @author tony
 * @author Yiqing Jin
 * @author mgostintsev
 */
public class CountryBoundaryMapTest
{
    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMapTest.class);
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();

    @Test
    public void readGridIndexFromBoundaryFile() throws ParseException
    {
        // Common code that will be re-used below
        final WKTReader reader = new WKTReader();
        final Rectangle rectangleInMAF = Rectangle.forLocations(Location.forString("18.09, -63.06"),
                Location.forString("18.08, -63.04"));

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addLine(1L, rectangleInMAF, new HashMap<String, String>());
        final Atlas rawAtlas = builder.get();

        // Read the serialized Country Boundary Map and Grid Index from file. Then try slicing a
        // feature with the pre-built index.
        final Time start = Time.now();
        final CountryBoundaryMap mapWithGridIndex = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertTrue(mapWithGridIndex.hasGridIndex());
        final RawAtlasCountrySlicer slicerWithPrebuildIndex = new RawAtlasCountrySlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(mapWithGridIndex));

        final Atlas slicedAtlas = slicerWithPrebuildIndex.sliceLines(rawAtlas);
        logger.info("It took {} to slice using serialized pre-built grid index",
                start.elapsedSince());

        // Construct the grid index on the fly
        final Time start2 = Time.now();
        final CountryBoundaryMap mapFromOsmTextFile = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(mapFromOsmTextFile.hasGridIndex());
        mapFromOsmTextFile.initializeGridIndex(mapFromOsmTextFile.getLoadedCountries());
        Assert.assertTrue(mapFromOsmTextFile.hasGridIndex());

        final RawAtlasCountrySlicer slicerWithOnTheFlyIndex = new RawAtlasCountrySlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(mapWithGridIndex));

        final Atlas reslicedAtlas = slicerWithOnTheFlyIndex.sliceLines(rawAtlas);
        logger.info("It took {} to slice using constructed grid index", start2.elapsedSince());

        // Make sure the slice results are identical
        reslicedAtlas.lines().forEach(
                slicedLine -> reslicedAtlas.line(slicedLine.getIdentifier()).equals(slicedLine));

        // Validate that it took less time to read in the grid index and slice than to create the
        // grid index on the fly.
        Assert.assertTrue(start2.getEpoch().isMoreThan(start.getEpoch()));
    }

    @Test
    public void testAntiMeridian()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());
        final LineString lineString = (LineString) TestUtility
                .createJtsGeometryFromWKT("LINESTRING ( -179 18.84927, 179 18.84927 )");

        // HTI is the closest to the line
        Assert.assertEquals("HTI", map.getCountryCodeISO3(lineString).getIso3CountryCode());
    }

    @Test
    public void testBorderDeduplication()
    {
        final InputStreamResource atlasResource = new InputStreamResource(
                () -> CountryBoundaryMapTest.class
                        .getResourceAsStream("USA_HTI_overlapping.atlas.txt"));
        final Atlas atlas = new TextAtlasBuilder().read(atlasResource);
        final CountryBoundaryMap map = CountryBoundaryMap.fromAtlas(atlas);
        final StringResource boundaryHTI = new StringResource(new InputStreamResource(
                () -> CountryBoundaryMapTest.class.getResourceAsStream("HTI_boundary.txt")));
        final StringResource boundaryUSA = new StringResource(
                new InputStreamResource(() -> CountryBoundaryMapTest.class
                        .getResourceAsStream("USA_boundary_reduced.txt")));
        // confirm the duplicated border belongs to the USA
        Assert.assertEquals(boundaryUSA.all(),
                map.countryBoundary("USA").get(0).getBoundary().toString());
        // confirm the duplicated border does not belong to HTI
        Assert.assertEquals(boundaryHTI.all(),
                map.countryBoundary("HTI").get(0).getBoundary().toString());
    }

    @Test
    public void testBoundaryLoading() throws ParseException
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("CIV_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());
        Assert.assertEquals("CIV", firstCountryName(map));

        final Location locationInsideInner1 = Location.forString("4.5847047, -7.573053");
        final Location locationInsideInner2 = Location.forString("4.5828105, -7.572514");
        final PolyLine polyLineInsideInner = new PolyLine(locationInsideInner1,
                locationInsideInner2);
        Assert.assertEquals(1, map.boundaries(locationInsideInner1).size());
        Assert.assertEquals(1, map.boundaries(polyLineInsideInner).size());

        final Location locationBetweenInnerAndOuter1 = Location.forString("4.5842728, -7.57121");
        final Location locationBetweenInnerAndOuter2 = Location.forString("4.5848686, -7.567427");
        final PolyLine polyLineAcrossInner = new PolyLine(locationInsideInner1,
                locationBetweenInnerAndOuter1);
        final PolyLine polyLineBetweenInnerAndOuter = new PolyLine(locationBetweenInnerAndOuter2,
                locationBetweenInnerAndOuter1);
        Assert.assertEquals(1, map.boundaries(locationBetweenInnerAndOuter1).size());
        Assert.assertEquals(1, map.boundaries(polyLineAcrossInner).size());
        Assert.assertEquals(1, map.boundaries(polyLineBetweenInnerAndOuter).size());

        final Location locationOutsideOuter1 = Location.forString("4.6002906, -7.5696683");
        final Location locationOutsideOuter2 = Location.forString("4.610689, -7.5605944");
        final PolyLine polyLineAcrossOuter = new PolyLine(locationOutsideOuter1,
                locationBetweenInnerAndOuter1);
        final PolyLine polyLineOuter = new PolyLine(locationOutsideOuter1, locationOutsideOuter2);
        Assert.assertEquals(0, map.boundaries(locationOutsideOuter1).size());
        Assert.assertEquals(0, map.boundaries(locationOutsideOuter2).size());
        Assert.assertEquals(1, map.boundaries(polyLineAcrossOuter).size());
        Assert.assertEquals(0, map.boundaries(polyLineOuter).size());
    }

    @Test(expected = CoreException.class)
    public void testDuplicateBoundary() throws URISyntaxException
    {
        // ABC and DEF has the same boundaries
        final Set<String> countries = new HashSet<>();
        countries.add("ABC");
        countries.add("DEF");

        // Read from shape file
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap.fromShapeFile(new File(
                CountryBoundaryMapTest.class.getResource("duplicate_shape.shp").getFile()));
        Assert.assertFalse(boundaryMap.hasGridIndex());

        // Initialize grid index
        boundaryMap.initializeGridIndex(countries);
        Assert.assertTrue(boundaryMap.hasGridIndex());
    }

    @Test
    public void testFeatureCrossingCountryBoundary() throws ParseException
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());

        final WKTReader reader = new WKTReader();
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        final PolyLine geometry = PolyLine.wkt(
                "LINESTRING ( -71.7424191 18.7499411097, -71.730485136 18.749848501, -71.730081575 18.749979671, -71.730142154 18.749575218, -71.730486015 18.7498444, -71.7424191 18.7499411097 )");
        builder.addLine(1L, geometry, new HashMap<String, String>());
        final Atlas rawAtlas = builder.get();
        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(map));
        final Atlas slicedAtlas = slicer.sliceLines(rawAtlas);
        Assert.assertEquals(2, slicedAtlas.numberOfLines());
    }

    @Test
    public void testFeatureRightByCountryBoundary() throws ParseException
    {
        // Work on HTI and DOM
        final Set<String> countries = new HashSet<>();
        countries.add("HTI");
        countries.add("DOM");

        // Initialize grid index
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());
        map.initializeGridIndex(countries);
        Assert.assertTrue(map.hasGridIndex());

        // Slice a line along the border
        final PolyLine geometry = PolyLine.wkt(
                "LINESTRING(-71.71119689941406 19.465297438875965,-71.70982360839844 19.425153718960143,-71.72767639160156 19.390181749736552,-71.77093505859375 19.363623938901224,-71.8121337890625 19.32280716454424,-71.78123474121094 19.296886457967965,-71.74896240234375 19.250218840825706,-71.70433044433594 19.22428664772902,-71.66038513183594 19.21391262405755,-71.66862487792969 19.176301302579176,-71.67755126953125 19.143870855908183,-71.73660278320312 19.117921909279115,-71.75033569335938 19.07509724212452,-71.81625366210938 19.03161239237521,-71.88217163085938 19.003048981647012,-71.91925048828125 18.95370063230706,-71.89521789550781 18.923175265301367,-71.80938720703125 18.923175265301367,-71.73934936523438 18.938113908068473,-71.66107177734375 18.94850521929427,-71.60957336425781 18.910184055628548,-71.61026000976562 18.86405711499645,-71.6195297241211 18.813042837757894,-71.64630889892578 18.78249184724649,-71.7242431640625 18.77371553802311,-71.78054809570312 18.745108099985455,-71.83959960937499 18.683975975631473,-71.87118530273438 18.6592567227563)");

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addLine(1000000L, geometry, new HashMap<String, String>());
        builder.addLine(2000000L, geometry.reversed(), new HashMap<String, String>());
        final Atlas rawAtlas = builder.get();

        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(map));
        final Atlas slicedAtlas = slicer.sliceLines(rawAtlas);
        Assert.assertEquals(6, slicedAtlas.numberOfLines());

        // First piece should be in DOM and rest should be in HTI
        Assert.assertEquals("DOM", slicedAtlas.line(1001000L).getTag(ISOCountryTag.KEY).get());

        slicedAtlas.lines(
                line -> line.getOsmIdentifier() == 1000000L && line.getIdentifier() != 1001000L)
                .forEach(line -> Assert.assertEquals("HTI", line.getTag(ISOCountryTag.KEY).get()));

        // Reverse the line and slice again
        // Again first piece should be in DOM and rest should be in HTI
        Assert.assertEquals("DOM", slicedAtlas.line(2001000L).getTag(ISOCountryTag.KEY).get());
        slicedAtlas.lines(
                line -> line.getOsmIdentifier() == 2000000L && line.getIdentifier() != 2001000L)
                .forEach(line -> Assert.assertEquals("HTI", line.getTag(ISOCountryTag.KEY).get()));

        // Returned pieces should be reverse version of each other
        // First pieces are from DOM, they should have reverse geometry
        Assert.assertEquals(slicedAtlas.line(1001000L).asPolyLine(),
                slicedAtlas.line(2001000L).asPolyLine().reversed());

        Assert.assertEquals(slicedAtlas.line(1003000L).asPolyLine(),
                slicedAtlas.line(2003000L).asPolyLine().reversed());
        Assert.assertEquals(slicedAtlas.line(1002000L).asPolyLine(),
                slicedAtlas.line(2002000L).asPolyLine().reversed());
    }

    @Test
    public void testForceSlicing()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());
        final Set<String> countries = new HashSet<>();
        countries.add("HTI");
        countries.add("DOM");
        map.initializeGridIndex(countries);
        // Crosses HTI only and falls in the international waters on both sides
        final PolyLine lineString = PolyLine.wkt(
                "LINESTRING(-72.62310537054378 16.33562831580734,-73.54595693304378 18.890373956748753)");

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addLine(1000000L, lineString, new HashMap<String, String>());
        builder.addLine(2000000L, lineString, Collections.singletonMap("IShouldBeSliced", "yes"));
        builder.addLine(3000000L, lineString, Collections.singletonMap("IShouldBeSliced", "no"));
        final Atlas rawAtlas = builder.get();
        final AtlasLoadingOption loading = AtlasLoadingOption.createOptionWithAllEnabled(map);
        loading.setForceSlicingFilter(new ConfiguredTaggableFilter(
                new StandardConfiguration(new InputStreamResource(() -> CountryBoundaryMap.class
                        .getResourceAsStream("slicing-filter.json")))));
        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(loading);
        final Atlas slicedAtlas = slicer.slice(rawAtlas);

        Assert.assertEquals(5, slicedAtlas.numberOfLines());
        Assert.assertNotNull(slicedAtlas.line(1000000L));
        Assert.assertNotNull(slicedAtlas.line(2001000L));
        Assert.assertNotNull(slicedAtlas.line(2002000L));
        Assert.assertNotNull(slicedAtlas.line(2003000L));
        Assert.assertNotNull(slicedAtlas.line(3000000L));
    }

    @Test
    public void testGetCountryCode()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());

        Point point = JTS_POINT_CONVERTER
                .convert(Location.forString("19.068387997775737, -71.7029007844633"));
        CountryCodeProperties countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("DOM", countryDetails.getIso3CountryCode());

        point = JTS_POINT_CONVERTER
                .convert(Location.forString("19.069172931560374, -71.70712929872246"));
        countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("HTI", countryDetails.getIso3CountryCode());

        point = JTS_POINT_CONVERTER.convert(Location.forString("19.0681781, -71.7075623"));
        countryDetails = map.getCountryCodeISO3(point, false);
        Assert.assertEquals("HTI,DOM", countryDetails.getIso3CountryCode());
    }

    @Test
    public void testGridIndexDeconstructionAndReconstruction()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());

        final Set<String> countries = new HashSet<>(Arrays.asList("HTI", "DOM"));
        map.initializeGridIndex(countries);
        Assert.assertTrue(map.hasGridIndex());

        try
        {
            final STRtree originalIndex = map.getGridIndex();

            final STRtree reconstructedIndex = new STRtree();
            final MultiMap<Geometry, Envelope> gridIndexCells = map.getCells();
            gridIndexCells.forEach((polygon, cells) ->
            {
                cells.forEach(cell -> reconstructedIndex.insert(cell, polygon));
            });
            reconstructedIndex.build();

            // There's no great way to compare large STR Trees
            Assert.assertEquals(originalIndex.size(), reconstructedIndex.size());
            Assert.assertEquals(originalIndex.getNodeCapacity(),
                    reconstructedIndex.getNodeCapacity());
            Assert.assertEquals(originalIndex.getRoot().getBounds().toString(),
                    reconstructedIndex.getRoot().getBounds().toString());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test(expected = CoreException.class)
    public void testGridIndexReconstructionWithMissingCountryCode()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());

        final Set<String> countries = new HashSet<>(
                Arrays.asList("HTI", "DOM", /* Not there on purpose */"CIV"));
        // This is expected to throw a CoreException listing the missing country, versus a NPE.
        map.initializeGridIndex(countries);
    }

    @Test
    public void testIndexBuildWithMultipleThreads()
    {
        // Read map file
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(map.hasGridIndex());

        // Create several threads and try to initialize index simultaneously
        final int threadPoolSize = 4;
        final STRtree[] indices = new STRtree[threadPoolSize];
        try (Pool testPool = new Pool(threadPoolSize, "grid-index-test"))
        {
            for (int index = 0; index < threadPoolSize; index++)
            {
                final int threadIndex = index;
                testPool.queue(() ->
                {
                    logger.info("Trying to initialize grid index.");
                    map.initializeGridIndex(map.getLoadedCountries());
                    indices[threadIndex] = map.getGridIndex();
                });
            }
        }
        catch (final Exception e)
        {
            Assert.fail("Grid index creation in multiple threads failed.");
        }

        // Validate
        Assert.assertTrue(map.hasGridIndex());
        final STRtree referenceTree = indices[0];
        Assert.assertNotNull(referenceTree);
        for (int index = 1; index < threadPoolSize; index++)
        {
            Assert.assertTrue(CountryBoundaryMapCompareCommand.areSTRtreesEqual(referenceTree,
                    indices[index]));
        }
    }

    @Test
    public void testNearestNeighborCountryCodeOnMultiLineStringOutsideBoundary()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("DMA_boundary.txt")));
        final PolyLine polyLine = PolyLine.wkt(new InputStreamResource(
                () -> CountryBoundaryMapTest.class.getResourceAsStream("DMA_snake_polyline.wkt"))
                        .firstLine());
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addLine(1000000L, polyLine, Collections.singletonMap("IShouldBeSliced", "yes"));
        final Atlas rawAtlas = builder.get();
        final AtlasLoadingOption loading = AtlasLoadingOption.createOptionWithAllEnabled(map);
        loading.setForceSlicingFilter(new ConfiguredTaggableFilter(
                new StandardConfiguration(new InputStreamResource(() -> CountryBoundaryMap.class
                        .getResourceAsStream("slicing-filter.json")))));
        final RawAtlasCountrySlicer slicer = new RawAtlasCountrySlicer(loading);
        final Atlas slicedAtlas = slicer.slice(rawAtlas);
        Assert.assertEquals(3,
                Iterables.size(slicedAtlas.lines(line -> Validators.isOfType(line,
                        SyntheticNearestNeighborCountryCodeTag.class,
                        SyntheticNearestNeighborCountryCodeTag.YES))));
    }

    @Test
    public void testOnDemandIndexAndIndexFromFileViaArea()
    {
        // Generate grid index for the first time
        final CountryBoundaryMap firstMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(firstMap.hasGridIndex());
        firstMap.initializeGridIndex(
                new JtsMultiPolygonToMultiPolygonConverter().backwardConvert(MultiPolygon.MAXIMUM));
        Assert.assertTrue(firstMap.hasGridIndex());

        // Read grid index from file
        final CountryBoundaryMap secondMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertTrue(secondMap.hasGridIndex());

        // Compare
        Assert.assertTrue(CountryBoundaryMapCompareCommand.areSTRtreesEqual(firstMap.getGridIndex(),
                secondMap.getGridIndex()));
    }

    @Test
    public void testOnDemandIndexAndIndexFromFileViaCountryList()
    {
        final Set<String> countries = new HashSet<>();
        countries.add("AIA");
        countries.add("MAF");

        // Generate grid index for the first time
        final CountryBoundaryMap firstMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(firstMap.hasGridIndex());
        firstMap.initializeGridIndex(countries);
        Assert.assertTrue(firstMap.hasGridIndex());

        // Read grid index from file
        final CountryBoundaryMap secondMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertTrue(secondMap.hasGridIndex());

        // Compare
        Assert.assertTrue(CountryBoundaryMapCompareCommand.areSTRtreesEqual(firstMap.getGridIndex(),
                secondMap.getGridIndex()));
    }

    @Test
    public void testPartialLoad()
    {
        final Rectangle rectangleInStMartin = Rectangle.forLocations(
                Location.forString("18.0298609, -63.0665379"),
                Location.forString("18.0298052, -63.0663907"));
        final CountryBoundaryMap partialStMartinMap = new CountryBoundaryMap(rectangleInStMartin);
        partialStMartinMap.readFromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(partialStMartinMap.hasGridIndex());

        Assert.assertEquals(1, partialStMartinMap.size());
        Assert.assertEquals("MAF", firstCountryName(partialStMartinMap));
        Assert.assertNotNull(partialStMartinMap.countryBoundary("MAF"));
        Assert.assertNull(partialStMartinMap.countryBoundary("AIA"));

        final Rectangle rectangleInAIA = Rectangle.forLocations(
                Location.forString("18.096068, -63.0643537"),
                Location.forString("18.0927713, -63.0612415"));
        final CountryBoundaryMap partialAIAMap = new CountryBoundaryMap(rectangleInAIA);
        partialAIAMap.readFromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        Assert.assertFalse(partialAIAMap.hasGridIndex());
        Assert.assertEquals(1, partialAIAMap.size());
        Assert.assertEquals("AIA", firstCountryName(partialAIAMap));
        Assert.assertNotNull(partialAIAMap.countryBoundary("AIA"));
        Assert.assertNull(partialAIAMap.countryBoundary("MAF"));
    }

    @Test
    public void testWithinBoundingBoxButNotWithinBoundary()
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("DMA_boundary.txt")));
        final Location location = Location.forWkt("POINT (-61.6678538 15.2957509)");
        final CountryCodeProperties countryCodeProperties = map.getCountryCodeISO3(location);
        Assert.assertTrue(countryCodeProperties.usingNearestNeighbor());
    }

    private String firstCountryName(final CountryBoundaryMap map)
    {
        return map.boundaries(Rectangle.MAXIMUM).get(0).getCountryName();
    }
}
