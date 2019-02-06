package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * Search atlases for some given feature identifiers, with various options and restrictions. Based
 * on similar identifier locater commands by cstaylor and bbreithaupt.
 *
 * @author lcram
 * @author cstaylor
 * @author bbreithaupt
 */
public class IdentifierSearchCommand extends AtlasLoaderCommand
{
    private static final String ID_OPTION_LONG = "id";
    private static final String ID_OPTION_DESCRIPTION = "A comma separated list of Atlas ids for which to search.";
    private static final String ID_OPTION_HINT = "ids";

    private static final String OSMID_OPTION_LONG = "osmid";
    private static final String OSMID_OPTION_DESCRIPTION = "A comma separated list of OSM ids for which to search.";
    private static final String OSMID_OPTION_HINT = "osmids";

    private Set<Long> ids;
    private Set<Long> osmIds;
    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new IdentifierSearchCommand().runSubcommandAndExit(args);
    }

    public IdentifierSearchCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public String getCommandName()
    {
        return "find-id";
    }

    @Override
    public String getSimpleDescription()
    {
        return "find features with given identifier(s) in given atlas(es)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", IdentifierSearchCommand.class
                .getResourceAsStream("IdentifierSearchCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", IdentifierSearchCommand.class
                .getResourceAsStream("IdentifierSearchCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(ID_OPTION_LONG, ID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, ID_OPTION_HINT);
        registerOptionWithRequiredArgument(OSMID_OPTION_LONG, OSMID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OSMID_OPTION_HINT);
        super.registerOptionsAndArguments();
    }

    @Override
    public int start()
    {
        this.ids = this.optionAndArgumentDelegate.getOptionArgument(ID_OPTION_LONG, this::parseIds)
                .orElse(new HashSet<>());
        this.osmIds = this.optionAndArgumentDelegate
                .getOptionArgument(OSMID_OPTION_LONG, this::parseIds).orElse(new HashSet<>());

        if (this.ids.isEmpty() && this.osmIds.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no ids were successfully parsed");
            return 1;
        }

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName)
    {
        for (final Long atlasId : this.ids)
        {
            for (final ItemType type : ItemType.values())
            {
                final AtlasEntity entity = atlas.entity(atlasId, type);
                if (entity != null)
                {
                    this.outputDelegate.printlnStdout(
                            "Found entity with atlas ID " + atlasId + " in " + atlasFileName + ":",
                            TTYAttribute.BOLD);
                    this.outputDelegate.printlnStdout(entity.toString(), TTYAttribute.GREEN);
                    this.outputDelegate.printlnStdout("");
                }
            }
        }

        /*
         * This loop is O(N) (where N is the number of atlas entities), assuming the list of osmIds
         * is much smaller than the size of of the entity set.
         */
        for (final AtlasEntity entity : atlas.entities())
        {
            for (final Long osmId : this.osmIds)
            {
                if (osmId.longValue() == entity.getOsmIdentifier())
                {
                    this.outputDelegate.printlnStdout(
                            "Found entity with OSM ID " + osmId + " in " + atlasFileName + ":",
                            TTYAttribute.BOLD);
                    this.outputDelegate.printlnStdout(entity.toString(), TTYAttribute.GREEN);
                    this.outputDelegate.printlnStdout("");
                }
            }
        }
    }

    private Set<Long> parseIds(final String idString)
    {
        final Set<Long> idSet = new HashSet<>();

        if (idString.isEmpty())
        {
            return idSet;
        }

        final String[] idStringSplit = idString.split(",");
        for (final String idElement : idStringSplit)
        {
            Long id;
            try
            {
                id = Long.parseLong(idElement);
                idSet.add(id);
            }
            catch (final NumberFormatException exception)
            {
                this.outputDelegate
                        .printlnWarnMessage("could not parse id " + idElement + ": skipping...");
            }
        }
        return idSet;
    }
}
