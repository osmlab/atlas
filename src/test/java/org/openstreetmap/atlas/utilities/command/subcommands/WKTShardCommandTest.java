package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class WKTShardCommandTest
{
    @Test
    public void testCommand()
    {
        final ByteArrayOutputStream outContent1 = new ByteArrayOutputStream();
        WKTShardCommand command = new WKTShardCommand();
        command.setNewOutStream(new PrintStream(outContent1));
        command.runSubcommand("--slippy=10", "POINT (0 0)");
        Assert.assertEquals(
                "POINT (0 0) covered by:\n" + "[SlippyTile: zoom = 10, x = 512, y = 512]\n",
                outContent1.toString());

        final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        command = new WKTShardCommand();
        command.setNewOutStream(new PrintStream(outContent2));
        command.runSubcommand("--slippy=5", "POINT (0 0)");
        Assert.assertEquals(
                "POINT (0 0) covered by:\n" + "[SlippyTile: zoom = 5, x = 16, y = 16]\n",
                outContent2.toString());
    }
}
