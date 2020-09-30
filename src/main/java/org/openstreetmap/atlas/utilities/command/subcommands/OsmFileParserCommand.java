package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;

/**
 * @author matthieun
 */
public class OsmFileParserCommand extends AbstractAtlasShellToolsCommand
{
    private static final String JOSM_OSM_FILE = "josm";
    private static final String OSM_FILE = "osm";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;

    public static void main(final String[] args)
    {
        new OsmFileParserCommand().runSubcommandAndExit(args);
    }

    public OsmFileParserCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
    }

    @Override
    public int execute()
    {
        final String josmOsmFile = this.optionAndArgumentDelegate.getUnaryArgument(JOSM_OSM_FILE)
                .orElseThrow(AtlasShellToolsException::new);
        final String osmFile = this.optionAndArgumentDelegate.getUnaryArgument(OSM_FILE)
                .orElseThrow(AtlasShellToolsException::new);
        new OsmFileParser().update(new File(josmOsmFile, this.getFileSystem()),
                new File(osmFile, this.getFileSystem()));
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "josm2osm";
    }

    @Override
    public String getSimpleDescription()
    {
        return "transform a JOSM OSM file into a real OSM file.";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", OsmFileParserCommand.class
                .getResourceAsStream("OsmFileParserCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", OsmFileParserCommand.class
                .getResourceAsStream("OsmFileParserCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(JOSM_OSM_FILE, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument(OSM_FILE, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }
}
