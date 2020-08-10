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
public class WKTShardCommandTest
{
    @Test
    public void testCommand()
    {
        final ByteArrayOutputStream outContent1a = new ByteArrayOutputStream();
        WKTShardCommand command = new WKTShardCommand();
        command.setNewOutStream(new PrintStream(outContent1a));
        command.runSubcommand("--sharding=slippy@10", "POINT (0 0)");
        Assert.assertEquals(
                "POINT (0 0) covered by:\n" + "[SlippyTile: zoom = 10, x = 512, y = 512]\n",
                outContent1a.toString());

        final ByteArrayOutputStream outContent2a = new ByteArrayOutputStream();
        command = new WKTShardCommand();
        command.setNewOutStream(new PrintStream(outContent2a));
        command.runSubcommand("--sharding=slippy@1", "0-0-0");
        Assert.assertEquals(
                "0-0-0 contains or intersects:\n" + "[SlippyTile: zoom = 1, x = 0, y = 0]\n"
                        + "[SlippyTile: zoom = 1, x = 1, y = 0]\n"
                        + "[SlippyTile: zoom = 1, x = 0, y = 1]\n"
                        + "[SlippyTile: zoom = 1, x = 1, y = 1]\n",
                outContent2a.toString());

        final ByteArrayOutputStream outContent3a = new ByteArrayOutputStream();
        command = new WKTShardCommand();
        command.setNewOutStream(new PrintStream(outContent3a));
        command.runSubcommand("--sharding=slippy@1", "LINESTRING (1 1, 2 2)");
        Assert.assertEquals(
                "LINESTRING (1 1, 2 2) intersects:\n" + "[SlippyTile: zoom = 1, x = 1, y = 0]\n",
                outContent3a.toString());

        final File folder = File.temporaryFolder();
        try
        {
            final Resource resource1 = new InputStreamResource(
                    () -> WKTShardCommandTest.class.getResourceAsStream("sharding-tree-1.txt"));
            final File shardingTree = folder.child("sharding-tree-1.txt");
            resource1.copyTo(shardingTree);
            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewOutStream(new PrintStream(outContent2));
            command.runSubcommand("--sharding=dynamic@" + shardingTree.getAbsolutePathString(),
                    "POINT (1 1)");
            Assert.assertEquals(
                    "POINT (1 1) covered by:\n" + "[SlippyTile: zoom = 1, x = 1, y = 0]\n",
                    outContent2.toString());

            final Resource resource2 = new InputStreamResource(() -> WKTShardCommandTest.class
                    .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt"));
            final File boundaryMap = folder.child("MAF_AIA_osm_boundaries_with_grid_index.txt");
            resource2.copyTo(boundaryMap);

            final ByteArrayOutputStream outContent1b = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewOutStream(new PrintStream(outContent1b));
            final String polygonString = "POLYGON ((-63.07390759923378 18.20801927106241,-63.05279324986854 18.20801927106241,-63.05279324986854 18.1971750442678,-63.07390759923378 18.1971750442678,-63.07390759923378 18.20801927106241))";
            command.runSubcommand("--country-boundary=" + boundaryMap.getAbsolutePathString(),
                    polygonString);
            Assert.assertEquals(polygonString + " contains or intersects:\n" + "AIA\n",
                    outContent1b.toString());

            final ByteArrayOutputStream outContent2b = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewOutStream(new PrintStream(outContent2b));
            final String pointString = "POINT (-63.07390759923378 18.20801927106241)";
            command.runSubcommand("--country-boundary=" + boundaryMap.getAbsolutePathString(),
                    pointString);
            Assert.assertEquals(pointString + " covered by:\n" + "AIA\n", outContent2b.toString());

            final ByteArrayOutputStream outContent3b = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewOutStream(new PrintStream(outContent3b));
            final String lineString = "LINESTRING (-63.07390759923378 18.20801927106241,-63.05279324986854 18.20801927106241)";
            command.runSubcommand("--country-boundary=" + boundaryMap.getAbsolutePathString(),
                    lineString);
            Assert.assertEquals(lineString + " intersects:\n" + "AIA\n", outContent3b.toString());
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    @Test
    public void testCommandWithInputFile()
    {
        final File folder = File.temporaryFolder();
        try
        {
            final File inputText = folder.child("input.txt");
            inputText.writeAndClose("POINT (0 0)\nPOINT (1 1)\n");

            final ByteArrayOutputStream outContent1a = new ByteArrayOutputStream();
            final WKTShardCommand command = new WKTShardCommand();
            command.setNewOutStream(new PrintStream(outContent1a));
            command.runSubcommand("--sharding=slippy@10",
                    "--input=" + inputText.getAbsolutePathString());
            Assert.assertEquals(
                    "POINT (0 0) covered by:\n" + "[SlippyTile: zoom = 10, x = 512, y = 512]\n"
                            + "\n" + "POINT (1 1) covered by:\n"
                            + "[SlippyTile: zoom = 10, x = 514, y = 509]\n",
                    outContent1a.toString());
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    @Test
    public void testUnparseableInput()
    {
        final ByteArrayOutputStream errContent1a = new ByteArrayOutputStream();
        final WKTShardCommand command = new WKTShardCommand();
        command.setNewErrStream(new PrintStream(errContent1a));
        command.runSubcommand("--sharding=slippy@10", "POINT (((");
        Assert.assertEquals("wkt-shard: error: unable to parse POINT ((( as WKT or shard string\n",
                errContent1a.toString());
    }
}
