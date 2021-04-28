package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.conversion.StringToPredicateConverter;

/**
 * An {@link AtlasShellToolsCommandTemplate} for commands that want to read an input groovy
 * predicate.
 *
 * @author lcram
 */
public class PredicateTemplate implements AtlasShellToolsCommandTemplate
{
    private static final String PREDICATE_OPTION_LONG = "predicate";
    private static final String PREDICATE_IMPORTS_OPTION_LONG = "imports";

    private final Integer[] contexts;

    public static <T> Optional<Predicate<T>> getPredicate(final Class<T> clazz,
            final List<String> importsAllowList, final AbstractAtlasShellToolsCommand parentCommand)
    {
        if (parentCommand.getOptionAndArgumentDelegate().hasOption(PREDICATE_OPTION_LONG))
        {
            final String predicateString = parentCommand.getOptionAndArgumentDelegate()
                    .getOptionArgument(PREDICATE_OPTION_LONG)
                    .orElseThrow(AtlasShellToolsException::new);
            List<String> userImports = new ArrayList<>();
            if (parentCommand.getOptionAndArgumentDelegate()
                    .hasOption(PREDICATE_IMPORTS_OPTION_LONG))
            {
                userImports = StringList
                        .split(parentCommand.getOptionAndArgumentDelegate()
                                .getOptionArgument(PREDICATE_IMPORTS_OPTION_LONG)
                                .orElseThrow(AtlasShellToolsException::new), ",")
                        .getUnderlyingList();
            }
            final List<String> allImports = new ArrayList<>();
            allImports.addAll(userImports);
            allImports.addAll(importsAllowList);
            return Optional.of(new StringToPredicateConverter<T>()
                    .withAddedStarImportPackages(allImports).convert(predicateString));
        }
        return Optional.empty();
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
    public PredicateTemplate(final Integer... contexts)
    {
        this.contexts = contexts;
    }

    @Override
    public void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.addManualPageSection("PREDICATE",
                ShardingTemplate.class.getResourceAsStream("PredicateTemplateSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.registerOptionWithRequiredArgument(PREDICATE_OPTION_LONG,
                "A flexible groovy predicate supplied at the command line. See DESCRIPTION and PREDICATE sections for details.",
                OptionOptionality.OPTIONAL, "groovy-code", this.contexts);
        parentCommand.registerOptionWithRequiredArgument(PREDICATE_IMPORTS_OPTION_LONG,
                "A comma separated list of some additional package imports to include for the predicate option, if present.",
                OptionOptionality.OPTIONAL, "packages", this.contexts);
    }
}
