package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class OsmFileParserCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final OsmFileParserCommand command = new OsmFileParserCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--verbose", "/Users/foo/test.josm.osm", "/Users/foo/test.osm");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals("", errContent.toString());
            Assert.assertTrue(new File("/Users/foo/test.osm", filesystem).exists());
            Assert.assertEquals(
                    new String(OsmFileParserCommandTest.class
                            .getResourceAsStream("MultiPolygonTest.osm").readAllBytes()),
                    new File("/Users/foo/test.osm", filesystem).readAndClose() + "\n");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final File josmOsmFile = new File(filesystem.getPath("/Users/foo", "test.josm.osm"));
        josmOsmFile.writeAndClose(OsmFileParserCommandTest.class
                .getResourceAsStream("MultiPolygonTest.josm.osm").readAllBytes());
    }
}
