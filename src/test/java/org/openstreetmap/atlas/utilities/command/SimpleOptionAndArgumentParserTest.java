package org.openstreetmap.atlas.utilities.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
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
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);

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

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg2", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getVariadicArgument("multi1"));
    }

    @Test(expected = CoreException.class)
    public void testInvalidArgumentDeclarationOrder()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        // This should fail. You cannot register another argument after an optional argument.
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);

        final List<String> arguments = Arrays.asList("arg1");
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
    }

    @Test(expected = ArgumentException.class)
    public void testMissingUnaryArgument() throws ArgumentException
    {
        testMissingRequiredArgument(ArgumentArity.UNARY);
    }

    @Test(expected = ArgumentException.class)
    public void testMissingVariadicArgument() throws ArgumentException
    {
        testMissingRequiredArgument(ArgumentArity.VARIADIC);
    }

    @Test
    public void testMultipleShortFormArgumentShorthand()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "a short form");
        parser.registerOption("opt2", 'b', "a short form");
        parser.registerOption("opt3", 'c', "a short form");

        List<String> arguments = Arrays.asList("-abc");
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
        Assert.assertEquals(true, parser.hasOption("opt3"));

        // Swap the order and try again
        arguments = Arrays.asList("-cba");
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
        Assert.assertEquals(true, parser.hasOption("opt3"));
    }

    @Test
    public void testOptionWithDefaultValue()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOptionWithOptionalArgument("opt1", "an opt with an optional arg", "optarg");

        final List<String> arguments = Arrays.asList("--opt1");
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
        Assert.assertEquals("defaultValue",
                parser.getLongOptionArgument("opt1").orElse("defaultValue"));
    }

    @Test
    public void testUnaryArgumentOptionality()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL);

        List<String> arguments = Arrays.asList("arg1");
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());

        arguments = Arrays.asList("arg1", "arg2");
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg2", parser.getUnaryArgument("single2").get());
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
    public void testUnsuppliedOptionalArgument()
    {
        SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL);

        List<String> arguments = Arrays.asList("arg1");
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertFalse(parser.getUnaryArgument("single2").isPresent());

        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);

        arguments = Arrays.asList("arg1");
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertTrue(parser.getVariadicArgument("multi1").isEmpty());
    }

    @Test
    public void testVariadicArgumentInAllOrders()
    {
        // First
        SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);

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
        Assert.assertEquals("arg4", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg5", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg1", "arg2", "arg3"),
                parser.getVariadicArgument("multi1"));

        // Middle
        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg5", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg2", "arg3", "arg4"),
                parser.getVariadicArgument("multi1"));

        // Last
        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg2", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getVariadicArgument("multi1"));
    }

    @Test
    public void testVariadicArgumentOptionality()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);

        List<String> arguments = Arrays.asList("arg1");
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());

        arguments = Arrays.asList("arg1", "arg2", "arg3");
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
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals(Arrays.asList("arg2", "arg3"), parser.getVariadicArgument("multi1"));
    }

    private void testMissingRequiredArgument(final ArgumentArity arity) throws ArgumentException
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("multi1", arity, ArgumentOptionality.REQUIRED);

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
