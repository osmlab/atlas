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

        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.runSubcommand("--sharding=dynamic@/Users/foo/sharding.txt", "POINT (1 1)");
            Assert.assertEquals(
                    "POINT (1 1) covered by:\n" + "[SlippyTile: zoom = 1, x = 1, y = 0]\n",
                    outContent2.toString());

            final ByteArrayOutputStream outContent1b = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent1b));
            final String polygonString = "POLYGON ((-63.07390759923378 18.20801927106241,-63.05279324986854 18.20801927106241,-63.05279324986854 18.1971750442678,-63.07390759923378 18.1971750442678,-63.07390759923378 18.20801927106241))";
            command.runSubcommand("--country-boundary=/Users/foo/boundaries.txt", polygonString);
            Assert.assertEquals(polygonString + " contains or intersects:\n" + "AIA\n",
                    outContent1b.toString());

            final ByteArrayOutputStream outContent2b = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2b));
            final String pointString = "POINT (-63.07390759923378 18.20801927106241)";
            command.runSubcommand("--country-boundary=/Users/foo/boundaries.txt", pointString);
            Assert.assertEquals(pointString + " covered by:\n" + "AIA\n", outContent2b.toString());

            final ByteArrayOutputStream outContent3b = new ByteArrayOutputStream();
            command = new WKTShardCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent3b));
            final String lineString = "LINESTRING (-63.07390759923378 18.20801927106241,-63.05279324986854 18.20801927106241)";
            command.runSubcommand("--country-boundary=/Users/foo/boundaries.txt", lineString);
            Assert.assertEquals(lineString + " intersects:\n" + "AIA\n", outContent3b.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testCommandWithInputFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent1a = new ByteArrayOutputStream();
            final WKTShardCommand command = new WKTShardCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent1a));
            command.runSubcommand("--sharding=slippy@10", "--input=/Users/foo/input.txt");
            Assert.assertEquals(
                    "POINT (0 0) covered by:\n" + "[SlippyTile: zoom = 10, x = 512, y = 512]\n"
                            + "\n" + "POINT (1 1) covered by:\n"
                            + "[SlippyTile: zoom = 10, x = 514, y = 509]\n",
                    outContent1a.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testUnparseableInput()
    {
        final ByteArrayOutputStream errContent1a = new ByteArrayOutputStream();
        final WKTShardCommand command = new WKTShardCommand();
        command.setNewErrStream(new PrintStream(errContent1a));
        command.runSubcommand("--sharding=slippy@10", "POINT (((");
        Assert.assertEquals(
                "wkt-shard: error: unable to parse 'POINT (((' as WKT or shard string\n",
                errContent1a.toString());
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final File inputText = new File(filesystem.getPath("/Users/foo", "input.txt"));
        inputText.writeAndClose("POINT (0 0)\nPOINT (1 1)\n");
        final File shardingTree = new File(filesystem.getPath("/Users/foo", "sharding.txt"));
        shardingTree.writeAndClose(WKTShardCommandTest.class
                .getResourceAsStream("sharding-tree-1.txt").readAllBytes());
        final File boundaryMap = new File(filesystem.getPath("/Users/foo", "boundaries.txt"));
        boundaryMap.writeAndClose(WKTShardCommandTest.class
                .getResourceAsStream("MAF_AIA_osm_boundaries_with_grid_index.txt").readAllBytes());
    }
}
