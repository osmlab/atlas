package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class OsmToAtlasCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final OsmToAtlasCommand command = new OsmToAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--verbose", "/Users/foo/test.josm.osm", "/Users/foo/test.atlas",
                    "--josm", "--country=DMA");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertTrue(errContent.toString().isEmpty());
            final File outputAtlasFile = new File("/Users/foo/test.atlas", filesystem);
            Assert.assertTrue(outputAtlasFile.exists());
            final Atlas outputAtlas = new AtlasResourceLoader()
                    .load(new InputStreamResource(outputAtlasFile::read)
                            .withName(outputAtlasFile.getAbsolutePathString()));
            Assert.assertEquals(4, outputAtlas.numberOfAreas());
            Assert.assertEquals(1, outputAtlas.numberOfRelations());
            Assert.assertEquals("DMA",
                    outputAtlas.area(102506000000L).getTag(ISOCountryTag.KEY).orElse(""));
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
