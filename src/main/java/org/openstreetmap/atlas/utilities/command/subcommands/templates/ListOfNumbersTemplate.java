package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.TemplateTestCommand;

/**
 * An example of how to implement an {@link AtlasShellToolsCommandTemplate}. This template simply
 * provides an option that accepts a comma separated list of numbers. Note that the code to parse
 * the option can be contained within the template itself! Check {@link TemplateTestCommand} to see
 * how to use the template in a command implementation.
 * 
 * @author lcram
 */
public class ListOfNumbersTemplate implements AtlasShellToolsCommandTemplate
{
    private static final String LIST_OF_NUMBERS_OPTION_LONG = "list-of-numbers";
    private static final String COULD_NOT_PARSE = "could not parse %s '%s'";

    /**
     * The parse contexts under which we want the options provided by this template to appear. Leave
     * empty to use the default context.
     */
    private final Integer[] contexts;

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

    /**
     * This constructor allows callers to specify under which contexts they want the options
     * provided by this template to appear. If left blank, this template will only be applied to the
     * default context.
     * 
     * @param contexts
     *            the parse contexts under which you want the options provided by this template to
     *            appear
     */
    public ListOfNumbersTemplate(final Integer... contexts)
    {
        this.contexts = contexts;
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
                "Specify a comma separated list of numbers.", OptionOptionality.REQUIRED, "numbers",
                this.contexts);
    }
}
