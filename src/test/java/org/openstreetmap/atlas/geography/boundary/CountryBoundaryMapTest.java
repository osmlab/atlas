package org.openstreetmap.atlas.geography.boundary;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.test.TestUtility;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author tony
 * @author Yiqing Jin
 * @author mgostintsev
 */
public class CountryBoundaryMapTest
{
    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMapTest.class);
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();
    private static Envelope MAXIMUM_ENVELOPE = new Envelope(-180, 180, -90, 90);

    @Test
    @SuppressWarnings("unchecked")
    public void compareShapeFileGridIndexToDeserializedFromTextGridIndex()
    {
        final CountryBoundaryMap rawMapFromText = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));

        final STRtree rawIndex = rawMapFromText.getRawIndex();
        final List<Polygon> polygons = rawIndex.query(MAXIMUM_ENVELOPE);
        final DynamicGridIndexBuilder dynamicBuilder = new DynamicGridIndexBuilder(polygons,
                MAXIMUM_ENVELOPE, null);
        final STRtree generatedGridIndex = dynamicBuilder.getIndex();

        final CountryBoundaryMap rawMapFromTextWithGridIndex = new CountryBoundaryMap(
                new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("AIA_MAF_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        final AbstractGridIndexBuilder createdDynamicBuilder = rawMapFromTextWithGridIndex
                .createGridIndex(rawMapFromTextWithGridIndex.getLoadedCountries());
        final STRtree loadedGridIndex = createdDynamicBuilder.getIndex();

        // Validate grid cell equality
        Assert.assertEquals(generatedGridIndex.size(), loadedGridIndex.size());
        Assert.assertEquals(generatedGridIndex.itemsTree(), loadedGridIndex.itemsTree());
    }

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
        final CountryBoundaryMap mapWithGridIndex = new CountryBoundaryMap(
                new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("AIA_MAF_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        final List<Geometry> firstSlice = mapWithGridIndex.slice(1000000L, geometry);

        logger.info(firstSlice.toString());
        logger.info("It took {} to slice using serialized pre-built grid index",
                start.elapsedSince());

        final Time start2 = Time.now();

        final CountryBoundaryMap mapFromOsmTextFile = new CountryBoundaryMap(
                new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        // Construct the grid index on the fly
        mapFromOsmTextFile.createGridIndex(mapFromOsmTextFile.getLoadedCountries());

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
        final CountryBoundaryMap map = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        final LineString lineString = (LineString) TestUtility
                .createJtsGeometryFromWKT("LINESTRING ( -179 18.84927, 179 18.84927 )");

        // HTI is the closest to the line
        Assert.assertEquals("HTI", map.getCountryCodeISO3(lineString).getFirst());
    }

    @Test
    public void testBoundaryLoading()
    {
        final CountryBoundaryMap map = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("CIV_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));

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

    @Test
    public void testFeatureCrossingCountryBoundary() throws ParseException
    {
        final CountryBoundaryMap map = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        final WKTReader reader = new WKTReader();

        final Geometry geometry = reader.read(
                "POLYGON (( -71.7424191 18.7499411097, -71.730485136 18.749848501, -71.730081575 18.749979671, -71.730142154 18.749575218, -71.730486015 18.7498444, -71.7424191 18.7499411097 ))");
        final List<Geometry> pieces = map.slice(1000000L, geometry);
        logger.info(pieces.toString());
        Assert.assertEquals(2, pieces.size());
    }

    @Test
    public void testGetCountryCode()
    {
        final CountryBoundaryMap map = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));

        Point point = JTS_POINT_CONVERTER
                .convert(Location.forString("19.068387997775737, -71.7029007844633"));
        Tuple<String, Boolean> countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("DOM", countryDetails.getFirst());

        point = JTS_POINT_CONVERTER
                .convert(Location.forString("19.069172931560374, -71.70712929872246"));
        countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("HTI", countryDetails.getFirst());

        point = JTS_POINT_CONVERTER.convert(Location.forString("19.0681781, -71.7075623"));
        countryDetails = map.getCountryCodeISO3(point, false);
        Assert.assertEquals("HTI,DOM", countryDetails.getFirst());
    }

    @Test
    public void testGridIndexDeconstructionAndReconstruction()
    {
        final CountryBoundaryMap map = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
        final Set<String> countries = new HashSet<>(Arrays.asList("HTI", "DOM"));
        final MultiMap<String, Envelope> gridIndexCells = map.createGridIndex(countries, true)
                .getSpatialIndexCells();

        final Field field;
        try
        {
            // Use reflection to test the private grid index field
            field = CountryBoundaryMap.class.getDeclaredField("gridIndex");
            field.setAccessible(true);
            final STRtree originalIndex = (STRtree) field.get(map);

            final STRtree reconstructedIndex = new STRtree();
            for (final Envelope cell : gridIndexCells.allValues())
            {
                final Polygon polygon = AbstractGridIndexBuilder.buildGeoBox(cell.getMinX(),
                        cell.getMaxX(), cell.getMinY(), cell.getMaxY());
                reconstructedIndex.insert(polygon.getEnvelopeInternal(), polygon);
            }
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

    @Test
    public void testPartialLoad()
    {
        final Rectangle rectangleInStMartin = Rectangle.forLocations(
                Location.forString("18.0298609, -63.0665379"),
                Location.forString("18.0298052, -63.0663907"));
        final CountryBoundaryMap partialStMartinMap = new CountryBoundaryMap(
                new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP),
                rectangleInStMartin);

        Assert.assertEquals(1, partialStMartinMap.size());
        Assert.assertEquals("MAF", firstCountryName(partialStMartinMap));
        Assert.assertNotNull(partialStMartinMap.countryBoundary("MAF"));
        Assert.assertNull(partialStMartinMap.countryBoundary("AIA"));

        final Rectangle rectangleInAIA = Rectangle.forLocations(
                Location.forString("18.096068, -63.0643537"),
                Location.forString("18.0927713, -63.0612415"));
        final CountryBoundaryMap partialAIAMap = new CountryBoundaryMap(new InputStreamResource(
                CountryBoundaryMapTest.class.getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                        .withDecompressor(Decompressor.GZIP),
                rectangleInAIA);
        Assert.assertEquals(1, partialAIAMap.size());
        Assert.assertEquals("AIA", firstCountryName(partialAIAMap));
        Assert.assertNotNull(partialAIAMap.countryBoundary("AIA"));
        Assert.assertNull(partialAIAMap.countryBoundary("MAF"));
    }

    private String firstCountryName(final CountryBoundaryMap map)
    {
        return map.boundaries(Rectangle.MAXIMUM).get(0).getCountryName();
    }
}
