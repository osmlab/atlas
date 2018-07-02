package org.openstreetmap.atlas.geography.atlas.pbf;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
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
 */
public class OsmPbfProcessorTest
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfProcessorTest.class);

    @Test
    public void testKeepOutsideWaysThatAreConnected()
    {
        final Resource osmFromJosm = new InputStreamResource(() -> OsmPbfProcessorTest.class
                .getResourceAsStream("osmPbfProcessorTest_keepOutsideWaysThatAreConnected.osm"));
        final WritableResource osmFile = new StringResource();
        final WritableResource pbfFile = new StringResource();
        final Resource boundaries = new InputStreamResource(
                () -> OsmPbfProcessorTest.class.getResourceAsStream("DMA_boundary.txt"));
        new OsmFileParser().update(osmFromJosm, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap.fromPlainText(boundaries);
        final MultiPolygon boundary = countryBoundaryMap.countryBoundary("DMA").get(0)
                .getBoundary();
        logger.debug("Boundary: {}", boundary.toWkt());
        final AtlasLoadingOption option = AtlasLoadingOption
                .createOptionWithAllEnabled(countryBoundaryMap);
        final OsmPbfLoader loader = new OsmPbfLoader(pbfFile, boundary, option);
        final Atlas atlas = loader.read();
        logger.debug("Atlas: {}", atlas);

        // Nodes outside, totally at the end of the network
        // 39018000000
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                atlas.node(39018000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.name().toLowerCase(),
                atlas.node(39018000000L).tag(SyntheticBoundaryNodeTag.KEY));
        // 39000000000
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                atlas.node(39000000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.name().toLowerCase(),
                atlas.node(39000000000L).tag(SyntheticBoundaryNodeTag.KEY));
        // 39020000000
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                atlas.node(39020000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.name().toLowerCase(),
                atlas.node(39020000000L).tag(SyntheticBoundaryNodeTag.KEY));

        // Node outside, but connected to yet another road to be added
        // 39001000000
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.name(),
                atlas.node(39001000000L).tag(SyntheticNearestNeighborCountryCodeTag.KEY));
        Assert.assertFalse(
                atlas.node(39001000000L).getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
        // 38991000000
        Assert.assertTrue(atlas.node(38991000000L)
                .getTag(SyntheticNearestNeighborCountryCodeTag.KEY).isPresent());
        Assert.assertFalse(
                atlas.node(38991000000L).getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
    }
}
