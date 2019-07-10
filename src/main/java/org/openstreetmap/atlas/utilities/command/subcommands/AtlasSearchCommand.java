package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * Search atlases for some given feature identifiers or properties, with various options and
 * restrictions. Draws some inspiration from similar identifier locater commands by cstaylor and
 * bbreithaupt.
 *
 * @author lcram
 * @author cstaylor
 * @author bbreithaupt
 */
public class AtlasSearchCommand extends AtlasLoaderCommand
{
    private static final String GEOMETRY_OPTION_LONG = "geometry";
    private static final String GEOMETRY_OPTION_DESCRIPTION = "A colon separated list of geometry WKTs for which to search.";
    private static final String GEOMETRY_OPTION_HINT = "wkt-geometry";

    private static final String TAGGABLEFILTER_OPTION_LONG = "taggableFilter";
    private static final String TAGGABLEFILTER_OPTION_DESCRIPTION = "A TaggableFilter by which to filter the search space.";
    private static final String TAGGABLEFILTER_OPTION_HINT = "filter";

    private static final String ID_OPTION_LONG = "id";
    private static final String ID_OPTION_DESCRIPTION = "A comma separated list of Atlas ids for which to search.";
    private static final String ID_OPTION_HINT = "ids";

    private static final String OSMID_OPTION_LONG = "osmid";
    private static final String OSMID_OPTION_DESCRIPTION = "A comma separated list of OSM ids for which to search.";
    private static final String OSMID_OPTION_HINT = "osmids";

    private static final List<String> ITEM_TYPE_STRINGS = Arrays.stream(ItemType.values())
            .map(ItemType::toString).collect(Collectors.toList());
    private static final String TYPES_OPTION_LONG = "types";
    private static final String TYPES_OPTION_DESCRIPTION = "A comma separated list of ItemTypes by which to narrow the search. Valid types are: "
            + new StringList(ITEM_TYPE_STRINGS).join(", ") + ". Defaults to including all values.";
    private static final String TYPES_OPTION_HINT = "types";

    private static final String OUTPUT_ATLAS = "collected-multi.atlas";
    private static final String COLLECT_OPTION_LONG = "collect-matching";
    private static final String COLLECT_OPTION_DESCRIPTION = "Collect all matching atlas files and save to a file using the MultiAtlas.";

    private Set<String> wkts;
    private TaggableFilter taggableFilter;

    private Set<Long> ids;
    private Set<Long> osmIds;
    private Set<ItemType> typesToCheck;

