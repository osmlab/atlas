package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class SubAtlasCommandTest
{
    @Test
    public void testPredicate()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final SubAtlasCommand command = new SubAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--predicate=!e.relations().isEmpty()", "--output=/Users/foo", "--verbose");

            Assert.assertEquals("", outContent.toString());
            Assert.assertEquals(
                    "subatlas: loading /Users/foo/test.atlas.txt\n"
                            + "subatlas: saved to /Users/foo/sub_test.atlas\n",
                    errContent.toString());

            final File subAtlasFile = new File("/Users/foo/sub_test.atlas", filesystem);
            final Atlas subAtlas = new AtlasResourceLoader()
                    .load(new InputStreamResource(subAtlasFile::read)
                            .withName(subAtlasFile.getAbsolutePathString()));
            Assert.assertEquals(5, Iterables.size(subAtlas.entities()));
            Assert.assertNotNull(subAtlas.node(8000000L));
            Assert.assertNotNull(subAtlas.node(9000000L));
            Assert.assertNotNull(subAtlas.node(10000000L));
            Assert.assertNotNull(subAtlas.edge(11000000L));
            Assert.assertNotNull(subAtlas.edge(11000001L));
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem)
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        builder.addPoint(1000000L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));
        builder.addPoint(2000000L, Location.forWkt("POINT(2 2)"), Maps.hashMap("baz", "bat"));
        builder.addPoint(3000000L, Location.forWkt("POINT(3 3)"),
                Maps.hashMap("foo", "bar", "baz", "bat"));

        builder.addLine(4000000L, PolyLine.wkt("LINESTRING(10 10, 11 11, 12 12)"),
                Maps.hashMap("this_is", "a_line"));
        builder.addLine(5000000L, PolyLine.wkt("LINESTRING(13 13, 14 14, 15 15)"),
                Maps.hashMap("this_is", "a_line", "foo", "bar"));
        builder.addLine(6000000L, PolyLine.wkt("LINESTRING(16 16, 17 17, 18 18)"),
                Maps.hashMap("hello", "world", "another", "tag1"));

        builder.addArea(7000000L, Polygon.wkt("POLYGON((20 20, 21 20, 21 21, 20 20))"),
                Maps.hashMap("baz", "bat"));

        builder.addNode(8000000L, Location.forWkt("POINT(30 30)"), Maps.hashMap("foo", "bar"));
        builder.addNode(9000000L, Location.forWkt("POINT(32 32)"), Maps.hashMap("baz", "bat"));
        builder.addNode(10000000L, Location.forWkt("POINT(34 34)"),
                Maps.hashMap("another", "tag2"));

        builder.addEdge(11000000L, PolyLine.wkt("LINESTRING(30 30, 31 30, 32 32)"),
                Maps.hashMap("foo", "bar"));
        builder.addEdge(11000001L, PolyLine.wkt("LINESTRING(32 32, 32 33, 34 34)"),
                Maps.hashMap("baz", "bat"));

        final RelationBean bean = new RelationBean();
        bean.addItem(11000000L, "role0", ItemType.EDGE);
        bean.addItem(11000001L, "role1", ItemType.EDGE);
        builder.addRelation(12000000L, 12L, bean, Maps.hashMap("route", "bike"));

        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas.txt", filesystem);
        assert atlas != null;
        atlas.saveAsText(atlasFile);
    }
}
