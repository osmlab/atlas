package org.openstreetmap.atlas.geography.atlas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader.AtlasFileSelector;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * {@link AtlasResourceLoader} tests
 *
 * @author cstaylor
 * @author mgostintsev
 * @author remegraw
 */
public class AtlasResourceLoaderTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void missingDirectory()
    {
        Assert.assertNull(new AtlasResourceLoader().load(new File(
                Paths.get(System.getProperty("user.home"), "FileThatDoesntExist").toString())));
    }

    @Test
    public void multipleFiles()
    {
        final File parent = File.temporaryFolder();
        try
        {
            final File atlas1 = parent.child("iAmAn.atlas");
            atlas1.writeAndClose("1");
            final File atlas2 = parent.child("iTooAmAn.atlas");
            atlas2.writeAndClose("2");
            final File other = parent.child("iAmNotAnAtlas.txt");
            other.writeAndClose("3");
            final List<Resource> selected = new AtlasFileSelector().select(parent);
            // This one does not filter on an Atlas.
            Assert.assertEquals(3, selected.size());
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    @Test
    public void nullFile()
    {
        final File nullfile = null;
        Assert.assertNull(new AtlasResourceLoader().load(nullfile));
    }

    @Test
    public void oneFile()
    {
        File temporary = null;
        try
        {
            temporary = File.temporary();
            temporary.writeAndClose("1");
            final List<Resource> selected = new AtlasFileSelector().select(temporary);
            Assert.assertEquals(1, selected.size());
            Assert.assertTrue(temporary == selected.get(0));
        }
        finally
        {
            temporary.delete();
        }
    }

    @Test
    public void testAtlasFileExtensionFilter()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1, new Location(Latitude.degrees(0), Longitude.degrees(0)),
                Maps.hashMap());
        final Atlas atlas = builder.get();
        final ByteArrayResource atlasResource = new ByteArrayResource()
                .withName("iDoNotHaveAnAtlasExt.txt");
        atlas.save(atlasResource);
        Assert.assertNull(new AtlasResourceLoader().load(atlasResource));
        Assert.assertNotNull(new AtlasResourceLoader().withAtlasFileExtensionFilterSetTo(false)
                .load(atlasResource));
    }

    @Test
    public void testLoadingAtlasResourceWithFiltering()
    {
        // Dummy predicate, which brings in all entities
        final Predicate<AtlasEntity> allEntities = entity -> true;

        // Filter that brings in only Nodes
        final Predicate<AtlasEntity> onlyNodes = entity -> entity instanceof Node;

        // Load Atlas with all-entity filter
        final Atlas atlasWithAllEntityFilter = new AtlasResourceLoader()
                .withAtlasEntityFilter(allEntities)
                .load(new InputStreamResource(() -> AtlasResourceLoaderTest.class
                        .getResourceAsStream("NZL_9-506-316.atlas.gz"))
                                .withDecompressor(Decompressor.GZIP)
                                .withName("NZL_9-506-316.atlas.gz"));

        // Load same Atlas without filter
        final Atlas atlasWithoutFilter = new AtlasResourceLoader().load(new InputStreamResource(
                () -> AtlasResourceLoaderTest.class.getResourceAsStream("NZL_9-506-316.atlas.gz"))
                        .withDecompressor(Decompressor.GZIP).withName("NZL_9-506-316.atlas.gz"));

        // Load same Atlas with only-Nodes filter
        final Atlas onlyNodesAtlas = new AtlasResourceLoader().withAtlasEntityFilter(onlyNodes)
                .load(new InputStreamResource(() -> AtlasResourceLoaderTest.class
                        .getResourceAsStream("NZL_9-506-316.atlas.gz"))
                                .withDecompressor(Decompressor.GZIP)
                                .withName("NZL_9-506-316.atlas.gz"));

        // Assert Atlas equality
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.areas()),
                Iterables.size(atlasWithoutFilter.areas()));
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.edges()),
                Iterables.size(atlasWithoutFilter.edges()));
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.lines()),
                Iterables.size(atlasWithoutFilter.lines()));
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.relations()),
                Iterables.size(atlasWithoutFilter.relations()));
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.nodes()),
                Iterables.size(atlasWithoutFilter.nodes()));
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.points()),
                Iterables.size(atlasWithoutFilter.points()));

        // Assert Atlas inequality
        Assert.assertNotEquals(Iterables.size(atlasWithoutFilter.areas()),
                Iterables.size(onlyNodesAtlas.areas()));
        Assert.assertNotEquals(Iterables.size(atlasWithoutFilter.edges()),
                Iterables.size(onlyNodesAtlas.edges()));
        Assert.assertNotEquals(Iterables.size(atlasWithoutFilter.lines()),
                Iterables.size(onlyNodesAtlas.lines()));
        Assert.assertNotEquals(Iterables.size(atlasWithoutFilter.relations()),
                Iterables.size(onlyNodesAtlas.relations()));
        Assert.assertNotEquals(Iterables.size(atlasWithoutFilter.points()),
                Iterables.size(onlyNodesAtlas.points()));

        // Only Node size should be equal
        Assert.assertEquals(Iterables.size(atlasWithAllEntityFilter.nodes()),
                Iterables.size(onlyNodesAtlas.nodes()));
    }

    @Test
    public void testNullSubAtlasAfterFiltering()
    {
        // Filter that brings in only Edges with highway tag tertiary or greater
        final Predicate<AtlasEntity> edgesTertiaryOrGreater = entity -> entity instanceof Edge
                && Validators.from(HighwayTag.class, (Edge) entity).isPresent()
                && Validators.from(HighwayTag.class, (Edge) entity).get()
                        .isMoreImportantThan(HighwayTag.TERTIARY);

        // Load atlas with only edges tertiary or greater and check that it is null as expected
        final Atlas atlasWithEdgesTertiaryOrGreater = new AtlasResourceLoader()
                .withAtlasEntityFilter(edgesTertiaryOrGreater)
                .load(new InputStreamResource(() -> AtlasResourceLoaderTest.class
                        .getResourceAsStream("ECU_6-16-31.atlas")).withName("ECU_6-16-31.atlas"));
        Assert.assertEquals(null, atlasWithEdgesTertiaryOrGreater);
    }

    @Test
    public void testAtlasSerialization() throws IOException, ClassNotFoundException
    {
        final String ATLAS_FILE_NAME = "ECU_6-16-31.atlas";

        PackedAtlas atlas = (PackedAtlas) new AtlasResourceLoader().load(new InputStreamResource(
                () -> AtlasResourceLoaderTest.class.getResourceAsStream(ATLAS_FILE_NAME))
                        .withName("ECU_6-16-31.atlas"));
        final UUID identifier = atlas.getIdentifier();
        final long numOfEdges = atlas.numberOfEdges();
        final long numOfNodes = atlas.numberOfNodes();

        // plain simple serialization and deserialization
        byte[] serialized = serialize(atlas);
        PackedAtlas deserialized = (PackedAtlas) deserialize(serialized);

        final UUID deserializedIdentifier = deserialized.getIdentifier();
        final long deserializedNumOfEdges = deserialized.numberOfEdges();
        final long deserializedNumOfNodes = deserialized.numberOfNodes();

        assert (identifier.equals(deserializedIdentifier));
        assert (numOfEdges == deserializedNumOfEdges);
        assert (numOfNodes == deserializedNumOfNodes);
    }

    private static byte[] serialize(Object obj) throws IOException
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }
}
