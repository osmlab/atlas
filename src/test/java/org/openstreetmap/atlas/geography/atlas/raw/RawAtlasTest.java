package org.openstreetmap.atlas.geography.atlas.raw;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
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
    public void testBringInConnectedBridgeNodesOutsideCountryBoundaries()
    {
        final Resource osmFromJosm = new InputStreamResource(
                () -> RawAtlasTest.class.getResourceAsStream("outsideConnectedOneWayWays.osm"));
        final WritableResource osmFile = new StringResource();
        final WritableResource pbfFile = new StringResource();
        final Resource boundaries = new InputStreamResource(
                () -> RawAtlasTest.class.getResourceAsStream("DNK_SWE_boundary.txt"));
        new OsmFileParser().update(osmFromJosm, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap.fromPlainText(boundaries);
        final MultiPolygon boundary = countryBoundaryMap.countryBoundary("DNK").get(0)
                .getBoundary();
        logger.debug("Boundary: {}", boundary.toWkt());
        final AtlasLoadingOption option = AtlasLoadingOption
                .createOptionWithAllEnabled(countryBoundaryMap);
        final RawAtlasGenerator generator = new RawAtlasGenerator(pbfFile, option, boundary);
        final Atlas rawAtlas = generator.build();
        logger.debug("Raw Atlas: {}", rawAtlas);

        final Atlas slicedAtlas = new RawAtlasCountrySlicer("DNK", countryBoundaryMap)
                .slice(rawAtlas);
        logger.debug("Sliced Atlas: {}", slicedAtlas);

        // Check the top node 3089123457 has the proper tagging - nearest neighbor and on the
        // boundary
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(), slicedAtlas
                .point(3739700937000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.name(),
                slicedAtlas.point(3739700937000000L).tag(SyntheticBoundaryNodeTag.KEY));

        // Check the bottom node 3089123458 has the proper tagging - nearest neighbor and on the
        // boundary
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(), slicedAtlas
                .point(3739700960000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.name(),
                slicedAtlas.point(3739700960000000L).tag(SyntheticBoundaryNodeTag.KEY));
    }

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
        final MultiPolygon boundary = countryBoundaryMap.countryBoundary("DMA").get(0)
                .getBoundary();
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

        final Atlas slicedAtlas = new RawAtlasCountrySlicer("DMA", countryBoundaryMap)
                .slice(rawAtlas);
        logger.debug("Sliced Atlas: {}", slicedAtlas);

        // Line partially inside, should be included
        // 38986000000
        Assert.assertNotNull(slicedAtlas.line(38986000000L));

        // Line totally outside, but connected
        // 39002000000
        Assert.assertNotNull(slicedAtlas.line(39002000000L));
        // 39019000000
        Assert.assertNotNull(slicedAtlas.line(39019000000L));

        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                slicedAtlas.point(38991000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                slicedAtlas.point(39001000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                slicedAtlas.point(39020000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                slicedAtlas.point(39018000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                slicedAtlas.point(39000000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
    }
}
