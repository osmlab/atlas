package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.UnknownOptionException;

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

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals("value", parser.getLongOptionArgument("opt3").get());

        /*
         * hasOption(longForm) will return true even if only the shortForm was actually present on
         * the command line
         */
        Assert.assertEquals(true, parser.hasOption("opt4"));
        Assert.assertEquals(true, parser.hasShortOption('o'));

        Assert.assertEquals(Arrays.asList("arg1"), parser.getArgumentForHint("single1"));
        Assert.assertEquals(Arrays.asList("arg2"), parser.getArgumentForHint("single2"));
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getArgumentForHint("multi1"));
    }

    @Test
    public void testMissingArgument()
    {
        // TODO fill in
    }

    @Test
    public void testMissingOption()
    {
        // TODO fill in
    }

    @Test
    public void testOptionParseException()
    {
        // TODO fill in
    }

    @Test
    public void testUnknownOptionException()
    {
        // TODO fill in
    }

    @Test
    public void testVariadicArgumentInAllOrders()
    {
        final List<String> arguments = Arrays.asList("arg1", "arg2", "arg3", "arg4", "arg5");

        // First
        final SimpleOptionAndArgumentParser parser1 = new SimpleOptionAndArgumentParser();
        parser1.registerArgument("multi1", ArgumentArity.VARIADIC);
        parser1.registerArgument("single1", ArgumentArity.UNARY);
        parser1.registerArgument("single2", ArgumentArity.UNARY);
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
        parser2.registerArgument("single1", ArgumentArity.UNARY);
        parser2.registerArgument("multi1", ArgumentArity.VARIADIC);
        parser2.registerArgument("single2", ArgumentArity.UNARY);
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
        parser3.registerArgument("single1", ArgumentArity.UNARY);
        parser3.registerArgument("single2", ArgumentArity.UNARY);
        parser3.registerArgument("multi1", ArgumentArity.VARIADIC);
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
