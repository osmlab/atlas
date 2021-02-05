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
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class AtlasMetadataReaderCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasMetadataReaderCommand command = new AtlasMetadataReaderCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas");

            Assert.assertEquals("/Users/foo/test.atlas metadata:\n" + "Size: \n" + "\tNodes: 0\n"
                    + "\tEdges: 0\n" + "\tAreas: 0\n" + "\tLines: 0\n" + "\tPoints: 1\n"
                    + "\tRelations: 0\n" + "Original: false\n" + "Code Version: codeVersion\n"
                    + "Data Version: dataVersion\n" + "Country: countryName\n"
                    + "Shard: shardName\n" + "Tags:\n\t" + "baz -> bat\n\t" + "foo -> bar" + "\n\n",
                    outContent.toString());
            Assert.assertEquals("", errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testOptions()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasMetadataReaderCommand command = new AtlasMetadataReaderCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--size", "--original", "--code-version",
                    "--data-version", "--country", "--shard", "--tags");

            Assert.assertEquals("/Users/foo/test.atlas metadata:\n" + "Size: \n" + "\tNodes: 0\n"
                    + "\tEdges: 0\n" + "\tAreas: 0\n" + "\tLines: 0\n" + "\tPoints: 1\n"
                    + "\tRelations: 0\n" + "Original: false\n" + "Code Version: codeVersion\n"
                    + "Data Version: dataVersion\n" + "Country: countryName\n"
                    + "Shard: shardName\n" + "Tags:\n\t" + "baz -> bat\n\t" + "foo -> bar" + "\n\n",
                    outContent.toString());
            Assert.assertEquals("", errContent.toString());
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
        final AtlasMetaData metaData = new AtlasMetaData(new AtlasSize(0L, 0L, 0L, 0L, 1L, 0L),
                false, "codeVersion", "dataVersion", "countryName", "shardName",
                Maps.hashMap("foo", "bar", "baz", "bat"));
        builder.setMetaData(metaData);

        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas", filesystem);
        assert atlas != null;
        atlas.save(atlasFile);
    }
}
