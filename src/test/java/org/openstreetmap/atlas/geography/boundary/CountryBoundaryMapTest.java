package org.openstreetmap.atlas.geography.boundary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasSlicer;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.test.TestUtility;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
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
    public void testAntiMeridian()
    {
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryBoundaryMapTest.class
                        .getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final LineString lineString = (LineString) TestUtility
                .createJtsGeometryFromWKT("LINESTRING ( -179 18.84927, 179 18.84927 )");

        // HTI is the closest to the line
        Assert.assertEquals("HTI,DOM", map.getCountryCodeISO3(lineString).getIso3CountryCode());
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
        final JtsPolygonToMultiPolygonConverter converter = new JtsPolygonToMultiPolygonConverter();
        // confirm the duplicated border belongs to the USA
        Assert.assertTrue(MultiPolygon.wkt(boundaryUSA.all())
                .isSimilarTo(converter.convert(map.countryBoundary("USA").get(0))));
        // confirm the duplicated border does not belong to HTI
        Assert.assertTrue(MultiPolygon.wkt(boundaryHTI.all())
                .isSimilarTo(converter.convert(map.countryBoundary("HTI").get(0))));
    }

    @Test
    public void testBoundaryLoading() throws ParseException
    {
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(new InputStreamResource(
                () -> CountryBoundaryMapTest.class.getResourceAsStream("CIV_osm_boundaries.txt.gz"))
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
    public void testExtensionBoundariesFilter()
    {
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        final PolyLine line = PolyLine.wkt(
                "LINESTRING(-63.069960775034794 18.20724437315409,-63.056442441599735 18.203616100626693,-63.058416547434696 18.211076399156397)");

        final MultiMap<String, Polygon> boundaries = map.boundaries(line);
        Assert.assertEquals(1, boundaries.size());

        Assert.assertEquals("POLYGON ((-62.76312 18.1617887",
                boundaries.get("AIA").get(0).toText().substring(0, 30));
    }

    @Test
    public void testFeatureCrossingCountryBoundary() throws ParseException
    {
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryBoundaryMapTest.class
                        .getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        final WKTReader reader = new WKTReader();
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        final org.openstreetmap.atlas.geography.Polygon geometry = new org.openstreetmap.atlas.geography.Polygon(
                PolyLine.wkt(
                        "LINESTRING ( -71.7424191 18.7499411097, -71.730485136 18.749848501, -71.730081575 18.749979671, -71.730142154 18.749575218, -71.730486015 18.7498444, -71.7424191 18.7499411097 )"));
        builder.addArea(1L, geometry, new HashMap<String, String>());
        final Atlas rawAtlas = builder.get();
        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(map), rawAtlas).slice();
        Assert.assertEquals(2, slicedAtlas.numberOfAreas());
    }

    @Test
    public void testFeatureRightByCountryBoundary() throws ParseException
    {
        // Work on HTI and DOM
        final Set<String> countries = new HashSet<>();
        countries.add("HTI");
        countries.add("DOM");

        // Initialize grid index
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryBoundaryMapTest.class
                        .getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        // Slice a line along the border
        final PolyLine geometry = PolyLine.wkt(
                "LINESTRING(-71.71119689941406 19.465297438875965,-71.70982360839844 19.425153718960143,-71.72767639160156 19.390181749736552,-71.77093505859375 19.363623938901224,-71.8121337890625 19.32280716454424,-71.78123474121094 19.296886457967965,-71.74896240234375 19.250218840825706,-71.70433044433594 19.22428664772902,-71.66038513183594 19.21391262405755,-71.66862487792969 19.176301302579176,-71.67755126953125 19.143870855908183,-71.73660278320312 19.117921909279115,-71.75033569335938 19.07509724212452,-71.81625366210938 19.03161239237521,-71.88217163085938 19.003048981647012,-71.91925048828125 18.95370063230706,-71.89521789550781 18.923175265301367,-71.80938720703125 18.923175265301367,-71.73934936523438 18.938113908068473,-71.66107177734375 18.94850521929427,-71.60957336425781 18.910184055628548,-71.61026000976562 18.86405711499645,-71.6195297241211 18.813042837757894,-71.64630889892578 18.78249184724649,-71.7242431640625 18.77371553802311,-71.78054809570312 18.745108099985455,-71.83959960937499 18.683975975631473,-71.87118530273438 18.6592567227563)");

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addLine(1000000L, geometry, new HashMap<String, String>());
        builder.addLine(2000000L, geometry.reversed(), new HashMap<String, String>());
        final Atlas rawAtlas = builder.get();

        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(map), rawAtlas).slice();
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
    public void testGetCountryCode()
    {
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryBoundaryMapTest.class
                        .getResourceAsStream("HTI_DOM_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));

        Point point = JTS_POINT_CONVERTER
                .convert(Location.forString("19.068387997775737, -71.7029007844633"));
        CountryCodeProperties countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("DOM", countryDetails.getIso3CountryCode());

        point = JTS_POINT_CONVERTER
                .convert(Location.forString("19.069172931560374, -71.70712929872246"));
        countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("HTI", countryDetails.getIso3CountryCode());

        point = JTS_POINT_CONVERTER.convert(Location.forString("19.0681781, -71.7075623"));
        countryDetails = map.getCountryCodeISO3(point);
        Assert.assertEquals("HTI,DOM", countryDetails.getIso3CountryCode());
    }

    private String firstCountryName(final CountryBoundaryMap map)
    {
        return map.boundaries(Rectangle.MAXIMUM).keySet().iterator().next();
    }
}
