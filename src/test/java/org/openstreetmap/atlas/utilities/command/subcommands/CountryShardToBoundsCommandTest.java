package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * @author lcram
 */
public class CountryShardToBoundsCommandTest
{
    @Test
    public void testCommand()
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

        final File folder = File.temporaryFolder();
        try
        {
            final Resource resource = new InputStreamResource(
                    () -> CountryShardToBoundsCommandTest.class
                            .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt"));
            final File boundaryMap = folder.child("MAF_AIA_osm_boundaries_with_grid_index.txt");
            resource.copyTo(boundaryMap);

            final ByteArrayOutputStream outContent1b = new ByteArrayOutputStream();
            command = new CountryShardToBoundsCommand();
            command.setNewOutStream(new PrintStream(outContent1b));
            command.runSubcommand("--country-boundary=" + boundaryMap.getAbsolutePathString(),
                    "AIA");
            final String expected = "AIA boundary:\n"
                    + "POLYGON ((-62.76312 18.1617887, -62.8763889 18.1902778,";
            Assert.assertEquals(expected, outContent1b.toString().substring(0, expected.length()));
        }
        finally
        {
            folder.deleteRecursively();
        }
    }
}
