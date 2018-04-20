package org.openstreetmap.atlas.geography.atlas.packed;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.routing.AStarRouter;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class PackedAtlasSerializerTest
{
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasSerializerTest.class);

    @Test
    public void testDeserializedIntegrity()
    {
        final Atlas deserialized = deserialized();
        final Route route = AStarRouter.balanced(deserialized, Distance.meters(100))
                .route(deserialized.edge(9), deserialized.edge(98));
        Assert.assertEquals(2, route.size());
    }

    @Test
    public void testDeserializedSpatialIndex()
    {
        final Atlas deserialized = deserialized();
        Assert.assertEquals(1, Iterables.size(
                deserialized.areasIntersecting(Location.TEST_3.boxAround(Distance.ONE_METER))));
    }

    @Test
    public void testDeserializeThenSerialize()
    {
        final Atlas deserialized = deserialized();
        final ByteArrayResource resource = new ByteArrayResource(5_000_000)
                .withName("testDeserializeThenSerialize");
        deserialized.save(resource);
    }

    @Test
    public void testPartialLoad() throws NoSuchFieldException, SecurityException
    {
        final PackedAtlas atlas = new PackedAtlasTest().getAtlas();
        logger.info("{}", atlas);
        final File file = File.temporary();
        logger.info("Saving atlas to {}", file);
        atlas.save(file);
        final Time start = Time.now();
        final PackedAtlas deserialized = PackedAtlas.load(file);
        logger.info("Deserialized Atlas File in {}", start.elapsedSince());

        // Node
        final long nodeIdentifier = 1234;

        Assert.assertFalse(getField(deserialized,
                PackedAtlas.FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX) != null);
        final Node node = deserialized.node(nodeIdentifier);
        Assert.assertTrue(getField(deserialized,
                PackedAtlas.FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX) != null);
        logger.info("Got Node {}", nodeIdentifier);

        Assert.assertFalse(getField(deserialized, PackedAtlas.FIELD_NODE_LOCATIONS) != null);
        final Location location = node.getLocation();
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_NODE_LOCATIONS) != null);
        logger.info("Got Node location {}", location);

        Assert.assertFalse(getField(deserialized, PackedAtlas.FIELD_EDGE_IDENTIFIERS) != null);
        Assert.assertFalse(getField(deserialized, PackedAtlas.FIELD_NODE_IN_EDGES_INDICES) != null);
        Assert.assertFalse(
                getField(deserialized, PackedAtlas.FIELD_NODE_OUT_EDGES_INDICES) != null);
        final Set<Edge> connectedEdges = node.connectedEdges();
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_EDGE_IDENTIFIERS) != null);
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_NODE_IN_EDGES_INDICES) != null);
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_NODE_OUT_EDGES_INDICES) != null);
        logger.info("Got Node connected Edges {}", connectedEdges.stream()
                .map(edge -> edge.getIdentifier()).collect(Collectors.toList()));

        Assert.assertFalse(getField(deserialized, PackedAtlas.FIELD_NODE_TAGS) != null);
        final Map<String, String> nodeTags = node.getTags();
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_NODE_TAGS) != null);
        logger.info("Got Node tags {}", nodeTags);

        logger.info("{}", node);

        // Edge
        final long edgeIdentifier = 98;
        final Edge edge = deserialized.edge(edgeIdentifier);
        logger.info("Got Edge {}", edgeIdentifier);
        final PolyLine line = edge.asPolyLine();
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_EDGE_POLY_LINES) != null);
        logger.info("Got Edge geometry {}", line);
        Assert.assertTrue(getField(deserialized, PackedAtlas.FIELD_EDGE_POLY_LINES) != null);
        final Set<Node> connectedNodes = edge.connectedNodes();
        logger.info("Got Edge connected Nodes {}", connectedNodes.stream()
                .map(aNode -> aNode.getIdentifier()).collect(Collectors.toList()));
        final Map<String, String> edgeTags = edge.getTags();
        logger.info("Got Edge tags {}", edgeTags);
        logger.info("{}", edge);

        // Area
        final long areaIdentifier = 45;
        final Area area = deserialized.area(areaIdentifier);
        logger.info("Got Area {}", areaIdentifier);
        final Surface surface = area.asPolygon().surface();
        logger.info("Got Area surface {}", surface);
        final Map<String, String> areaTags = area.getTags();
        logger.info("Got Area tags {}", areaTags);
        logger.info("{}", areaTags);

        logger.info("Deleting {}", file);
        file.delete();
    }

    @Test
    public void testSize()
    {
        final PackedAtlas atlas = new PackedAtlasTest().getAtlas();
        final ByteArrayResource zipped = new ByteArrayResource(8192).withName("zippedByteArray");
        zipped.setCompressor(Compressor.GZIP);
        atlas.save(zipped);
        logger.info("Zipped Size: {}", zipped.length());
    }

    private Atlas deserialized()
    {
        final Atlas atlas = new PackedAtlasTest().getAtlas();
        final ByteArrayResource resource = new ByteArrayResource(524288)
                .withName("testSerializationByteArray");
        atlas.save(resource);
        return PackedAtlas.load(resource);
    }

    private Object getField(final Atlas atlas, final String name)
    {
        try
        {
            final Field field = PackedAtlas.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(atlas);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not get field {}", name, e);
        }
    }
}
