package org.openstreetmap.atlas.geography.atlas.raw.creation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveLineItem;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveLocationItem;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveObjectStore;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveRelation;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmosisReaderMock;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.collect.Iterables;

/**
 * Tests {@link RawAtlasGenerator} Raw Atlas creation. These tests include basic parity check on
 * feature counts between the old and new PBF ingest methods, test functionality for various
 * Relation cases, tests edge cases surrounding corrupt or incomplete PBF files.
 *
 * @author mgostintsev
 */
public class RawAtlasGeneratorTest
{
    private static final Map<String, String> EMPTY = new HashMap<>();

    @Test
    public void testLoadingPbfWithPointAsRelationMember()
    {
        final AtlasPrimitiveObjectStore store = new AtlasPrimitiveObjectStore();

        // Create 2 nodes
        store.addPoint(new AtlasPrimitiveLocationItem(1, Location.TEST_1, EMPTY));
        store.addPoint(new AtlasPrimitiveLocationItem(2, Location.TEST_2, EMPTY));

        // Create line 1
        final PolyLine line1 = new Segment(Location.TEST_1, Location.TEST_2);
        store.addLine(new AtlasPrimitiveLineItem(3, line1, EMPTY));

        // Create line 2
        final PolyLine line2 = new Segment(Location.TEST_2, Location.TEST_1);
        store.addLine(new AtlasPrimitiveLineItem(4, line2, EMPTY));

        // Create simple U-turn relation
        final RelationBean relationBean = new RelationBean();
        relationBean.addItem(3L, "to", ItemType.LINE);
        relationBean.addItem(2L, "via", ItemType.POINT);
        relationBean.addItem(4L, "from", ItemType.LINE);
        store.addRelation(new AtlasPrimitiveRelation(123, 123, relationBean, Maps.stringMap(),
                Rectangle.forLocated(line1, line2)));

        final OsmosisReaderMock osmosis = new OsmosisReaderMock(store);
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(() -> osmosis,
                AtlasLoadingOption.createOptionWithNoSlicing());
        final Atlas atlas = rawAtlasGenerator.build();

        Assert.assertEquals(2, atlas.numberOfLines());
        Assert.assertEquals(2, atlas.numberOfPoints());
        Assert.assertEquals(1, atlas.numberOfRelations());
        Assert.assertEquals(0, atlas.numberOfNodes());
        Assert.assertEquals(0, atlas.numberOfEdges());
        Assert.assertEquals(0, atlas.numberOfAreas());
    }

    @Test(expected = CoreException.class)
    public void testLoadingPbfWithWayThatReferencesMissingNode()
    {
        final AtlasPrimitiveObjectStore store = new AtlasPrimitiveObjectStore();

        // Add Points
        store.addPoint(new AtlasPrimitiveLocationItem(1, Location.TEST_1, EMPTY));
        store.addPoint(new AtlasPrimitiveLocationItem(2, Location.TEST_2, EMPTY));

        // Add Lines
        store.addLine(new AtlasPrimitiveLineItem(3, new Segment(Location.TEST_1, Location.TEST_2),
                EMPTY));
        store.addLine(new AtlasPrimitiveLineItem(4, new Segment(Location.TEST_3, Location.TEST_4),
                EMPTY));

        final OsmosisReaderMock osmosis = new OsmosisReaderMock(store);
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(() -> osmosis,
                AtlasLoadingOption.createOptionWithNoSlicing());

        // The raw Atlas should not get built, as one of the nodes referenced by the PBF is missing.
        @SuppressWarnings("unused")
        final Atlas atlas = rawAtlasGenerator.build();
    }

    @Test
    public void testParityBetweenRawAtlasAndGeneratedAtlas()
    {
        // Previous PBF-to-Atlas Implementation
        final String pbfPath = RawAtlasGeneratorTest.class.getResource("9-433-268.osm.pbf")
                .getPath();
        final OsmPbfLoader loader = new OsmPbfLoader(new File(pbfPath), AtlasLoadingOption
                .createOptionWithNoSlicing().setLoadWaysSpanningCountryBoundaries(false));
        final Atlas oldAtlas = loader.read();

        // Raw Atlas Implementation
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        Assert.assertEquals(
                "The original Atlas counts of (Lines + Master Edges + Areas) should equal the total number of all Lines in the Raw Atlas, let's verify this",
                Iterables.size(Iterables.filter(oldAtlas.edges(), edge -> edge.isMasterEdge()))
                        + oldAtlas.numberOfAreas() + oldAtlas.numberOfLines(),
                rawAtlas.numberOfLines());

        Assert.assertEquals("The two Atlas files should have identical number of Relations",
                oldAtlas.numberOfRelations(), rawAtlas.numberOfRelations());

        // Note: Nodes/Points in the old PBF-to-Atlas implementation vs. Points in Raw Atlas
        // implementation are difficult to compare, due to us bringing in every Way shape-point.
        // Skipping this check here.
    }

    @Test
    public void testPbfWithIncompleteRelations()
    {
        // This PBF has several interesting use cases. 1. It will have relations of relations. 2.
        // Some of the member relations will be outside of the PBF and will be missing.
        final String path = RawAtlasGeneratorTest.class.getResource("7-105-51.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas atlas = rawAtlasGenerator.build();

        // The Raw Atlas should never contain Nodes, Edges or Areas
        Assert.assertEquals(0, atlas.numberOfNodes());
        Assert.assertEquals(0, atlas.numberOfEdges());
        Assert.assertEquals(0, atlas.numberOfAreas());

        // Only Points, Lines and Relations
        Assert.assertEquals(457884, atlas.numberOfPoints());
        Assert.assertEquals(45839, atlas.numberOfLines());
        Assert.assertEquals(347, atlas.numberOfRelations());
    }

    @Test
    public void testRawAtlasCreation()
    {
        final String path = RawAtlasGeneratorTest.class.getResource("9-433-268.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas atlas = rawAtlasGenerator.build();

        // The Raw Atlas should never contain Nodes, Edges or Areas
        Assert.assertEquals(0, atlas.numberOfNodes());
        Assert.assertEquals(0, atlas.numberOfEdges());
        Assert.assertEquals(0, atlas.numberOfAreas());

        // Only Points, Lines and Relations
        Assert.assertEquals(52203, atlas.numberOfPoints());
        Assert.assertEquals(6080, atlas.numberOfLines());
        Assert.assertEquals(3, atlas.numberOfRelations());
    }
}
