package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.collections.StringList;
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

    private static final String OUTPUT_ATLAS = "collected-multi.atlas";
    private static final String COLLECT_OPTION_LONG = "collect-matching";
    private static final String COLLECT_OPTION_DESCRIPTION = "Collect all matching atlas files and save to a file using the MultiAtlas.";

    private static final List<String> ITEM_TYPE_STRINGS = Arrays.stream(ItemType.values())
            .map(ItemType::toString).collect(Collectors.toList());
    private static final String TYPES_OPTION_LONG = "types";
    private static final String TYPES_OPTION_DESCRIPTION = "A comma separated list of ItemTypes by which to narrow the search. Valid types are: "
            + new StringList(ITEM_TYPE_STRINGS).join(", ") + ". Defaults to including all values.";
    private static final String TYPES_OPTION_HINT = "types";

    private Set<Long> ids;
    private Set<Long> osmIds;
    private Set<ItemType> typesToCheck;
    private Set<Atlas> matchingAtlases;
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
        }

        return 0;
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
        registerOptionWithRequiredArgument(TYPES_OPTION_LONG, TYPES_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, TYPES_OPTION_HINT);
        registerOption(COLLECT_OPTION_LONG, COLLECT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    @Override
    public int start()
    {
        this.ids = this.optionAndArgumentDelegate.getOptionArgument(ID_OPTION_LONG, this::parseIds)
                .orElse(new HashSet<>());
        this.osmIds = this.optionAndArgumentDelegate
                .getOptionArgument(OSMID_OPTION_LONG, this::parseIds).orElse(new HashSet<>());
        this.typesToCheck = this.optionAndArgumentDelegate
                .getOptionArgument(TYPES_OPTION_LONG, this::parseItemTypes)
                .orElse(Sets.hashSet(ItemType.values()));
        this.matchingAtlases = new HashSet<>();

        if (this.typesToCheck.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no ItemTypes were successfully parsed");
            return 1;
        }

        if (this.ids.isEmpty() && this.osmIds.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no ids were successfully parsed");
            return 1;
        }

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        for (final Long atlasId : this.ids)
        {
            for (final ItemType type : this.typesToCheck)
            {
                final AtlasEntity entity = atlas.entity(atlasId, type);
                if (entity != null)
                {
                    this.outputDelegate.printlnStdout("Found entity with atlas ID " + atlasId
                            + " in " + atlasResource.getPath() + ":", TTYAttribute.BOLD);
                    this.outputDelegate.printlnStdout(entity.toString(), TTYAttribute.GREEN);
                    this.outputDelegate.printlnStdout("");
                    this.matchingAtlases.add(atlas);
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
                if (osmId.longValue() == entity.getOsmIdentifier()
                        && this.typesToCheck.contains(entity.getType()))
                {
                    this.outputDelegate.printlnStdout("Found entity with OSM ID " + osmId + " in "
                            + atlasResource.getPath() + ":", TTYAttribute.BOLD);
                    this.outputDelegate.printlnStdout(entity.toString(), TTYAttribute.GREEN);
                    this.outputDelegate.printlnStdout("");
                    this.matchingAtlases.add(atlas);
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

    private Set<ItemType> parseItemTypes(final String typeString)
    {
        final Set<ItemType> typeSet = new HashSet<>();

        if (typeString.isEmpty())
        {
            return typeSet;
        }

        final String[] typeStringSplit = typeString.split(",");
        for (final String typeElement : typeStringSplit)
        {
            ItemType type;
            try
            {
                type = ItemType.valueOf(typeElement.toUpperCase());
                typeSet.add(type);
            }
            catch (final IllegalArgumentException exception)
            {
                this.outputDelegate.printlnWarnMessage(
                        "could not parse ItemType " + typeElement + ": skipping...");
            }
        }
        return typeSet;
    }
}
