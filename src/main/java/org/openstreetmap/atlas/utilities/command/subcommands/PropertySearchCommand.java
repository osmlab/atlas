package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;

/**
 * Search atlases for some given properties, with various options and restrictions.
 * 
 * @author lcram
 */
public class PropertySearchCommand extends AtlasLoaderCommand
{
    private static final String GEOMETRY_OPTION_LONG = "geometry";
    private static final String GEOMETRY_OPTION_DESCRIPTION = "A colon separated list of geometry WKTs for which to search.";
    private static final String GEOMETRY_OPTION_HINT = "wkt-geometry";

    private static final String OUTPUT_ATLAS = "collected-multi.atlas";
    private static final String COLLECT_OPTION_LONG = "collect-matching";
    private static final String COLLECT_OPTION_DESCRIPTION = "Collect all matching atlas files and save to a file using the MultiAtlas.";

    private Set<String> wkts;
    private Set<Atlas> matchingAtlases;
    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new PropertySearchCommand().runSubcommandAndExit(args);
    }

    public PropertySearchCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int finish()
    {
        if (this.optionAndArgumentDelegate.hasOption(COLLECT_OPTION_LONG)
                && !this.matchingAtlases.isEmpty())
        {
            final Atlas outputAtlas = new MultiAtlas(this.matchingAtlases);
            final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                    OUTPUT_ATLAS);
            final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
            new PackedAtlasCloner().cloneFrom(outputAtlas).save(outputFile);
            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate
                        .printlnCommandMessage("saved to " + concatenatedPath.toString());
            }
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "find-property";
    }

    @Override
    public String getSimpleDescription()
    {
        return "find features with given properties in given atlas(es)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", PropertySearchCommand.class
                .getResourceAsStream("PropertySearchCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", PropertySearchCommand.class
                .getResourceAsStream("PropertySearchCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(GEOMETRY_OPTION_LONG, GEOMETRY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, GEOMETRY_OPTION_HINT);
        registerOption(COLLECT_OPTION_LONG, COLLECT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    @Override
    public int start()
    {
        this.wkts = this.optionAndArgumentDelegate
                .getOptionArgument(GEOMETRY_OPTION_LONG, this::parseWkts).orElse(new HashSet<>());
        this.matchingAtlases = new HashSet<>();

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        for (final AtlasEntity entity : atlas.entities())
        {

        }
    }

    private Set<String> parseWkts(final String wktString)
    {
        final Set<String> wktSet = new HashSet<>();

        if (wktString.isEmpty())
        {
            return wktSet;
        }
        Arrays.stream(wktString.split(":")).forEach(wktSet::add);
        return wktSet;
    }
}
