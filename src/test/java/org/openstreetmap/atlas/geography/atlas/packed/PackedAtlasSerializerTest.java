package org.openstreetmap.atlas.geography.atlas.packed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.routing.AStarRouter;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.AbstractWritableResource;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.arrays.ByteArray;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
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
    /**
     * @author lcram
     */
    private static class TraceableByteArrayResource extends AbstractWritableResource
    {
        private static final Logger logger = LoggerFactory.getLogger(ByteArrayResource.class);

        private static final int BYTE_MASK = 0xFF;

        private int numberStreamsClosed = 0;

        private final ByteArray array;

        TraceableByteArrayResource()
        {
            this.array = new ByteArray(Long.MAX_VALUE);
            this.array.setName("ByteArrayResource");
        }

        /**
         * @param initialSize
         *            An initial size to help avoiding resizings.
         */
        TraceableByteArrayResource(final long initialSize)
        {
            final int blockSize = (int) (initialSize <= Integer.MAX_VALUE ? initialSize
                    : Integer.MAX_VALUE);
            this.array = new ByteArray(Long.MAX_VALUE, blockSize, Integer.MAX_VALUE);
            this.array.setName("ByteArrayResource");
        }

        public int getNumberStreamsClosed()
        {
            return this.numberStreamsClosed;
        }

        @Override
        public long length()
        {
            return this.array.size();
        }

        public TraceableByteArrayResource withName(final String name)
        {
            setName(name);
            this.array.setName(name);
            return this;
        }

        @Override
        protected InputStream onRead()
        {
            return new InputStream()
            {
                private long index = 0L;
                private boolean readOpen = true;

                @Override
                public void close()
                {
                    TraceableByteArrayResource.this.numberStreamsClosed++;
                    logger.info("Closing a stream in TraceableByteArrayResource {}",
                            TraceableByteArrayResource.this.getName());
                    this.readOpen = false;
                }

                @Override
                public int read() throws IOException
                {
                    if (!this.readOpen)
                    {
                        throw new CoreException("Cannot read a closed stream");
                    }
                    if (this.index >= TraceableByteArrayResource.this.array.size())
                    {
                        return -1;
                    }
                    return TraceableByteArrayResource.this.array.get(this.index++) & BYTE_MASK;
                }
            };
        }

        @Override
        protected OutputStream onWrite()
        {
            return new OutputStream()
            {
                private boolean writeOpen = true;

                @Override
                public void close()
                {
                    this.writeOpen = false;
                    logger.trace("Closed writer after {} bytes.",
                            TraceableByteArrayResource.this.array.size());
                }

                @Override
                public void write(final int byteValue) throws IOException
                {
                    if (!this.writeOpen)
                    {
                        throw new CoreException("Cannot write to a closed stream");
                    }
                    TraceableByteArrayResource.this.array.add((byte) (byteValue & BYTE_MASK));
                }
            };
        }
    }

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
        Assert.assertEquals(2, Iterables.size(
                deserialized.areasIntersecting(Location.TEST_8.boxAround(Distance.ONE_METER))));
    }

    @Test
    public void testDeserializeThenSerialize()
    {
        final Atlas deserialized = deserialized();
        final ByteArrayResource resource = new ByteArrayResource(5_000_000)
                .withName("testDeserializeThenSerialize");
        deserialized.save(resource);
    }

    @Test(expected = CoreException.class)
    public void testInvalidFileFormat()
    {
        final byte[] values = { 1, 2, 3 };
        final ByteArrayResource resource = new ByteArrayResource();
        resource.writeAndClose(values);
        final Atlas atlas = new AtlasResourceLoader().load(resource);
        atlas.metaData();
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
    public void testResourceClosureOnInvalidLoadFormat()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1, new Location(Latitude.degrees(0), Longitude.degrees(0)),
                Maps.hashMap());
        final PackedAtlas atlas = (PackedAtlas) builder.get();

        final TraceableByteArrayResource javaResource = new TraceableByteArrayResource();
        javaResource.setName("java");
        final TraceableByteArrayResource protoResource = new TraceableByteArrayResource();
        protoResource.setName("proto");

        atlas.setSaveSerializationFormat(AtlasSerializationFormat.JAVA);
        atlas.save(javaResource);
        atlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
        atlas.save(protoResource);

        final Atlas javaAtlas = new AtlasResourceLoader().withAtlasFileExtensionFilterSetTo(false)
                .load(javaResource);
        final Atlas protoAtlas = new AtlasResourceLoader().withAtlasFileExtensionFilterSetTo(false)
                .load(protoResource);

        Assert.assertEquals(1, javaResource.getNumberStreamsClosed());
        Assert.assertEquals(2, protoResource.getNumberStreamsClosed());
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
