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
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class ConcatenateAtlasCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final ConcatenateAtlasCommand command = new ConcatenateAtlasCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "/Users/foo/test2.atlas", "--verbose",
                    "--output=/Users/foo");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "fatlas: loading /Users/foo/test.atlas\n"
                            + "fatlas: loading /Users/foo/test2.atlas\n"
                            + "fatlas: processing atlas /Users/foo/test.atlas (1/2)\n"
                            + "fatlas: processing atlas /Users/foo/test2.atlas (2/2)\n"
                            + "fatlas: cloning...\n" + "fatlas: saved to /Users/foo/output.atlas\n",
                    errContent.toString());
            final File outputAtlasFile = new File("/Users/foo/output.atlas", filesystem);
            final Atlas outputAtlas = new AtlasResourceLoader().load(outputAtlasFile);
            Assert.assertEquals(2, outputAtlas.numberOfPoints());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1000000L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));
        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas", filesystem);
        assert atlas != null;
        atlas.save(atlasFile);

        final PackedAtlasBuilder builder2 = new PackedAtlasBuilder();
        builder2.addPoint(2000000L, Location.forWkt("POINT(2 2)"), Maps.hashMap("baz", "bat"));
        final Atlas atlas2 = builder2.get();
        final File atlasFile2 = new File("/Users/foo/test2.atlas", filesystem);
        assert atlas2 != null;
        atlas2.save(atlasFile2);
    }
}
