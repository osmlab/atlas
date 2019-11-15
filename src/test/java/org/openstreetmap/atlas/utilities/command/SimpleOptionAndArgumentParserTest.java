package org.openstreetmap.atlas.utilities.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.AmbiguousAbbreviationException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.UnknownOptionException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.UnparsableContextException;

/**
 * @author lcram
 */
public class SimpleOptionAndArgumentParserTest
{
    @Test(expected = UnparsableContextException.class)
    public void testFailOnInvalidShortOptionAbbrev() throws UnparsableContextException
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "the 1st option", OptionOptionality.OPTIONAL, 1);
        parser.registerOptionWithRequiredArgument("opt2", 'b', "the 2nd option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOption("opt3", 'c', "the 3rd option", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments = Arrays.asList("-abc");
        try
        {
            parser.parse(arguments);
        }
        catch (final AmbiguousAbbreviationException e)
        {
            Assert.fail(e.getMessage());
        }
        catch (final UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testIgnoreUnknownOptions()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.ignoreUnknownOptions(true);
        parser.registerOption("opt1", "the 1st option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt2", "the 2nd option", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments = Arrays.asList("--opt1", "--opt3", "--opt2");

        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(parser.hasOption("opt1"));
        Assert.assertTrue(parser.hasOption("opt2"));
        Assert.assertFalse(parser.hasOption("opt3"));

        final SimpleOptionAndArgumentParser parser2 = new SimpleOptionAndArgumentParser();
        parser2.ignoreUnknownOptions(true);
        parser2.registerOption("opt1", 'o', "the 1st option", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments2 = Arrays.asList("-z", "-o");

        try
        {
            parser2.parse(arguments2);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(parser2.hasOption("opt1"));
    }

    @Test(expected = CoreException.class)
    public void testInvalidArgumentDeclarationOrder()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        // This should fail. You cannot register another argument after an optional argument.
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);

        final List<String> arguments = Arrays.asList("arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = UnparsableContextException.class)
    public void testMultipleParseContextUnparsable() throws UnparsableContextException
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "an option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt2", 'b', "an option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt3", 'c', "an option", OptionOptionality.OPTIONAL, 2);
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 2);
        parser.registerOptionWithRequiredArgument("opt4", 'd', "an option",
                OptionOptionality.OPTIONAL, "hint", 3);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 3);

        final List<String> arguments = Arrays.asList("--opt2", "--opt3");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testMultipleParseContexts()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "an option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt2", 'b', "an option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt3", 'c', "an option", OptionOptionality.OPTIONAL, 2);
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 2);
        parser.registerOptionWithRequiredArgument("opt4", 'd', "an option",
                OptionOptionality.OPTIONAL, "hint", 3);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 3);

        List<String> arguments = Arrays.asList("--opt2");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(1, parser.getContext());
        Assert.assertFalse(parser.hasOption("opt1"));
        Assert.assertTrue(parser.hasOption("opt2"));
        Assert.assertFalse(parser.hasOption("opt3"));
        Assert.assertFalse(parser.hasOption("opt4"));

        arguments = Arrays.asList("--opt3", "arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(2, parser.getContext());
        Assert.assertFalse(parser.hasOption("opt1"));
        Assert.assertFalse(parser.hasOption("opt2"));
        Assert.assertTrue(parser.hasOption("opt3"));
        Assert.assertFalse(parser.hasOption("opt4"));
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());

        arguments = Arrays.asList("--opt4", "optarg");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(3, parser.getContext());
        Assert.assertFalse(parser.hasOption("opt1"));
        Assert.assertFalse(parser.hasOption("opt2"));
        Assert.assertFalse(parser.hasOption("opt3"));
        Assert.assertTrue(parser.hasOption("opt4"));
        Assert.assertEquals("optarg", parser.getOptionArgument("opt4").get());
        Assert.assertFalse(parser.getUnaryArgument("single1").isPresent());
        Assert.assertFalse(parser.getUnaryArgument("single2").isPresent());
    }

    @Test
    public void testMultipleShortFormArgumentShorthand()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "a short form", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt2", 'b', "a short form", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt3", 'c', "a short form", OptionOptionality.OPTIONAL, 1);

        List<String> arguments = Arrays.asList("-abc");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
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
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals(true, parser.hasOption("opt3"));
    }

    @Test
    public void testOfMixedOptionsAndArguments()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", "the 1st option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt2", "the 2nd option", OptionOptionality.OPTIONAL, 1);
        parser.registerOptionWithRequiredArgument("opt3", "the 3rd option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOption("opt4", 'o', "a short form option (4th)", OptionOptionality.OPTIONAL,
                1);
        parser.registerOptionWithOptionalArgument("opt5", "the 5th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOptionWithRequiredArgument("opt6", "the 6th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOptionWithRequiredArgument("opt7", "the 7th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED, 1);

        final List<String> arguments = Arrays.asList("--opt1", "--opt3=value3", "arg1", "--opt2",
                "arg2", "arg3", "-o", "--opt5", "arg4", "--opt6", "value6", "arg5", "--opt7",
                "value7");

        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals("value3", parser.getOptionArgument("opt3").get());

        /*
         * hasOption(longForm) will return true even if only the shortForm was actually present on
         * the command line
         */
        Assert.assertEquals(true, parser.hasOption("opt4"));

        Assert.assertEquals(true, parser.hasOption("opt5"));
        Assert.assertFalse(parser.getOptionArgument("opt5").isPresent());
        Assert.assertEquals("value6", parser.getOptionArgument("opt6").get());
        Assert.assertEquals("value7", parser.getOptionArgument("opt7").get());

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg2", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg3", "arg4", "arg5"),
                parser.getVariadicArgument("multi1"));
    }

    @Test
    public void testOptionArgumentConversion()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOptionWithRequiredArgument("two", "the number two",
                OptionOptionality.OPTIONAL, "value", 1);
        parser.registerOptionWithRequiredArgument("myList", "a list of numbers",
                OptionOptionality.OPTIONAL, "value", 1);
        parser.registerOptionWithRequiredArgument("three", "the number three",
                OptionOptionality.OPTIONAL, "value", 1);
        parser.registerOptionWithRequiredArgument("someOption", 'o', "another option",
                OptionOptionality.OPTIONAL, "value", 1);
        parser.registerOptionWithRequiredArgument("someOption2", 'p', "another option2",
                OptionOptionality.OPTIONAL, "value", 1);

        final List<String> arguments = Arrays.asList("--two=2", "--myList=1:2:3", "-p", "3.14",
                "--three=foo", "-ofalse");

        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(new Integer(2),
                parser.getOptionArgument("two", optionArgument -> Integer.parseInt(optionArgument))
                        .get());

        Assert.assertEquals(Arrays.asList(1, 2, 3),
                parser.getOptionArgument("myList", optionArgument ->
                {
                    final List<Integer> myList = new ArrayList<>();
                    final String[] split = optionArgument.split(":");
                    for (final String string : split)
                    {
                        myList.add(Integer.parseInt(string));
                    }
                    return myList;
                }).get());

        Assert.assertEquals(new Double(3.14),
                parser.getOptionArgument("someOption2", optionArgument ->
                {
                    try
                    {
                        return Double.parseDouble(optionArgument);
                    }
                    catch (final Exception e)
                    {
                        // return null on error, causing getOptionArgument to return an empty
                        // optional
                        return null;
                    }
                }).get());

        Assert.assertEquals(new Boolean(false),
                parser.getOptionArgument("someOption", optionArgument ->
                {
                    try
                    {
                        return Boolean.parseBoolean(optionArgument);
                    }
                    catch (final Exception e)
                    {
                        // return null on error, causing getOptionArgument to return an empty
                        // optional
                        return null;
                    }
                }).get());

        // this conversion will fail, but then we will fall back on a default value
        Assert.assertEquals(new Integer(3), parser.getOptionArgument("three", optionArgument ->
        {
            try
            {
                return Integer.parseInt(optionArgument);
            }
            catch (final Exception e)
            {
                // return null on error, causing getOptionArgument to return an empty optional
                return null;
            }
        }).orElse(new Integer(3)));
    }

    public void testOptionArgumentValueOverwrite()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOptionWithOptionalArgument("opt1", "an option", OptionOptionality.OPTIONAL,
                "ARG", 1);
        parser.registerOptionWithOptionalArgument("opt2", "an option", OptionOptionality.OPTIONAL,
                "ARG", 1);

        final List<String> arguments = Arrays.asList("--opt1=optarg1", "--opt2=optarg2",
                "--opt1=newArg", "--opt2");

        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("newArg", parser.getOptionArgument("opt1").get());
        Assert.assertFalse(parser.getOptionArgument("opt2").isPresent());
    }

    @Test
    public void testOptionMap()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "the 1st option", OptionOptionality.OPTIONAL, 1);
        parser.registerOptionWithRequiredArgument("opt2", 'b', "the 2nd option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOption("opt3", 'c', "the 3rd option", OptionOptionality.OPTIONAL, 1);

        Assert.assertEquals("the 1st option",
                parser.getOptionNameToRegisteredOption().get("opt1").getDescription());
        Assert.assertEquals("the 2nd option",
                parser.getOptionNameToRegisteredOption().get("opt2").getDescription());
        Assert.assertEquals("the 3rd option",
                parser.getOptionNameToRegisteredOption().get("opt3").getDescription());
    }

    @Test
    public void testOptionWithDefaultValue()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOptionWithOptionalArgument("opt1", "an opt with an optional arg",
                OptionOptionality.OPTIONAL, "optarg", 1);

        final List<String> arguments = Arrays.asList("--opt1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("defaultValue",
                parser.getOptionArgument("opt1").orElse("defaultValue"));
    }

    @Test
    public void testPrefixAbbreviation()
    {
        final SimpleOptionAndArgumentParser parser1 = new SimpleOptionAndArgumentParser();
        parser1.registerOption("opt1", "option1", OptionOptionality.OPTIONAL, 1);
        parser1.registerOption("anotherOpt", "option2", OptionOptionality.OPTIONAL, 1);
        parser1.registerOption("option", "option3", OptionOptionality.OPTIONAL, 1);
        parser1.registerOption("optionSuffix", "option4", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments = Arrays.asList("--opt1", "--an", "--option", "--optionSuf");
        try
        {
            parser1.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(parser1.hasOption("opt1"));
        Assert.assertTrue(parser1.hasOption("anotherOpt"));
        Assert.assertTrue(parser1.hasOption("option"));
        Assert.assertTrue(parser1.hasOption("optionSuffix"));
    }

    @Test(expected = AmbiguousAbbreviationException.class)
    public void testPrefixAmbiguous() throws AmbiguousAbbreviationException
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("option", "option1", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("optionSuffix", "option2", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments2 = Arrays.asList("--opt");
        try
        {
            parser.parse(arguments2);
        }
        catch (UnknownOptionException | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = UnparsableContextException.class)
    public void testRequiredOptionsMissing() throws UnparsableContextException
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("option1", "option1", OptionOptionality.REQUIRED, 1);
        parser.registerOption("option2", "option2", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments = Arrays.asList("--option2");

        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testShortFormOptions()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", 'a', "the 1st option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt2", 'b', "the 2nd option", OptionOptionality.OPTIONAL, 1);
        parser.registerOption("opt3", 'c', "the 3rd option", OptionOptionality.OPTIONAL, 1);
        parser.registerOptionWithRequiredArgument("opt4", 'd', "the 4th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOptionWithRequiredArgument("opt5", 'e', "the 5th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOptionWithOptionalArgument("opt6", 'f', "the 6th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerOptionWithOptionalArgument("opt7", 'g', "the 7th option",
                OptionOptionality.OPTIONAL, "ARG", 1);
        parser.registerArgument("hint1", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 1);

        final List<String> arguments = Arrays.asList("-abc", "-doptarg1", "-e", "optarg2",
                "-foptarg3", "-g", "arg");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(true, parser.hasOption("opt1"));
        Assert.assertEquals(true, parser.hasOption("opt2"));
        Assert.assertEquals(true, parser.hasOption("opt3"));
        Assert.assertEquals("optarg1", parser.getOptionArgument("opt4").get());
        Assert.assertEquals("optarg2", parser.getOptionArgument("opt5").get());
        Assert.assertEquals("optarg3", parser.getOptionArgument("opt6").get());
        Assert.assertFalse(parser.getOptionArgument("opt7").isPresent());
        Assert.assertEquals("arg", parser.getUnaryArgument("hint1").get());
    }

    @Test
    public void testSingleOptionalArgument()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 1);

        List<String> arguments = new ArrayList<>();
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(1, parser.getContext());

        arguments = Arrays.asList("arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(1, parser.getContext());
        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
    }

    @Test
    public void testUnaryArgumentOptionality()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 1);

        List<String> arguments = Arrays.asList("arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertFalse(parser.getUnaryArgument("single2").isPresent());

        arguments = Arrays.asList("arg1", "arg2");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
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
        parser1.registerOption("opt1", "option1", OptionOptionality.OPTIONAL, 1);

        final List<String> arguments = Arrays.asList("--opt2");
        try
        {
            parser1.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testUnsuppliedOptionalArgument()
    {
        SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL, 1);

        List<String> arguments = Arrays.asList("arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertFalse(parser.getUnaryArgument("single2").isPresent());

        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL, 1);

        arguments = Arrays.asList("arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertTrue(parser.getVariadicArgument("multi1").isEmpty());
    }

    @Test
    public void testVariadicArgumentInAllPositions()
    {
        // First
        SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);

        final List<String> arguments = Arrays.asList("arg1", "arg2", "arg3", "arg4", "arg5");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg4", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg5", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg1", "arg2", "arg3"),
                parser.getVariadicArgument("multi1"));

        // Middle
        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals("arg5", parser.getUnaryArgument("single2").get());
        Assert.assertEquals(Arrays.asList("arg2", "arg3", "arg4"),
                parser.getVariadicArgument("multi1"));

        // Last
        parser = new SimpleOptionAndArgumentParser();
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("single2", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED, 1);
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
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
        parser.registerArgument("single1", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED, 1);
        parser.registerArgument("multi1", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL, 1);

        List<String> arguments = Arrays.asList("arg1");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());

        arguments = Arrays.asList("arg1", "arg2", "arg3");
        try
        {
            parser.parse(arguments);
        }
        catch (AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException e)
        {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("arg1", parser.getUnaryArgument("single1").get());
        Assert.assertEquals(Arrays.asList("arg2", "arg3"), parser.getVariadicArgument("multi1"));
    }
}
