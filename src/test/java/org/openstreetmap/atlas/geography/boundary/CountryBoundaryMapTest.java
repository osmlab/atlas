package org.openstreetmap.atlas.geography.boundary;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.test.TestUtility;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
        final Geometry geometry = reader.read(new WktPolygonConverter().convert(rectangleInMAF));

        // Read the serialized Country Boundary Map and Grid Index from file. Then try slicing a
        // feature with the pre-built index.
        final Time start = Time.now();
        final CountryBoundaryMap mapWithGridIndex = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        Assert.assertTrue(mapWithGridIndex.hasGridIndex());

        final List<Geometry> firstSlice = mapWithGridIndex.slice(1000000L, geometry);
        logger.info(firstSlice.toString());
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

        final List<Geometry> secondSlice = mapFromOsmTextFile.slice(1000000L, geometry);

        logger.info(secondSlice.toString());
        logger.info("It took {} to slice using constructed grid index", start2.elapsedSince());

        // Make sure the slice results are identical
        Assert.assertEquals(firstSlice, secondSlice);

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
        final Geometry geometry = reader.read(
                "POLYGON (( -71.7424191 18.7499411097, -71.730485136 18.749848501, -71.730081575 18.749979671, -71.730142154 18.749575218, -71.730486015 18.7498444, -71.7424191 18.7499411097 ))");
        final List<Geometry> pieces = map.slice(1000000L, geometry);
        logger.info(pieces.toString());
        Assert.assertEquals(2, pieces.size());
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
        final WKTReader reader = new WKTReader();
        final Geometry geometry = reader.read(
                "LINESTRING(-71.71119689941406 19.465297438875965,-71.70982360839844 19.425153718960143,-71.72767639160156 19.390181749736552,-71.77093505859375 19.363623938901224,-71.8121337890625 19.32280716454424,-71.78123474121094 19.296886457967965,-71.74896240234375 19.250218840825706,-71.70433044433594 19.22428664772902,-71.66038513183594 19.21391262405755,-71.66862487792969 19.176301302579176,-71.67755126953125 19.143870855908183,-71.73660278320312 19.117921909279115,-71.75033569335938 19.07509724212452,-71.81625366210938 19.03161239237521,-71.88217163085938 19.003048981647012,-71.91925048828125 18.95370063230706,-71.89521789550781 18.923175265301367,-71.80938720703125 18.923175265301367,-71.73934936523438 18.938113908068473,-71.66107177734375 18.94850521929427,-71.60957336425781 18.910184055628548,-71.61026000976562 18.86405711499645,-71.6195297241211 18.813042837757894,-71.64630889892578 18.78249184724649,-71.7242431640625 18.77371553802311,-71.78054809570312 18.745108099985455,-71.83959960937499 18.683975975631473,-71.87118530273438 18.6592567227563)");
        final List<Geometry> pieces = map.slice(1000000L, geometry);
        Assert.assertEquals(4, pieces.size());

        // First piece should be in DOM and rest should be in HTI
        Assert.assertEquals("DOM",
                CountryBoundaryMap.getGeometryProperty(pieces.get(0), ISOCountryTag.KEY));
        pieces.stream().skip(1).forEach(piece -> Assert.assertEquals("HTI",
                CountryBoundaryMap.getGeometryProperty(piece, ISOCountryTag.KEY)));

        // Reverse the line and slice again
        // Again first piece should be in DOM and rest should be in HTI
        final List<Geometry> reversedPieces = map.slice(2000000L, geometry.reverse());
        Assert.assertEquals(4, reversedPieces.size());
        Assert.assertEquals("DOM",
                CountryBoundaryMap.getGeometryProperty(reversedPieces.get(0), ISOCountryTag.KEY));
        pieces.stream().skip(1).forEach(piece -> Assert.assertEquals("HTI",
                CountryBoundaryMap.getGeometryProperty(piece, ISOCountryTag.KEY)));

        // Returned pieces should be reverse version of each other
        // First pieces are from DOM, they should have reverse geometry
        Assert.assertEquals(pieces.get(0), reversedPieces.get(0).reverse());

        // The rest is coming from HTI
        // Reversed geometry slice operation would return pieces in reverse order
        Assert.assertEquals(pieces.get(1), reversedPieces.get(3).reverse());
        Assert.assertEquals(pieces.get(2), reversedPieces.get(2).reverse());
        Assert.assertEquals(pieces.get(3), reversedPieces.get(1).reverse());
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
        final LineString lineString = (LineString) TestUtility.createJtsGeometryFromWKT(
                "LINESTRING(-72.62310537054378 16.33562831580734,-73.54595693304378 18.890373956748753)");

        final List<Geometry> sliced1 = map.slice(123, lineString);
        map.setShouldAlwaysSlicePredicate(
                taggable -> taggable.getTag("IShouldBeSliced").isPresent());
        final List<Geometry> sliced2 = map.slice(123, lineString);
        final List<Geometry> sliced3 = map.slice(123, lineString,
                Taggable.with("IShouldBeSliced", "yes"));
        final List<Geometry> sliced4 = map.slice(123, lineString,
                Taggable.with("ShouldIBeSliced", "no"));

        Assert.assertEquals(1, sliced1.size());
        Assert.assertEquals(1, sliced2.size());
        Assert.assertEquals(3, sliced3.size());
        Assert.assertEquals(1, sliced4.size());
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
        assertTrue(countryCodeProperties.usingNearestNeighbor());
    }

    private String firstCountryName(final CountryBoundaryMap map)
    {
        return map.boundaries(Rectangle.MAXIMUM).get(0).getCountryName();
    }
}
