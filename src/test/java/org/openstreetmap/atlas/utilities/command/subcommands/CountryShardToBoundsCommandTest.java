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
public class CountryShardToBoundsCommandTest
{
    @Test
    public void testCountry()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final CountryShardToBoundsCommand command = new CountryShardToBoundsCommand();
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewFileSystem(filesystem);

            command.runSubcommand("--country-boundary=/Users/foo/boundary.txt", "AIA");

            final String expected = "AIA boundary:\n"
                    + "POLYGON ((-62.76312 18.1617887, -62.8763889 18.1902778,";
            Assert.assertEquals(expected, outContent.toString().substring(0, expected.length()));
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testShard()
    {
        final ByteArrayOutputStream outContent1a = new ByteArrayOutputStream();
        CountryShardToBoundsCommand command = new CountryShardToBoundsCommand();
        command.setNewOutStream(new PrintStream(outContent1a));
        command.runSubcommand("9-168-233");
        Assert.assertEquals("9-168-233 bounds:\n"
                + "POLYGON ((-61.875 15.2841851, -61.875 15.9613291, -61.171875 15.9613291, -61.171875 15.2841851, -61.875 15.2841851))\n",
                outContent1a.toString());

        final ByteArrayOutputStream outContent2a = new ByteArrayOutputStream();
        command = new CountryShardToBoundsCommand();
        command.setNewOutStream(new PrintStream(outContent2a));
        command.runSubcommand("--reverse",
                "POLYGON ((-61.875 15.2841851, -61.875 15.9613291, -61.171875 15.9613291, -61.171875 15.2841851, -61.875 15.2841851))");
        Assert.assertEquals(
                "POLYGON ((-61.875 15.2841851, -61.875 15.9613291, -61.171875 15.9613291, -61.171875 15.2841851, -61.875 15.2841851)) exactly matched shard:\n"
                        + "[SlippyTile: zoom = 9, x = 168, y = 233]\n",
                outContent2a.toString());

        final ByteArrayOutputStream outContent3a = new ByteArrayOutputStream();
        command = new CountryShardToBoundsCommand();
        command.setNewOutStream(new PrintStream(outContent3a));
        command.runSubcommand("--reverse",
                "POLYGON ((-0.3515625 -0.1757812, -0.3515625 0, 0 0, 0 -0.1757812, -0.3515625 -0.1757812))");
        Assert.assertEquals(
                "POLYGON ((-0.3515625 -0.1757812, -0.3515625 0, 0 0, 0 -0.1757812, -0.3515625 -0.1757812)) exactly matched shard:\n"
                        + "[GeoHashTile: value = 7zzz]\n",
                outContent3a.toString());
    }

    private void setupFilesystem(final FileSystem filesystem) throws IOException
    {
        final File boundaryFile = new File(filesystem.getPath("/Users/foo", "boundary.txt"));
        boundaryFile.writeAndClose(CountryShardToBoundsCommandTest.class
                .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt").readAllBytes());
    }
}
