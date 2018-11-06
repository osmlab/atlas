package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.ArgumentParity;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.UnknownOptionException;

public class SimpleOptionAndArgumentParserTest
{
    @Test
    public void simpleTestOfMixedOptionsAndArguments()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", "the 1st option");
        parser.registerOption("opt2", "the 2nd option");
        parser.registerOptionWithRequiredArgument("opt3", "the 3rd option", "ARG");
        parser.registerArgument("single1", ArgumentParity.SINGLE);
        parser.registerArgument("single2", ArgumentParity.SINGLE);
        parser.registerArgument("multi1", ArgumentParity.MULTIPLE);

        final List<String> arguments = Arrays.asList("--opt2", "--opt3=value", "arg1", "--opt1",
                "arg2", "arg3", "arg4", "arg5");

        try
        {
            parser.parseOptionsAndArguments(arguments);
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasLongOption("opt1"));
        Assert.assertEquals(true, parser.hasLongOption("opt2"));
        Assert.assertEquals("value", parser.getLongOptionArgument("opt3").get());
        Assert.assertEquals(Arrays.asList("arg1"), parser.getArgumentForHint("single1"));
        Assert.assertEquals(Arrays.asList("arg2"), parser.getArgumentForHint("single2"));
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getArgumentForHint("multi1"));
    }

    @Test
    public void testMultiParityInAllOrders()
    {
        final List<String> arguments = Arrays.asList("arg1", "arg2", "arg3", "arg4", "arg5");

        // First
        final SimpleOptionAndArgumentParser parser1 = new SimpleOptionAndArgumentParser();
        parser1.registerArgument("multi1", ArgumentParity.MULTIPLE);
        parser1.registerArgument("single1", ArgumentParity.SINGLE);
        parser1.registerArgument("single2", ArgumentParity.SINGLE);
        try
        {
            parser1.parseOptionsAndArguments(arguments);
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(Arrays.asList("arg4"), parser1.getArgumentForHint("single1"));
        Assert.assertEquals(Arrays.asList("arg5"), parser1.getArgumentForHint("single2"));
        Assert.assertEquals(Arrays.asList("arg1", "arg2", "arg3"),
                parser1.getArgumentForHint("multi1"));

        // Middle
        final SimpleOptionAndArgumentParser parser2 = new SimpleOptionAndArgumentParser();
        parser2.registerArgument("single1", ArgumentParity.SINGLE);
        parser2.registerArgument("multi1", ArgumentParity.MULTIPLE);
        parser2.registerArgument("single2", ArgumentParity.SINGLE);
        try
        {
            parser2.parseOptionsAndArguments(arguments);
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(Arrays.asList("arg1"), parser2.getArgumentForHint("single1"));
        Assert.assertEquals(Arrays.asList("arg5"), parser2.getArgumentForHint("single2"));
        Assert.assertEquals(Arrays.asList("arg2", "arg3", "arg4"),
                parser2.getArgumentForHint("multi1"));

        // Last
        final SimpleOptionAndArgumentParser parser3 = new SimpleOptionAndArgumentParser();
        parser3.registerArgument("single1", ArgumentParity.SINGLE);
        parser3.registerArgument("single2", ArgumentParity.SINGLE);
        parser3.registerArgument("multi1", ArgumentParity.MULTIPLE);
        try
        {
            parser3.parseOptionsAndArguments(arguments);
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(Arrays.asList("arg1"), parser3.getArgumentForHint("single1"));
        Assert.assertEquals(Arrays.asList("arg2"), parser3.getArgumentForHint("single2"));
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser3.getArgumentForHint("multi1"));
    }
}
