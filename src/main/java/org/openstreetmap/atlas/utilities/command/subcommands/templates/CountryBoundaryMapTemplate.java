package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.util.Optional;

import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * An {@link AtlasShellToolsCommandTemplate} for commands that want to read an input
 * {@link CountryBoundaryMap}.
 *
 * @author lcram
 */
public class CountryBoundaryMapTemplate implements AtlasShellToolsCommandTemplate
{
    private static final String COUNTRY_BOUNDARY_OPTION_LONG = "country-boundary";

    private final Integer[] contexts;

    /**
     * Get a {@link CountryBoundaryMap} object from the user's input option.
     *
     * @param parentCommand
     *            the parent command that controls this template
     * @return the {@link CountryBoundaryMap} object built from the file specified by the user
     */
    public static Optional<CountryBoundaryMap> getCountryBoundaryMap(
            final AbstractAtlasShellToolsCommand parentCommand)
    {
        final Optional<CountryBoundaryMap> countryBoundaryMap;
        final File boundaryMapFile = new File(parentCommand.getOptionAndArgumentDelegate()
                .getOptionArgument(COUNTRY_BOUNDARY_OPTION_LONG)
                .orElseThrow(AtlasShellToolsException::new), parentCommand.getFileSystem());
        if (!boundaryMapFile.exists())
        {
            parentCommand.getCommandOutputDelegate().printlnErrorMessage(
                    "boundary file " + boundaryMapFile.getAbsolutePathString() + " does not exist");
            return Optional.empty();
        }
        if (parentCommand.getOptionAndArgumentDelegate().hasVerboseOption())
        {
            parentCommand.getCommandOutputDelegate()
                    .printlnCommandMessage("loading country boundary map...");
        }
        countryBoundaryMap = Optional.of(CountryBoundaryMap.fromPlainText(boundaryMapFile));
        if (parentCommand.getOptionAndArgumentDelegate().hasVerboseOption())
        {
            parentCommand.getCommandOutputDelegate().printlnCommandMessage("loaded boundary map");
        }
        return countryBoundaryMap;
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
    public CountryBoundaryMapTemplate(final Integer... contexts)
    {
        this.contexts = contexts;
    }

    @Override
    public void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.addManualPageSection("INPUT COUNTRY BOUNDARY MAP", ShardingTemplate.class
                .getResourceAsStream("CountryBoundaryMapTemplateSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.registerOptionWithRequiredArgument(COUNTRY_BOUNDARY_OPTION_LONG,
                "A boundary file to use for intersection checks. See INPUT COUNTRY BOUNDARY MAP section for details.",
                OptionOptionality.REQUIRED, "boundary-file", this.contexts);
    }
}
