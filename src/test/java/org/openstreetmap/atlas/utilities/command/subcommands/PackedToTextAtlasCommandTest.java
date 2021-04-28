package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class PackedToTextAtlasCommandTest
{
    @Test
    public void testReverse()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final PackedToTextAtlasCommand command = new PackedToTextAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/text.atlas.txt", "--verbose", "--output=/Users/foo",
                    "--reverse");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "packed2text: loading /Users/foo/text.atlas.txt\n"
                            + "packed2text: processing atlas /Users/foo/text.atlas.txt (1/1)\n"
                            + "packed2text: converting /Users/foo/text.atlas.txt...\n"
                            + "packed2text: saved to /Users/foo/text.atlas\n",
                    errContent.toString());
            final File outputAtlasFile = new File("/Users/foo/text.atlas", filesystem);
            final Atlas outputAtlas = new AtlasResourceLoader()
                    .load(new InputStreamResource(outputAtlasFile::read)
                            .withName(outputAtlasFile.getAbsolutePathString()));
            Assert.assertEquals(1, outputAtlas.numberOfPoints());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testToGeoJson()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final PackedToTextAtlasCommand command = new PackedToTextAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/text.atlas.txt", "--verbose", "--output=/Users/foo",
                    "--geojson");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "packed2text: loading /Users/foo/text.atlas.txt\n"
                            + "packed2text: processing atlas /Users/foo/text.atlas.txt (1/1)\n"
                            + "packed2text: converting /Users/foo/text.atlas.txt...\n"
                            + "packed2text: saved to /Users/foo/text.geojson\n",
                    errContent.toString());
            final File outputFile = new File("/Users/foo/text.geojson", filesystem);
            Assert.assertEquals(
                    "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},"
                            + "\"properties\":{\"foo\":\"bar\",\"identifier\":1000000,\"osmIdentifier\":1,\"itemType\":\"POINT\"}}],"
                            + "\"properties\":{\"size\":{\"Number of Edges\":0,\"Number of Nodes\":0,\"Number of Areas\":0,\"Number of Lines\":0,\"Number of Points\":1,\"Number of Relations\":0},"
                            + "\"original\":true,\"Code Version\":\"unknown\",\"Data Version\":\"TextAtlas\",\"Country\":\"unknown\",\"Shard Name\":\"unknown\",\"name\":\"text.atlas.txt\",\"Entity filter used\":true}}",
                    outputFile.readAndClose());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testToLDGeoJson()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final PackedToTextAtlasCommand command = new PackedToTextAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/binary.atlas", "--verbose", "--output=/Users/foo",
                    "--ldgeojson");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "packed2text: loading /Users/foo/binary.atlas\n"
                            + "packed2text: processing atlas /Users/foo/binary.atlas (1/1)\n"
                            + "packed2text: converting /Users/foo/binary.atlas...\n"
                            + "packed2text: saved to /Users/foo/binary.geojson\n",
                    errContent.toString());
            final File outputFile = new File("/Users/foo/binary.geojson", filesystem);
            Assert.assertEquals(
                    "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},"
                            + "\"properties\":{\"foo\":\"bar\",\"identifier\":1000000,\"osmIdentifier\":1,\"itemType\":\"POINT\"}}",
                    outputFile.readAndClose());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testToText()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final PackedToTextAtlasCommand command = new PackedToTextAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/binary.atlas", "--verbose", "--output=/Users/foo");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "packed2text: loading /Users/foo/binary.atlas\n"
                            + "packed2text: processing atlas /Users/foo/binary.atlas (1/1)\n"
                            + "packed2text: converting /Users/foo/binary.atlas...\n"
                            + "packed2text: saved to /Users/foo/binary.atlas.txt\n",
                    errContent.toString());
            final File outputAtlasFile = new File("/Users/foo/binary.atlas.txt", filesystem);
            Assert.assertEquals(
                    "# Nodes\n" + "# Edges\n" + "# Areas\n" + "# Lines\n" + "# Points\n"
                            + "1000000 && 1.0,1.0 && foo -> bar\n" + "# Relations",
                    outputAtlasFile.readAndClose());
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
        final Atlas atlas = builder.get();
        final File atlasTextFile = new File("/Users/foo/text.atlas.txt", filesystem);
        final File atlasBinaryFile = new File("/Users/foo/binary.atlas", filesystem);
        assert atlas != null;
        atlas.saveAsText(atlasTextFile);
        atlas.save(atlasBinaryFile);
    }
}
