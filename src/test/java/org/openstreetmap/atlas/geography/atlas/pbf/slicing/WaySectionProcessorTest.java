package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfMemoryStore;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class WaySectionProcessorTest
{
    private static final Logger logger = LoggerFactory.getLogger(WaySectionProcessorTest.class);

    @Test
    public void sectionAtBarriersTest()
    {
        final AtlasLoadingOption option = AtlasLoadingOption.createOptionWithNoSlicing();
        option.setAdditionalCountryCodes("TEST");
        final Collection<Tag> nodeTags = new ArrayList<>();
        nodeTags.add(new Tag(BarrierTag.KEY, BarrierTag.GATE.name()));
        final Collection<Tag> edgeTags = new ArrayList<>();
        edgeTags.add(new Tag(HighwayTag.KEY, HighwayTag.RESIDENTIAL.name()));

        // Store
        final PbfMemoryStore store = new PbfMemoryStore(option);

        // Nodes
        store.addNode(
                new Node(new CommonEntityData(7, 1, new Date(), new OsmUser(1, "testUser"), 1),
                        Location.TEST_6.getLatitude().asDegrees(),
                        Location.TEST_6.getLongitude().asDegrees()));
        store.addNode(
                new Node(
                        new CommonEntityData(8, 1, new Date(), new OsmUser(1, "testUser"), 1,
                                nodeTags),
                        Location.TEST_2.getLatitude().asDegrees(),
                        Location.TEST_2.getLongitude().asDegrees()));
        store.addNode(
                new Node(new CommonEntityData(9, 1, new Date(), new OsmUser(1, "testUser"), 1),
                        Location.TEST_1.getLatitude().asDegrees(),
                        Location.TEST_1.getLongitude().asDegrees()));

        // Way Nodes
        final List<WayNode> wayNodes = new ArrayList<>();
        wayNodes.add(new WayNode(7));
        wayNodes.add(new WayNode(8));
        wayNodes.add(new WayNode(9));

        // Way
        store.addWay(new Way(
                new CommonEntityData(1000, 1, new Date(), new OsmUser(1, "testUser"), 1, edgeTags),
                wayNodes));

        store.getWays().keySet().stream()
                .forEach(value -> logger.info("Before Sectioning: Way ID {}", value));
        Assert.assertEquals(1, store.wayCount());
        final WaySectionProcessor waySectionProcessor = new WaySectionProcessor(store);
        waySectionProcessor.run();
        store.getWays().keySet().stream()
                .forEach(value -> logger.info("After Sectioning: Way ID {}", value));
        Assert.assertEquals(2, store.wayCount());
    }
}
