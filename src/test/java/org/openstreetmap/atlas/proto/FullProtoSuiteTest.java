package org.openstreetmap.atlas.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize.AtlasSizeBuilder;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;
import org.openstreetmap.atlas.geography.atlas.delta.Diff;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class FullProtoSuiteTest
{
    private static final Logger logger = LoggerFactory.getLogger(FullProtoSuiteTest.class);

    private static final int BYTES_PER_KIBIBYTE = 1024;
    private static final int LARGE_DEFAULT_SIZE = 1024 * 1024 * 24;

    // Point parameters
    private static final int NUMBER_OF_POINTS = 1000;
    private static final int TAGS_PER_POINT = 5;

    // Line parameters
    private static final int NUMBER_OF_LINES = 1000;
    private static final int TAGS_PER_LINE = 6;
    private static final int MAXIMUM_LINE_SIZE = 10;
    private static final int MINIMUM_LINE_SIZE = 2;

    // Area parameters
    private static final int NUMBER_OF_AREAS = 1000;
    private static final int TAGS_PER_AREA = 3;
    private static final int MAXIMUM_AREA_SIZE = 12;
    private static final int MINIMUM_AREA_SIZE = 3;

    // Node parameters
    private static final int TAGS_PER_NODE = 2;

    // Edge parameters
    private static final int NUMBER_OF_EDGES = 1000;
    private static final int TAGS_PER_EDGE = 4;

    // Relation parameters
    private static final int NUMBER_OF_RELATIONS = 1000;
    private static final int TAGS_PER_RELATION = 7;

    private static int nextIdentifier = 0;

    private PackedAtlas cachedAtlas = null;

    @Test
    public void testConsistency()
    {
        final PackedAtlas atlas = getNewAtlas();

        final ByteArrayResource javaResource = new ByteArrayResource(LARGE_DEFAULT_SIZE);
        final ByteArrayResource protoResource = new ByteArrayResource(LARGE_DEFAULT_SIZE);

        logger.info("Starting serialization process...");

        Time javaTime = Time.now();
        atlas.setSaveSerializationFormat(AtlasSerializationFormat.JAVA);
        atlas.save(javaResource);
        logger.info("Java serialization time: {}", javaTime.elapsedSince());

        Time protoTime = Time.now();
        atlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
        atlas.save(protoResource);
        logger.info("Proto serialization time: {}", protoTime.elapsedSince());

        logger.info("Java resource size: {} bytes ({} KiB)", javaResource.length(),
                javaResource.length() / BYTES_PER_KIBIBYTE);
        logger.info("Proto resource size: {} bytes ({} KiB)", protoResource.length(),
                protoResource.length() / BYTES_PER_KIBIBYTE);

        javaTime = Time.now();
        final PackedAtlas loadedFromJava = PackedAtlas.load(javaResource);
        logger.info("Java deserialization time: {}", javaTime.elapsedSince());

        protoTime = Time.now();
        final PackedAtlas loadedFromProto = PackedAtlas.load(protoResource);
        logger.info("Proto deserialization time: {}", protoTime.elapsedSince());

        Assert.assertEquals(loadedFromJava, loadedFromProto);

        final SortedSet<Diff> diff = new AtlasDelta(loadedFromJava, loadedFromProto, true)
                .generate().getDifferences();
        Assert.assertTrue(diff.isEmpty());
    }

    @Test
    public void testEmptyTags()
    {
        final HashMap<String, String> map = new HashMap<>();

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1, new Location(Latitude.ZERO, Longitude.ZERO), map);

        final ByteArrayResource protoResource = new ByteArrayResource(LARGE_DEFAULT_SIZE);

        final PackedAtlas atlas = (PackedAtlas) builder.get();

        logger.info("Starting serialization process...");

        Time protoTime = Time.now();
        atlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
        atlas.save(protoResource);
        logger.info("Proto serialization time: {}", protoTime.elapsedSince());

        logger.info("Proto resource size: {} bytes ({} KiB)", protoResource.length(),
                protoResource.length() / BYTES_PER_KIBIBYTE);

        protoTime = Time.now();
        final PackedAtlas loadedFromProto = PackedAtlas.load(protoResource);
        logger.info("Proto deserialization time: {}", protoTime.elapsedSince());

        Assert.assertEquals(atlas, loadedFromProto);
    }

    private PackedAtlas getCachedAtlas()
    {
        if (this.cachedAtlas == null)
        {
            this.cachedAtlas = setupAtlas1();
        }
        return this.cachedAtlas;
    }

    private PackedAtlas getFileAtlas(final String string)
    {
        return PackedAtlas.load(new File(string));
    }

    private PackedAtlas getNewAtlas()
    {
        return setupAtlas1();
    }

    private int getNextIdentifier()
    {
        nextIdentifier++;
        return nextIdentifier;
    }

    private PackedAtlas setupAtlas1()
    {
        final Random random = new Random();
        final AtlasSize sizeEstimates = new AtlasSizeBuilder().withPointEstimate(NUMBER_OF_POINTS)
                .withLineEstimate(NUMBER_OF_LINES).withAreaEstimate(NUMBER_OF_AREAS)
                .withEdgeEstimate(NUMBER_OF_EDGES).withRelationEstimate(NUMBER_OF_RELATIONS)
                .build();
        final PackedAtlasBuilder builder = new PackedAtlasBuilder()
                .withSizeEstimates(sizeEstimates);

        // add some points
        for (int index = 0; index < NUMBER_OF_POINTS; index++)
        {
            final Map<String, String> randomTags = RandomTagsSupplier.randomTags(TAGS_PER_POINT);
            builder.addPoint(getNextIdentifier(), Location.random(Rectangle.MAXIMUM), randomTags);
        }

        // add some lines
        for (int index = 0; index < NUMBER_OF_LINES; index++)
        {
            final Map<String, String> randomTags = RandomTagsSupplier.randomTags(TAGS_PER_LINE);
            final PolyLine geometry = PolyLine.random(
                    random.nextInt(MAXIMUM_LINE_SIZE - MINIMUM_LINE_SIZE) + MINIMUM_LINE_SIZE,
                    Rectangle.TEST_RECTANGLE);
            builder.addLine(getNextIdentifier(), geometry, randomTags);
        }

        // add some areas
        for (int index = 0; index < NUMBER_OF_AREAS; index++)
        {
            final Map<String, String> randomTags = RandomTagsSupplier.randomTags(TAGS_PER_AREA);
            final Polygon geometry = Polygon.random(
                    random.nextInt(MAXIMUM_AREA_SIZE - MINIMUM_AREA_SIZE) + MINIMUM_AREA_SIZE,
                    Rectangle.TEST_RECTANGLE);
            builder.addArea(getNextIdentifier(), geometry, randomTags);
        }

        // create some nodes
        builder.addNode(getNextIdentifier(), Location.TEST_1,
                RandomTagsSupplier.randomTags(TAGS_PER_NODE));
        builder.addNode(getNextIdentifier(), Location.TEST_2,
                RandomTagsSupplier.randomTags(TAGS_PER_NODE));
        builder.addNode(getNextIdentifier(), Location.TEST_3,
                RandomTagsSupplier.randomTags(TAGS_PER_NODE));
        builder.addNode(getNextIdentifier(), Location.TEST_4,
                RandomTagsSupplier.randomTags(TAGS_PER_NODE));
        builder.addNode(getNextIdentifier(), Location.TEST_5,
                RandomTagsSupplier.randomTags(TAGS_PER_NODE));

        // connect the nodes into some edges
        for (int index = 0; index < NUMBER_OF_EDGES; index++)
        {
            builder.addEdge(
                    getNextIdentifier(), new PolyLine(Location.TEST_1, Location.TEST_2,
                            Location.TEST_3, Location.TEST_4, Location.TEST_5),
                    RandomTagsSupplier.randomTags(TAGS_PER_EDGE));
        }

        // add some relations
        final RelationBean structure1 = new RelationBean();
        structure1.addItem(1L, "p1", ItemType.POINT);
        structure1.addItem(2L, "p2", ItemType.POINT);
        structure1.addItem(3L, "p3", ItemType.POINT);
        for (int index = 0; index < NUMBER_OF_RELATIONS; index++)
        {
            builder.addRelation(getNextIdentifier(), nextIdentifier, structure1,
                    RandomTagsSupplier.randomTags(TAGS_PER_RELATION));
        }

        return (PackedAtlas) builder.get();
    }
}
