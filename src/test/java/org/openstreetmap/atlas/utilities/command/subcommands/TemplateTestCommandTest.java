package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class TemplateTestCommandTest
{
    @Test
    public void test()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final TemplateTestCommand command = new TemplateTestCommand();

        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand("--list-of-numbers", "1,2,3");

        Assert.assertEquals("[1, 2, 3]\n", outContent.toString());
        Assert.assertEquals("", errContent.toString());
    }

    @Test
    public void testFail()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final TemplateTestCommand command = new TemplateTestCommand();

        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand("--list-of-numbers", "1,foo,3");

        Assert.assertEquals("", outContent.toString());
        Assert.assertEquals(
                "template-test: error: could not parse number 'foo'\n"
                        + "template-test: error: failed to parse number list!\n",
                errContent.toString());
    }

    @Test
    public void testReverseContext()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final TemplateTestCommand command = new TemplateTestCommand();

        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand("--reverse");

        Assert.assertEquals("Using reverse context!\n", outContent.toString());
        Assert.assertEquals("", errContent.toString());
    }
}
