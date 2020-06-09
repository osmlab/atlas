package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.StringInputStream;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class AtlasShellToolsDemoCommandTest
{
    @Test
    public void testCommand()
    {
        final ByteArrayOutputStream outContent1 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent1 = new ByteArrayOutputStream();
        final InputStream inStream = new StringInputStream("hello!");

        final AtlasShellToolsDemoCommand command = new AtlasShellToolsDemoCommand();
        command.setNewOutStream(new PrintStream(outContent1));
        command.setNewErrStream(new PrintStream(errContent1));
        command.setNewInStream(inStream);
        command.setNewEnvironment(Maps.hashMap("user.home", "/Users/foo"));
        command.runSubcommand("--breakfast", "waffles");

        Assert.assertEquals(
                "Using special breakfast mode:\nwaffles\nNow say something!\n> You said: hello!\n",
                outContent1.toString());
        Assert.assertEquals("ast-demo: value of HOME environment variable: /Users/foo\n",
                errContent1.toString());
    }
}
