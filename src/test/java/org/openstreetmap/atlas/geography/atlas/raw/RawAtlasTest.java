package org.openstreetmap.atlas.geography.atlas.raw;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasSlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class RawAtlasTest
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasTest.class);

    @Test
    public void testBringInConnectedOutsideWays()
    {
        final Resource osmFromJosm = new InputStreamResource(() -> RawAtlasTest.class
                .getResourceAsStream("osmPbfProcessorTest_keepOutsideWaysThatAreConnected.osm"));
        final WritableResource osmFile = new StringResource();
        final WritableResource pbfFile = new StringResource();
        final Resource boundaries = new InputStreamResource(
                () -> RawAtlasTest.class.getResourceAsStream("DMA_boundary.txt"));
        new OsmFileParser().update(osmFromJosm, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap.fromPlainText(boundaries);
        final JtsPolygonToMultiPolygonConverter converter = new JtsPolygonToMultiPolygonConverter();
        final MultiPolygon boundary = converter
                .convert(countryBoundaryMap.countryBoundary("DMA").get(0));
        logger.debug("Boundary: {}", boundary.toWkt());
        final AtlasLoadingOption option = AtlasLoadingOption
                .createOptionWithAllEnabled(countryBoundaryMap);
        final RawAtlasGenerator generator = new RawAtlasGenerator(pbfFile, option, boundary);
        final Atlas rawAtlas = generator.build();
        logger.debug("Raw Atlas: {}", rawAtlas);

        // Line partially inside, should be included
        // 38986000000
        Assert.assertNotNull(rawAtlas.line(38986000000L));

        // Line totally outside, but connected
        // 39002000000
        Assert.assertNotNull(rawAtlas.line(39002000000L));
        // 39019000000
        Assert.assertNotNull(rawAtlas.line(39019000000L));

        option.setCountryCode("DMA");
        final Atlas slicedAtlas = new RawAtlasSlicer(option, rawAtlas).slice();
        logger.debug("Sliced Atlas: {}", slicedAtlas);

        // Line partially inside, should be included
        // 38986000000
        Assert.assertNotNull(slicedAtlas.line(38986000000L));

        // Line totally outside, but connected
        // 39002000000
        Assert.assertNull(slicedAtlas.line(39002000000L));
    }
}
