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
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author matthieun
 */
public class PbfToAtlasCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final PbfToAtlasCommand command = new PbfToAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--verbose", "--output=/Users/foo", "--countryName=FRA",
                    "/Users/foo/PbfToAtlasCommandTest.pbf");

            Assert.assertEquals("/Users/foo/FRA_PbfToAtlasCommandTest.atlas\n",
                    outContent.toString());
            Assert.assertEquals("pbf2atlas: loading /Users/foo/PbfToAtlasCommandTest.pbf\n",
                    errContent.toString());
            final File outputAtlasFile = new File("/Users/foo/FRA_PbfToAtlasCommandTest.atlas",
                    filesystem);
            final Atlas outputAtlas = new AtlasResourceLoader()
                    .load(new InputStreamResource(outputAtlasFile::read)
                            .withName(outputAtlasFile.getAbsolutePathString()));
            Assert.assertEquals(5, outputAtlas.numberOfNodes());
            Assert.assertEquals(4, outputAtlas.numberOfEdges());
            Assert.assertEquals(1, outputAtlas.numberOfAreas());
            Assert.assertEquals(1, outputAtlas.numberOfLines());
            Assert.assertEquals(1, outputAtlas.numberOfRelations());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final File pbfFile = new File("/Users/foo/PbfToAtlasCommandTest.pbf", filesystem);
        final Resource resource = new InputStreamResource(
                () -> PbfToAtlasCommandTest.class.getResourceAsStream("testPbf2Atlas.josm.osm"));
        final StringResource osmFile = new StringResource();
        new OsmFileParser().update(resource, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);
    }
}
