package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class HelloWorldCommandTest
{
    @Test
    public void test()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        HelloWorldCommand command = new HelloWorldCommand();
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));

        command.runSubcommand("--verbose");

        Assert.assertEquals("Hello, world!\n", outContent.toString());
        Assert.assertTrue(errContent.toString().isEmpty());

        final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
        command = new HelloWorldCommand();
        command.setNewOutStream(new PrintStream(outContent2));
        command.setNewErrStream(new PrintStream(errContent2));

        command.runSubcommand("--verbose", "--name=foo");

        Assert.assertEquals("Hello, foo!\n", outContent2.toString());
        Assert.assertTrue(errContent2.toString().isEmpty());
    }
}