    private Set<Atlas> matchingAtlases;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AtlasSearchCommand().runSubcommandAndExit(args);
    }

    public AtlasSearchCommand()
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
        return "find";
    }

    @Override
    public String getSimpleDescription()
    {
        return "find features with given identifiers or properties in given atlas(es)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", AtlasSearchCommand.class
                .getResourceAsStream("AtlasSearchCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", AtlasSearchCommand.class
                .getResourceAsStream("AtlasSearchCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(GEOMETRY_OPTION_LONG, GEOMETRY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, GEOMETRY_OPTION_HINT);
        registerOptionWithRequiredArgument(TAGGABLEFILTER_OPTION_LONG,
                TAGGABLEFILTER_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                TAGGABLEFILTER_OPTION_HINT);

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
        /*
         * Parse typesToCheck first. We will overwrite this if necessary in the case that the user
         * provides a type specific search criteria (e.g. --startNode).
         */
        this.typesToCheck = this.optionAndArgumentDelegate
                .getOptionArgument(TYPES_OPTION_LONG, this::parseCommaSeparatedItemTypes)
                .orElse(Sets.hashSet(ItemType.values()));

        /*
         * Handle the various search properties.
         */
        this.wkts = this.optionAndArgumentDelegate
                .getOptionArgument(GEOMETRY_OPTION_LONG, this::parseColonSeparatedWkts)
                .orElse(new HashSet<>());
        this.taggableFilter = this.optionAndArgumentDelegate
                .getOptionArgument(TAGGABLEFILTER_OPTION_LONG, TaggableFilter::forDefinition)
                .orElse(null);

        /*
         * Handle identifier searches.
         */
        this.ids = this.optionAndArgumentDelegate
                .getOptionArgument(ID_OPTION_LONG, this::parseCommaSeparatedLongs)
                .orElse(new HashSet<>());
        this.osmIds = this.optionAndArgumentDelegate
                .getOptionArgument(OSMID_OPTION_LONG, this::parseCommaSeparatedLongs)
                .orElse(new HashSet<>());

        this.matchingAtlases = new HashSet<>();

        if (this.typesToCheck.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no ItemTypes were successfully parsed");
            return 1;
        }

        if (this.ids.isEmpty() && this.osmIds.isEmpty() && this.wkts.isEmpty()
                && this.taggableFilter == null)
        {
            this.outputDelegate
                    .printlnErrorMessage("no ids or properties were successfully parsed");
            return 1;
        }

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        /*
         * This loop is O(N) (where N is the number of atlas entities), assuming the lists of
         * provided evaluation properties are much smaller than the size of the entity set.
         */
        for (final AtlasEntity entity : atlas.entities())
        {
            boolean entityMatchesAllCriteriaSoFar = true;
            if (!this.typesToCheck.contains(entity.getType()))
            {
                continue;
            }
            if (this.taggableFilter != null && !this.taggableFilter.test(entity))
            {
                continue;
            }

            if (!this.ids.isEmpty() && !this.ids.contains(entity.getIdentifier()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }

            if (entityMatchesAllCriteriaSoFar && !this.osmIds.isEmpty()
                    && !this.osmIds.contains(entity.getOsmIdentifier()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }

            if (entityMatchesAllCriteriaSoFar && !this.wkts.isEmpty()
                    && !this.wkts.contains(entity.toWkt()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }

            /*
             * If we made it here while matching all criteria, then we can print a diagnostic
             * detailing the find.
             */
            if (entityMatchesAllCriteriaSoFar)
            {
                this.outputDelegate.printlnStdout(
                        "Found entity matching criteria in " + atlasResource.getPath() + ":",
                        TTYAttribute.BOLD);
                this.outputDelegate.printlnStdout(entity.toDiffViewFriendlyString(),
                        TTYAttribute.GREEN);
                this.outputDelegate.printlnStdout("");
                this.matchingAtlases.add(atlas);
            }
        }
    }

    private Set<String> parseColonSeparatedWkts(final String wktString)
    {
        final Set<String> wktSet = new HashSet<>();

        if (wktString.isEmpty())
        {
            return wktSet;
        }

        final WKTReader reader = new WKTReader();
        Arrays.stream(wktString.split(":")).forEach(wkt ->
        {
            try
            {
                reader.read(wkt);
                wktSet.add(wkt);
            }
            catch (final ParseException exception)
            {
                this.outputDelegate
                        .printlnWarnMessage("could not parse wkt \'" + wkt + "\': skipping...");
            }
        });
        return wktSet;
    }

    private Set<ItemType> parseCommaSeparatedItemTypes(final String typeString)
    {
        final Set<ItemType> typeSet = new HashSet<>();

        if (typeString.isEmpty())
        {
            return typeSet;
        }

        final String[] typeStringSplit = typeString.split(",");
        for (final String typeElement : typeStringSplit)
        {
            final ItemType type;
            try
            {
                type = ItemType.valueOf(typeElement.toUpperCase());
                typeSet.add(type);
            }
            catch (final IllegalArgumentException exception)
            {
                this.outputDelegate.printlnWarnMessage(
                        "could not parse ItemType \'" + typeElement + "\': skipping...");
            }
        }
        return typeSet;
    }

    private Set<Long> parseCommaSeparatedLongs(final String idString)
    {
        final Set<Long> idSet = new HashSet<>();

        if (idString.isEmpty())
        {
            return idSet;
        }

        final String[] idStringSplit = idString.split(",");
        for (final String idElement : idStringSplit)
        {
            final Long identifier;
            try
            {
                identifier = Long.parseLong(idElement);
                idSet.add(identifier);
            }
            catch (final NumberFormatException exception)
            {
                this.outputDelegate.printlnWarnMessage(
                        "could not parse id \'" + idElement + "\': skipping...");
            }
        }
        return idSet;
    }

    private Set<String> parseCommaSeparatedStrings(final String string)
    {
        final Set<String> stringSet = new HashSet<>();

        if (string.isEmpty())
        {
            return stringSet;
        }

        final String[] stringSplit = string.split(",");
        Arrays.stream(stringSplit).forEach(stringSet::add);
        return stringSet;
    }
}
