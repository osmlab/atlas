package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.ArgumentParity;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.UnknownOptionException;

public class SimpleOptionAndArgumentParserTest
{
    @Test
    public void test()
    {
        final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
        parser.registerOption("opt1", "the 1st option");
        parser.registerOption("opt2", "the 2nd option");
        parser.registerOptionWithRequiredArgument("opt3", "the 3rd option", "ARG");
        parser.registerArgument("arg1", ArgumentParity.SINGLE);
        parser.registerArgument("arg2", ArgumentParity.MULTIPLE);
        parser.registerArgument("arg3", ArgumentParity.SINGLE);

        final List<String> arguments = Arrays.asList("--opt2", "--opt3=value", "arg1", "--opt1",
                "arg2", "arg3", "arg4", "arg5");

        try
        {
            parser.parseOptionsAndArguments(arguments);
        }
        catch (final UnknownOptionException e)
        {
            e.printStackTrace();
        }
        catch (final OptionParseException e)
        {
            e.printStackTrace();
        }
    }
}
