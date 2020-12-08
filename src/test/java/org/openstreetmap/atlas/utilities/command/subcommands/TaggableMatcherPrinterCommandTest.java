package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class TaggableMatcherPrinterCommandTest
{
    @Test
    public void test()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final TaggableMatcherPrinterCommand command = new TaggableMatcherPrinterCommand();
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));

        command.runSubcommand("foo=bar", "baz=bat", "--verbose");

        Assert.assertEquals("foo=bar\n" + "        =       \n" + "    ┌───┴───┐   \n"
                + "   foo     bar  \n" + "\n" + "\n" + "baz=bat\n" + "        =       \n"
                + "    ┌───┴───┐   \n" + "   baz     bat  \n" + "\n" + "\n", outContent.toString());
        Assert.assertEquals("", errContent.toString());
    }

    @Test
    public void testFail()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final TaggableMatcherPrinterCommand command = new TaggableMatcherPrinterCommand();
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));

        command.runSubcommand("foo=bar=baz", "--verbose");

        Assert.assertEquals("foo=bar=baz\n" + "\n", outContent.toString());
        Assert.assertEquals(
                "print-matcher: error: definition `foo=bar=baz' contained nested equality operators\n",
                errContent.toString());
    }
}
