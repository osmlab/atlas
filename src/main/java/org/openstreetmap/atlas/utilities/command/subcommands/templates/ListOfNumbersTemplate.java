package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * An example of how to implement an {@link AbstractAtlasShellToolsCommandTemplate}.
 * 
 * @author lcram
 */
public class ListOfNumbersTemplate implements AbstractAtlasShellToolsCommandTemplate
{
    private static final String LIST_OF_NUMBERS_OPTION_LONG = "list-of-numbers";
    private static final String COULD_NOT_PARSE = "could not parse %s '%s'";

    public static List<Integer> getListOfNumbers(final AbstractAtlasShellToolsCommand parentCommand)
    {
        final String listString = parentCommand.getOptionAndArgumentDelegate()
                .getOptionArgument(LIST_OF_NUMBERS_OPTION_LONG)
                .orElseThrow(AtlasShellToolsException::new);

        if (listString.isEmpty())
        {
            return new ArrayList<>();
        }

        final List<Integer> numberList = new ArrayList<>();
        final String[] listStringSplit = listString.split(",");
        for (final String numberElement : listStringSplit)
        {
            final int number;
            try
            {
                number = Integer.parseInt(numberElement);
                numberList.add(number);
            }
            catch (final NumberFormatException exception)
            {
                parentCommand.getCommandOutputDelegate().printlnErrorMessage(
                        String.format(COULD_NOT_PARSE, "number", numberElement));
                return new ArrayList<>();
            }
        }
        return numberList;
    }

    @Override
    public void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.addManualPageSection("LIST OF NUMBERS TEMPLATE",
                new ByteArrayInputStream(
                        ("This is an example man page section for the ListOfNumbersTemplate! "
                                + "This template adds an option that reads a list of numbers.")
                                        .getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.registerOptionWithRequiredArgument(LIST_OF_NUMBERS_OPTION_LONG,
                "Specify a comma separated list of numbers.", OptionOptionality.REQUIRED,
                "numbers");
    }
}
