package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
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
    private static final List<String> ITEM_TYPE_STRINGS = Arrays.stream(ItemType.values())
            .map(ItemType::toString).collect(Collectors.toList());
    private static final String TYPES_OPTION_LONG = "type";
    private static final String TYPES_OPTION_DESCRIPTION = "A comma separated list of ItemTypes by which to narrow the search. Valid types are: "
            + new StringList(ITEM_TYPE_STRINGS).join(", ")
            + ". Defaults to including all values, unless another option (e.g. --startNode) automatically narrows the search space.";
    private static final String TYPES_OPTION_HINT = "types";

    private static final String GEOMETRY_OPTION_LONG = "geometry";
    private static final String GEOMETRY_OPTION_DESCRIPTION = "A colon separated list of geometry WKTs for which to search.";
    private static final String GEOMETRY_OPTION_HINT = "wkt-geometry";

    private static final String TAGGABLEFILTER_OPTION_LONG = "taggableFilter";
    private static final String TAGGABLEFILTER_OPTION_DESCRIPTION = "A TaggableFilter by which to filter the search space.";
    private static final String TAGGABLEFILTER_OPTION_HINT = "filter";

    private static final String STARTNODE_OPTION_LONG = "startNode";
    private static final String STARTNODE_OPTION_DESCRIPTION = "A comma separated list of start node identifiers for which to search.";
    private static final String STARTNODE_OPTION_HINT = "ids";

    private static final String ENDNODE_OPTION_LONG = "endNode";
    private static final String ENDNODE_OPTION_DESCRIPTION = "A comma separated list of end node identifiers for which to search.";
    private static final String ENDNODE_OPTION_HINT = "ids";

    private static final String INEDGE_OPTION_LONG = "inEdge";
    private static final String INEDGE_OPTION_DESCRIPTION = "A comma separated list of in edge identifiers for which to search.";
    private static final String INEDGE_OPTION_HINT = "ids";

    private static final String OUTEDGE_OPTION_LONG = "outEdge";
    private static final String OUTEDGE_OPTION_DESCRIPTION = "A comma separated list of out edge identifiers for which to search.";
    private static final String OUTEDGE_OPTION_HINT = "ids";

    private static final String PARENTRELATIONS_OPTION_LONG = "parentRelations";
    private static final String PARENTRELATIONS_OPTION_DESCRIPTION = "A comma separated list of parent relation identifiers for which to search.";
    private static final String PARENTRELATIONS_OPTION_HINT = "ids";

    private static final String ID_OPTION_LONG = "id";
    private static final String ID_OPTION_DESCRIPTION = "A comma separated list of Atlas ids for which to search.";
    private static final String ID_OPTION_HINT = "ids";

    private static final String OSMID_OPTION_LONG = "osmid";
    private static final String OSMID_OPTION_DESCRIPTION = "A comma separated list of OSM ids for which to search.";
    private static final String OSMID_OPTION_HINT = "osmids";

    private static final String OUTPUT_ATLAS = "collected-multi.atlas";
    private static final String COLLECT_OPTION_LONG = "collect-matching";
    private static final String COLLECT_OPTION_DESCRIPTION = "Collect all matching atlas files and save to a file using the MultiAtlas.";

    private static final Integer ALL_TYPES_CONTEXT = 3;
    private static final Integer EDGE_ONLY_CONTEXT = 4;
    private static final Integer NODE_ONLY_CONTEXT = 5;

    private Set<String> wkts;
    private TaggableFilter taggableFilter;
    private Set<Long> startNodeIds;
    private Set<Long> endNodeIds;
    private Set<Long> inEdgeIds;
    private Set<Long> outEdgeIds;
    private Set<Long> parentRelations;

    private Set<Long> ids;
    private Set<Long> osmIds;
    private Set<ItemType> typesToCheck;

    private Set<Atlas> matchingAtlases;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private boolean unitTestMode = false;
    private final List<AtlasEntity> matchedEntities = new ArrayList<>();

    public static void main(final String[] args)
    {
        new AtlasSearchCommand().runSubcommandAndExit(args);
    }

    public AtlasSearchCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
        this.typesToCheck = new HashSet<>();
        this.startNodeIds = new HashSet<>();
        this.endNodeIds = new HashSet<>();
        this.inEdgeIds = new HashSet<>();
        this.outEdgeIds = new HashSet<>();
        this.parentRelations = new HashSet<>();
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

    public List<AtlasEntity> getMatchedEntities()
    {
        return this.matchedEntities;
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
        registerOptionWithRequiredArgument(TYPES_OPTION_LONG, TYPES_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, TYPES_OPTION_HINT, ALL_TYPES_CONTEXT);

        registerOptionWithRequiredArgument(GEOMETRY_OPTION_LONG, GEOMETRY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, GEOMETRY_OPTION_HINT, ALL_TYPES_CONTEXT,
                EDGE_ONLY_CONTEXT, NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(TAGGABLEFILTER_OPTION_LONG,
                TAGGABLEFILTER_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                TAGGABLEFILTER_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(STARTNODE_OPTION_LONG, STARTNODE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, STARTNODE_OPTION_HINT, EDGE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(ENDNODE_OPTION_LONG, ENDNODE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, ENDNODE_OPTION_HINT, EDGE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(INEDGE_OPTION_LONG, INEDGE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, INEDGE_OPTION_HINT, NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(OUTEDGE_OPTION_LONG, OUTEDGE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OUTEDGE_OPTION_HINT, NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(PARENTRELATIONS_OPTION_LONG,
                PARENTRELATIONS_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                PARENTRELATIONS_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT);

        registerOptionWithRequiredArgument(ID_OPTION_LONG, ID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, ID_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(OSMID_OPTION_LONG, OSMID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OSMID_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT);

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
        if (this.optionAndArgumentDelegate.getParserContext() == ALL_TYPES_CONTEXT)
        {
            this.typesToCheck = this.optionAndArgumentDelegate
                    .getOptionArgument(TYPES_OPTION_LONG, this::parseCommaSeparatedItemTypes)
                    .orElse(Sets.hashSet(ItemType.values()));
        }

        /*
         * Handle the various search properties.
         */
        this.wkts = this.optionAndArgumentDelegate
                .getOptionArgument(GEOMETRY_OPTION_LONG, this::parseColonSeparatedWkts)
                .orElse(new HashSet<>());
        this.taggableFilter = this.optionAndArgumentDelegate
                .getOptionArgument(TAGGABLEFILTER_OPTION_LONG, TaggableFilter::forDefinition)
                .orElse(null);
        if (this.optionAndArgumentDelegate.getParserContext() == EDGE_ONLY_CONTEXT)
        {
            this.typesToCheck.add(ItemType.EDGE);
            this.startNodeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(STARTNODE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
            this.endNodeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(ENDNODE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
        }
        if (this.optionAndArgumentDelegate.getParserContext() == NODE_ONLY_CONTEXT)
        {
            this.typesToCheck.add(ItemType.NODE);
            this.inEdgeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(INEDGE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
            this.outEdgeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(OUTEDGE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
        }
        this.parentRelations = this.optionAndArgumentDelegate
                .getOptionArgument(PARENTRELATIONS_OPTION_LONG, this::parseCommaSeparatedLongs)
                .orElse(new HashSet<>());

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

        if (this.typesToCheck.isEmpty() && this.wkts.isEmpty() && this.taggableFilter == null
                && this.startNodeIds.isEmpty() && this.endNodeIds.isEmpty()
                && this.parentRelations.isEmpty() && this.ids.isEmpty() && this.osmIds.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage(
                    "no ids, properties, or ItemTypes were successfully parsed");
            return 1;
        }

        return 0;
    }

    public AtlasSearchCommand withUnitTestMode()
    {
        this.unitTestMode = true;
        return this;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName, // NO SONAR
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
            if (this.optionAndArgumentDelegate.getParserContext() == NODE_ONLY_CONTEXT)
            {
                final Node node = (Node) entity;
                final Set<Long> intersectingInEdgeIdentifiers = com.google.common.collect.Sets
                        .intersection(node.inEdges().stream().map(Edge::getIdentifier)
                                .collect(Collectors.toSet()), this.inEdgeIds);
                final Set<Long> intersectingOutEdgeIdentifiers = com.google.common.collect.Sets
                        .intersection(node.outEdges().stream().map(Edge::getIdentifier)
                                .collect(Collectors.toSet()), this.outEdgeIds);
                if (entityMatchesAllCriteriaSoFar && !this.inEdgeIds.isEmpty()
                        && intersectingInEdgeIdentifiers.isEmpty())
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
                if (entityMatchesAllCriteriaSoFar && !this.outEdgeIds.isEmpty()
                        && intersectingOutEdgeIdentifiers.isEmpty())
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
            }
            if (this.optionAndArgumentDelegate.getParserContext() == EDGE_ONLY_CONTEXT)
            {
                final Edge edge = (Edge) entity;
                if (entityMatchesAllCriteriaSoFar && !this.startNodeIds.isEmpty()
                        && !this.startNodeIds.contains(edge.start().getIdentifier()))
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
                if (entityMatchesAllCriteriaSoFar && !this.endNodeIds.isEmpty()
                        && !this.endNodeIds.contains(edge.end().getIdentifier()))
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
            }
            if (entityMatchesAllCriteriaSoFar && !this.parentRelations.isEmpty())
            {
                final Set<Long> intersectingParentRelationIdentifiers = com.google.common.collect.Sets
                        .intersection(entity.relations().stream().map(Relation::getIdentifier)
                                .collect(Collectors.toSet()), this.parentRelations);
                if (intersectingParentRelationIdentifiers.isEmpty())
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
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
                if (this.unitTestMode)
                {
                    this.matchedEntities.add(entity);
                }
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
}
