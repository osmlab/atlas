package org.openstreetmap.atlas.utilities.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.ArgumentException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.UnknownOptionException;

/**
 * @author lcram
 */
public class SimpleOptionAndArgumentParserTest
{
    @Test
    public void simpleTestOfMixedOptionsAndArguments()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", "the 1st option");
        parser.registerOption("opt2", "the 2nd option");
        parser.registerOptionWithRequiredArgument("opt3", "the 3rd option", "ARG");
        parser.registerOption("opt4", 'o', "a short form");
        parser.registerArgument("single1", ArgumentArity.UNARY);
        parser.registerArgument("single2", ArgumentArity.UNARY);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC);

        final List<String> arguments = Arrays.asList("--opt2", "--opt3=value", "arg1", "--opt1",
                "arg2", "arg3", "-o", "arg4", "arg5");
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
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals("value", parser.getLongOptionArgument("opt3").get());

        /*
         * hasOption(longForm) will return true even if only the shortForm was actually present on
         * the command line
         */
        Assert.assertEquals(true, parser.hasOption("opt4"));

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1"));
        Assert.assertEquals("arg2", parser.getUnaryArgument("single2"));
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getVariadicArgument("multi1"));
    }

    @Test(expected = ArgumentException.class)
    public void testMissingUnaryArgument() throws ArgumentException
    {
        testMissingArgument(ArgumentArity.UNARY);
    }

    @Test(expected = ArgumentException.class)
    public void testMissingVariadicArgument() throws ArgumentException
    {
        testMissingArgument(ArgumentArity.VARIADIC);
    }

    @Test
    public void testMultipleShortFormArgumentShorthand()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "a short form");
        parser.registerOption("opt2", 'b', "a short form");
        parser.registerOption("opt3", 'c', "a short form");

        final List<String> arguments1 = Arrays.asList("-abc");
        try
        {
            parser.parseOptionsAndArguments(arguments1);
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals(true, parser.hasOption("opt3"));

        // Swap the order and try again
        final List<String> arguments2 = Arrays.asList("-cba");
        try
        {
            parser.parseOptionsAndArguments(arguments2);
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals(true, parser.hasOption("opt3"));
    }

    @Test(expected = UnknownOptionException.class)
    public void testUnknownOption() throws UnknownOptionException
    {
        final SimpleOptionAndArgumentParser parser1 = new SimpleOptionAndArgumentParser();
        parser1.registerOption("opt1", "option1");

        final List<String> arguments = Arrays.asList("--opt2");
        try
        {
            parser1.parseOptionsAndArguments(arguments);
        }
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final OptionParseException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testVariadicArgumentInAllOrders()
    {
        // First
        SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("multi1", ArgumentArity.VARIADIC);
        parser.registerArgument("single1", ArgumentArity.UNARY);
        parser.registerArgument("single2", ArgumentArity.UNARY);

        final List<String> arguments = Arrays.asList("arg1", "arg2", "arg3", "arg4", "arg5");
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
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("arg4", parser.getUnaryArgument("single1"));
        Assert.assertEquals("arg5", parser.getUnaryArgument("single2"));
        Assert.assertEquals(Arrays.asList("arg1", "arg2", "arg3"),
                parser.getVariadicArgument("multi1"));

        // Middle
        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC);
        parser.registerArgument("single2", ArgumentArity.UNARY);
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
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1"));
        Assert.assertEquals("arg5", parser.getUnaryArgument("single2"));
        Assert.assertEquals(Arrays.asList("arg2", "arg3", "arg4"),
                parser.getVariadicArgument("multi1"));

        // Last
        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY);
        parser.registerArgument("single2", ArgumentArity.UNARY);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC);
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
        catch (final ArgumentException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1"));
        Assert.assertEquals("arg2", parser.getUnaryArgument("single2"));
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getVariadicArgument("multi1"));
    }

    private void testMissingArgument(final ArgumentArity arity) throws ArgumentException
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("multi1", arity);

        final List<String> arguments = new ArrayList<>();
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
    }
}
