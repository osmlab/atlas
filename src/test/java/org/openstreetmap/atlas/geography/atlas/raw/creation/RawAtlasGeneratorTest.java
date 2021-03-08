package org.openstreetmap.atlas.geography.atlas.raw.creation;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
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
import org.openstreetmap.atlas.geography.atlas.pbf.OsmosisReaderMock;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.SyntheticDuplicateOsmNodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Tests {@link RawAtlasGenerator} Raw Atlas creation. These test functionality for various Relation
 * cases, tests edge cases surrounding corrupt or incomplete PBF files.
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

        @SuppressWarnings("resource")
        final OsmosisReaderMock osmosis = new OsmosisReaderMock(store);
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(() -> osmosis,
                AtlasLoadingOption.createOptionWithNoSlicing(), MultiPolygon.MAXIMUM);
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        Assert.assertEquals(2, atlas.numberOfLines());
        Assert.assertEquals(2, atlas.numberOfPoints());
        Assert.assertEquals(1, atlas.numberOfRelations());
    }

    @Test
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

        @SuppressWarnings("resource")
        final OsmosisReaderMock osmosis = new OsmosisReaderMock(store);
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(() -> osmosis,
                AtlasLoadingOption.createOptionWithNoSlicing(), MultiPolygon.MAXIMUM);

        // The raw Atlas should not get built, as one of the nodes referenced by the PBF is missing.
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        Assert.assertEquals(1, atlas.numberOfLines());
        Assert.assertEquals(2, atlas.numberOfPoints());
        Assert.assertEquals(0, atlas.numberOfRelations());
    }

    @Test
    public void testNestedSingleRelations()
    {
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(
                new InputStreamResource(() -> RawAtlasGeneratorTest.class
                        .getResourceAsStream("nestedSingleRelations.osm.pbf")));
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        // A duplicate point is removed
        Assert.assertEquals(5, atlas.numberOfPoints());
        Assert.assertEquals(0, atlas.numberOfLines());
        Assert.assertEquals(1, atlas.numberOfAreas());
        Assert.assertEquals(2, atlas.numberOfRelations());
    }

    @Test
    public void testNestedSingleRelationsKeepAll()
    {
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(
                new InputStreamResource(() -> RawAtlasGeneratorTest.class
                        .getResourceAsStream("nestedSingleRelations.osm.pbf")),
                AtlasLoadingOption.withNoFilter().setKeepAll(true), MultiPolygon.MAXIMUM);

        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        // The duplicate point is not removed
        Assert.assertEquals(6, atlas.numberOfPoints());
        Assert.assertEquals(0, atlas.numberOfLines());
        Assert.assertEquals(1, atlas.numberOfAreas());
        // No relations are dropped (there are four in the source PBF: `parent1`, `parent1-1`,
        // `parent2`, `parent2-2`).
        Assert.assertEquals(4, atlas.numberOfRelations());
    }

    @Test
    public void testPbfWithIncompleteRelations()
    {
        // This PBF has several interesting use cases. 1. It will have relations of relations. 2.
        // Some of the member relations will be outside of the PBF and will be missing.
        final String path = RawAtlasGeneratorTest.class.getResource("7-105-51.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        Assert.assertEquals(457863, atlas.numberOfPoints());
        Assert.assertEquals(13335, atlas.numberOfAreas());
        Assert.assertEquals(32521, atlas.numberOfLines());
        Assert.assertEquals(408, atlas.numberOfRelations());
        Assert.assertEquals(49, Iterables.size(atlas.points(
                point -> Validators.hasValuesFor(point, SyntheticDuplicateOsmNodeTag.class))));
    }

    @Test
    public void testRawAtlasCreation()
    {
        final String path = RawAtlasGeneratorTest.class.getResource("9-433-268.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        Assert.assertEquals(54636, atlas.numberOfPoints());
        Assert.assertEquals(5399, atlas.numberOfAreas());
        Assert.assertEquals(686, atlas.numberOfLines());
        Assert.assertEquals(5, atlas.numberOfRelations());
        // Duplicated nodes (same tags all round). These are deduplicated without the keepAll flag
        // in the atlas loading options.
        Assert.assertEquals(1, Iterables
                .size(atlas.points(1070166221000000L, 1070191833000000L, 1070195543000000L)));
    }

    @Test
    public void testRawAtlasCreationKeepAll()
    {
        final String path = RawAtlasGeneratorTest.class.getResource("9-433-268.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(
                new File(path, FileSystems.getDefault()),
                AtlasLoadingOption.withNoFilter().setKeepAll(true), MultiPolygon.MAXIMUM);
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        Assert.assertEquals(55265, atlas.numberOfPoints());
        Assert.assertEquals(5399, atlas.numberOfAreas());
        Assert.assertEquals(694, atlas.numberOfLines());
        Assert.assertEquals(11, atlas.numberOfRelations());
        // Duplicated nodes (same tags all round). These are deduplicated without the keepAll flag
        // in the atlas loading options.
        Assert.assertEquals(3, Iterables
                .size(atlas.points(1070166221000000L, 1070191833000000L, 1070195543000000L)));
    }

    @Test
    public void testRawAtlasCreationWithBoundingBox()
    {
        final String path = RawAtlasGeneratorTest.class.getResource("9-433-268.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path),
                MultiPolygon.forPolygon(Location.forWkt("POINT (124.9721500 -8.9466200)").bounds()
                        .expand(Distance.meters(100))));
        final Atlas atlas = rawAtlasGenerator.build();

        // Verify Atlas Entities
        assertBasicRawAtlasPrinciples(atlas);
        Assert.assertEquals(3851, atlas.numberOfPoints());
        Assert.assertEquals(34, atlas.numberOfLines());
        Assert.assertEquals(2, atlas.numberOfAreas());
        Assert.assertEquals(1, atlas.numberOfRelations());
    }

    private void assertBasicRawAtlasPrinciples(final Atlas atlas)
    {
        // The Raw Atlas should never contain Nodes, Edges or Areas
        Assert.assertEquals(0, atlas.numberOfNodes());
        Assert.assertEquals(0, atlas.numberOfEdges());
    }
}
