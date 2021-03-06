package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.PrettifyStringFormat;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.PredicateTemplate;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Search atlases for some given feature identifiers or properties, with various options and
 * restrictions. Draws some inspiration from similar identifier locator commands by cstaylor and
 * bbreithaupt.
 *
 * @author lcram
 */
// Some future improvements
// + fix RelationMember delimiting so that roles with ';' won't break everything?
public class AtlasSearchCommand extends AtlasLoaderCommand
{
    /**
     * @author lcram
     */
    private static class RelationMemberSearchConstraint
    {
        private ItemType type = null;
        private Long identifier = null;
        private String role = null;

        RelationMemberSearchConstraint()
        {
        }

        /**
         * Check if this constraint matches at least one member of a given {@link Relation}'s member
         * list.
         *
         * @param relation
         *            the {@link Relation} against which to check
         * @return if there is a match
         */
        boolean matches(final Relation relation)
        {
            boolean constraintMatchedAnItem = false;
            for (final RelationBean.RelationBeanItem item : relation.getBean())
            {
                boolean constraintMatchedType = true;
                if (this.type != null && !this.type.equals(item.getType()))
                {
                    constraintMatchedType = false;
                }
                boolean constraintMatchedId = true;
                if (this.identifier != null && !this.identifier.equals(item.getIdentifier()))
                {
                    constraintMatchedId = false;
                }
                boolean constraintMatchedRole = true;
                if (this.role != null && !this.role.equals(item.getRole()))
                {
                    constraintMatchedRole = false;
                }

                constraintMatchedAnItem = constraintMatchedType && constraintMatchedId
                        && constraintMatchedRole;

                /*
                 * Break early if we match an item, since we don't need to check any more.
                 */
                if (constraintMatchedAnItem)
                {
                    break;
                }
            }

            return constraintMatchedAnItem;
        }

        RelationMemberSearchConstraint withId(final Long identifier)
        {
            this.identifier = identifier;
            return this;
        }

        RelationMemberSearchConstraint withRole(final String role)
        {
            this.role = role;
            return this;
        }

        RelationMemberSearchConstraint withType(final ItemType type)
        {
            this.type = type;
            return this;
        }
    }

    private static final List<String> ITEM_TYPE_STRINGS = Arrays.stream(ItemType.values())
            .map(ItemType::toString).collect(Collectors.toList());
    private static final String TYPES_OPTION_LONG = "type";
    private static final String TYPES_OPTION_DESCRIPTION = "A comma separated list of ItemTypes by which to narrow the search. Valid types are: "
            + new StringList(ITEM_TYPE_STRINGS).join(", ")
            + ". Defaults to including all values, unless another option (e.g. --startNode) automatically narrows the search space.";
    private static final String TYPES_OPTION_HINT = "types";

    private static final String BOUNDING_POLYGON_OPTION_LONG = "bounding-polygons";
    private static final String BOUNDING_POLYGON_OPTION_DESCRIPTION = "Match all features within at least one member of a given colon separated list of bounding polygons.";
    private static final String BOUNDING_POLYGON_OPTION_HINT = "wkt-polygons";

    private static final String GEOMETRY_OPTION_LONG = "geometry";
    private static final String GEOMETRY_OPTION_DESCRIPTION = "A colon separated list of exact geometry WKTs for which to search.";
    private static final String GEOMETRY_OPTION_HINT = "wkt-geometry";

    private static final String SUB_GEOMETRY_OPTION_LONG = "sub-geometry";
    private static final String SUB_GEOMETRY_OPTION_DESCRIPTION = "Like --geometry, but can match against contained geometry. E.g. POINT(2 2) would match LINESTRING(1 1, 2 2, 3 3).";
    private static final String SUB_GEOMETRY_OPTION_HINT = "wkt-geometry";

    private static final String TAGGABLEFILTER_OPTION_LONG = "tag-filter";
    private static final String TAGGABLEFILTER_OPTION_DESCRIPTION = "A TaggableFilter by which to filter the search space.";
    private static final String TAGGABLEFILTER_OPTION_HINT = "filter";

    private static final String TAGGABLEMATCHER_OPTION_LONG = "tag-matcher";
    private static final String TAGGABLEMATCHER_OPTION_DESCRIPTION = "A TaggableMatcher by which to filter the search space.";
    private static final String TAGGABLEMATCHER_OPTION_HINT = "matcher";

    private static final String STARTNODE_OPTION_LONG = "start-nodes";
    private static final String STARTNODE_OPTION_DESCRIPTION = "A comma separated list of start node identifiers for which to search.";
    private static final String STARTNODE_OPTION_HINT = "ids";

    private static final String ENDNODE_OPTION_LONG = "end-nodes";
    private static final String ENDNODE_OPTION_DESCRIPTION = "A comma separated list of end node identifiers for which to search.";
    private static final String ENDNODE_OPTION_HINT = "ids";

    private static final String INEDGE_OPTION_LONG = "in-edges";
    private static final String INEDGE_OPTION_DESCRIPTION = "A comma separated list of in edge identifiers for which to search.";
    private static final String INEDGE_OPTION_HINT = "ids";

    private static final String OUTEDGE_OPTION_LONG = "out-edges";
    private static final String OUTEDGE_OPTION_DESCRIPTION = "A comma separated list of out edge identifiers for which to search.";
    private static final String OUTEDGE_OPTION_HINT = "ids";

    private static final String PARENT_RELATIONS_OPTION_LONG = "parent-relations";
    private static final String PARENT_RELATIONS_OPTION_DESCRIPTION = "A comma separated list of parent relation identifiers for which to search.";
    private static final String PARENT_RELATIONS_OPTION_HINT = "ids";

    private static final String RELATION_MEMBERS_OR_OPTION_LONG = "relation-members";
    private static final String RELATION_MEMBERS_OR_OPTION_DESCRIPTION = "Filter to relations that contain at least one of the given semicolon separated members."
            + " Members can be specified like e.g. `AREA,1234,myrole;EDGE,4567,*;*,9012,*'."
            + " Here you can see you may optionally supply a `*' wildcard to broaden the search constraints."
            + " This example string filters for relations that contain an Area with ID 1234 and role `myrole', OR an Edge with ID 4567 and any role, OR any feature type with ID 9012 and any role.";
    private static final String RELATION_MEMBERS_OR_OPTION_HINT = "member[;member]...";

    private static final String RELATION_MEMBERS_AND_OPTION_LONG = "and-relation-members";
    private static final String RELATION_MEMBERS_AND_OPTION_DESCRIPTION = "Filter to relations that contain all of the given semicolon separated members."
            + " Members can be specified like e.g. `EDGE,*,to;EDGE,*,from;NODE,1234,via'."
            + " Here you can see you may optionally supply a `*' wildcard to broaden the search constraints."
            + " This example string filters for relations that contain a `to' AND `from' Edge with any ID, as well as a Node 1234 with role `via'.";
    private static final String RELATION_MEMBERS_AND_OPTION_HINT = "member[;member]...";

    private static final String ID_OPTION_LONG = "id";
    private static final String ID_OPTION_DESCRIPTION = "A comma separated list of Atlas ids for which to search.";
    private static final String ID_OPTION_HINT = "ids";

    private static final String OSMID_OPTION_LONG = "osmid";
    private static final String OSMID_OPTION_DESCRIPTION = "A comma separated list of OSM ids for which to search.";
    private static final String OSMID_OPTION_HINT = "osmids";

    private static final String ALL_OPTION_LONG = "all";
    private static final String ALL_OPTION_DESCRIPTION = "Ignore all other criteria and just print all entities.";

    private static final String JSON_OPTION_LONG = "json";
    private static final String JSON_OPTION_DESCRIPTION = "Print matches in a parsable JSON format. For e.g., try chaining output into `jq' for more flexibility.";

    private static final String OUTPUT_ATLAS = "collected-multi.atlas";
    private static final String COLLECT_OPTION_LONG = "collect-matching";
    private static final String COLLECT_OPTION_DESCRIPTION = "Collect all matching atlas files and save to a file using the MultiAtlas.";

    private static final Integer ALL_TYPES_CONTEXT = 3;
    private static final Integer EDGE_ONLY_CONTEXT = 4;
    private static final Integer NODE_ONLY_CONTEXT = 5;
    private static final Integer RELATION_ONLY_CONTEXT = 6;
    private static final Integer SHOW_ALL_CONTEXT = 7;

    private static final String COULD_NOT_PARSE = "could not parse %s '%s'";

    private static final List<String> IMPORTS_ALLOW_LIST = Arrays.asList(
            "org.openstreetmap.atlas.geography.atlas.items",
            "org.openstreetmap.atlas.tags.annotations",
            "org.openstreetmap.atlas.tags.annotations.validation",
            "org.openstreetmap.atlas.tags.annotations.extraction", "org.openstreetmap.atlas.tags",
            "org.openstreetmap.atlas.tags.names", "org.openstreetmap.atlas.geography",
            "org.openstreetmap.atlas.utilities.collections");
    private static final String WILDCARD = "*";

    private Set<String> geometryWkts;
    private Set<String> subGeometryWkts;
    private Set<String> boundingWkts;
    private TaggableFilter taggableFilter;
    private TaggableMatcher taggableMatcher;
    private Set<Long> startNodeIds;
    private Set<Long> endNodeIds;
    private Set<Long> inEdgeIds;
    private Set<Long> outEdgeIds;
    private Set<Long> parentRelations;
    private Set<RelationMemberSearchConstraint> relationMemberConstraintsOR;
    private Set<RelationMemberSearchConstraint> relationMemberConstraintsAND;
    private Predicate<AtlasEntity> predicate;

    private Set<Long> ids;
    private Set<Long> osmIds;
    private Set<ItemType> typesToCheckFromOption;
    private final Set<ItemType> impliedTypesToCheck;

    private final Set<Atlas> matchingAtlases;

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
        this.geometryWkts = new HashSet<>();
        this.subGeometryWkts = new HashSet<>();
        this.boundingWkts = new HashSet<>();
        this.startNodeIds = new HashSet<>();
        this.endNodeIds = new HashSet<>();
        this.inEdgeIds = new HashSet<>();
        this.outEdgeIds = new HashSet<>();
        this.parentRelations = new HashSet<>();
        this.relationMemberConstraintsOR = new HashSet<>();
        this.relationMemberConstraintsAND = new HashSet<>();

        this.ids = new HashSet<>();
        this.osmIds = new HashSet<>();
        this.typesToCheckFromOption = new HashSet<>();
        this.impliedTypesToCheck = new HashSet<>();

        this.matchingAtlases = new HashSet<>();
    }

    @Override
    public int finish()
    {
        if (this.optionAndArgumentDelegate.hasOption(COLLECT_OPTION_LONG)
                && !this.matchingAtlases.isEmpty())
        {
            final Path concatenatedPath = this.getFileSystem()
                    .getPath(getOutputPath().toAbsolutePath().toString(), OUTPUT_ATLAS);
            final File outputFile = new File(concatenatedPath.toAbsolutePath().toString(),
                    this.getFileSystem());
            final Atlas outputAtlas;
            if (this.matchingAtlases.size() == 1)
            {
                outputAtlas = new ArrayList<>(this.matchingAtlases).get(0);
                outputAtlas.save(outputFile);
            }
            else
            {
                outputAtlas = new MultiAtlas(this.matchingAtlases);
                new PackedAtlasCloner().cloneFrom(outputAtlas).save(outputFile);
            }

            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate
                        .printlnCommandMessage("saved to " + concatenatedPath.toString());
            }
        }

        if (this.matchingAtlases.isEmpty())
        {
            return 1;
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
        registerManualPageSectionsFromTemplate(new PredicateTemplate());
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(TYPES_OPTION_LONG, TYPES_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, TYPES_OPTION_HINT, ALL_TYPES_CONTEXT);

        registerOptionWithRequiredArgument(BOUNDING_POLYGON_OPTION_LONG,
                BOUNDING_POLYGON_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                BOUNDING_POLYGON_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(GEOMETRY_OPTION_LONG, GEOMETRY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, GEOMETRY_OPTION_HINT, ALL_TYPES_CONTEXT,
                EDGE_ONLY_CONTEXT, NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(SUB_GEOMETRY_OPTION_LONG,
                SUB_GEOMETRY_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                SUB_GEOMETRY_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT, NODE_ONLY_CONTEXT,
                RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(TAGGABLEFILTER_OPTION_LONG,
                TAGGABLEFILTER_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                TAGGABLEFILTER_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT, NODE_ONLY_CONTEXT,
                RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(TAGGABLEMATCHER_OPTION_LONG,
                TAGGABLEMATCHER_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                TAGGABLEMATCHER_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(STARTNODE_OPTION_LONG, STARTNODE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, STARTNODE_OPTION_HINT, EDGE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(ENDNODE_OPTION_LONG, ENDNODE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, ENDNODE_OPTION_HINT, EDGE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(INEDGE_OPTION_LONG, INEDGE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, INEDGE_OPTION_HINT, NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(OUTEDGE_OPTION_LONG, OUTEDGE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OUTEDGE_OPTION_HINT, NODE_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(PARENT_RELATIONS_OPTION_LONG,
                PARENT_RELATIONS_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                PARENT_RELATIONS_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(RELATION_MEMBERS_OR_OPTION_LONG,
                RELATION_MEMBERS_OR_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                RELATION_MEMBERS_OR_OPTION_HINT, RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(RELATION_MEMBERS_AND_OPTION_LONG,
                RELATION_MEMBERS_AND_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                RELATION_MEMBERS_AND_OPTION_HINT, RELATION_ONLY_CONTEXT);
        registerOptionsAndArgumentsFromTemplate(new PredicateTemplate(ALL_TYPES_CONTEXT,
                EDGE_ONLY_CONTEXT, NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT));

        registerOptionWithRequiredArgument(ID_OPTION_LONG, ID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, ID_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT);
        registerOptionWithRequiredArgument(OSMID_OPTION_LONG, OSMID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OSMID_OPTION_HINT, ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT,
                NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT);

        registerOption(ALL_OPTION_LONG, ALL_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                SHOW_ALL_CONTEXT);
        registerOption(JSON_OPTION_LONG, JSON_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                ALL_TYPES_CONTEXT, EDGE_ONLY_CONTEXT, NODE_ONLY_CONTEXT, RELATION_ONLY_CONTEXT,
                SHOW_ALL_CONTEXT);

        registerOption(COLLECT_OPTION_LONG, COLLECT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    @Override
    public int start()
    {
        /*
         * Get the types we will need to search for, implied by the option context. So for example,
         * if the user supplied an option that indicates they are only interested in Relations (e.g.
         * --relation-members), then we can set the impliedTypesToCheck to contain only RELATION.
         */
        if (this.optionAndArgumentDelegate.getParserContext() == ALL_TYPES_CONTEXT)
        {
            this.impliedTypesToCheck.addAll(Sets.hashSet(ItemType.values()));
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == NODE_ONLY_CONTEXT)
        {
            this.impliedTypesToCheck.add(ItemType.NODE);
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == EDGE_ONLY_CONTEXT)
        {
            this.impliedTypesToCheck.add(ItemType.EDGE);
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == RELATION_ONLY_CONTEXT)
        {
            this.impliedTypesToCheck.add(ItemType.RELATION);
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == SHOW_ALL_CONTEXT)
        {
            this.impliedTypesToCheck.addAll(Sets.hashSet(ItemType.values()));
            /*
             * If our context is from --all, we don't need to do anything else.
             */
            return 0;
        }

        /*
         * Handle the various search properties.
         */
        if (this.optionAndArgumentDelegate.getParserContext() == ALL_TYPES_CONTEXT)
        {
            this.typesToCheckFromOption = this.optionAndArgumentDelegate
                    .getOptionArgument(TYPES_OPTION_LONG, this::parseCommaSeparatedItemTypes)
                    .orElse(new HashSet<>());
        }
        this.boundingWkts = this.optionAndArgumentDelegate
                .getOptionArgument(BOUNDING_POLYGON_OPTION_LONG, this::parseColonSeparatedWkts)
                .orElse(new HashSet<>());
        this.geometryWkts = this.optionAndArgumentDelegate
                .getOptionArgument(GEOMETRY_OPTION_LONG, this::parseColonSeparatedWkts)
                .orElse(new HashSet<>());
        this.subGeometryWkts = this.optionAndArgumentDelegate
                .getOptionArgument(SUB_GEOMETRY_OPTION_LONG, this::parseColonSeparatedWkts)
                .orElse(new HashSet<>());
        this.taggableFilter = this.optionAndArgumentDelegate
                .getOptionArgument(TAGGABLEFILTER_OPTION_LONG, TaggableFilter::forDefinition)
                .orElse(null);
        this.taggableMatcher = this.optionAndArgumentDelegate
                .getOptionArgument(TAGGABLEMATCHER_OPTION_LONG, TaggableMatcher::from).orElse(null);
        if (this.optionAndArgumentDelegate.getParserContext() == EDGE_ONLY_CONTEXT)
        {
            this.startNodeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(STARTNODE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
            this.endNodeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(ENDNODE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
        }
        if (this.optionAndArgumentDelegate.getParserContext() == NODE_ONLY_CONTEXT)
        {
            this.inEdgeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(INEDGE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
            this.outEdgeIds = this.optionAndArgumentDelegate
                    .getOptionArgument(OUTEDGE_OPTION_LONG, this::parseCommaSeparatedLongs)
                    .orElse(new HashSet<>());
        }
        if (this.optionAndArgumentDelegate.getParserContext() == RELATION_ONLY_CONTEXT)
        {
            this.relationMemberConstraintsOR = this.optionAndArgumentDelegate
                    .getOptionArgument(RELATION_MEMBERS_OR_OPTION_LONG,
                            this::parseSemicolonSeparatedRelationMembers)
                    .orElse(new HashSet<>());
            this.relationMemberConstraintsAND = this.optionAndArgumentDelegate
                    .getOptionArgument(RELATION_MEMBERS_AND_OPTION_LONG,
                            this::parseSemicolonSeparatedRelationMembers)
                    .orElse(new HashSet<>());
        }
        this.parentRelations = this.optionAndArgumentDelegate
                .getOptionArgument(PARENT_RELATIONS_OPTION_LONG, this::parseCommaSeparatedLongs)
                .orElse(new HashSet<>());
        this.predicate = PredicateTemplate.getPredicate(AtlasEntity.class, IMPORTS_ALLOW_LIST, this)
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

        if (this.typesToCheckFromOption.isEmpty() && this.boundingWkts.isEmpty()
                && this.geometryWkts.isEmpty() && this.subGeometryWkts.isEmpty()
                && this.taggableFilter == null && this.taggableMatcher == null
                && this.startNodeIds.isEmpty() && this.endNodeIds.isEmpty()
                && this.inEdgeIds.isEmpty() && this.outEdgeIds.isEmpty()
                && this.relationMemberConstraintsOR.isEmpty()
                && this.relationMemberConstraintsAND.isEmpty() && this.parentRelations.isEmpty()
                && this.predicate == null && this.ids.isEmpty() && this.osmIds.isEmpty())
        {
            this.outputDelegate
                    .printlnErrorMessage("no filtering objects were successfully constructed");
            return 1;
        }

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName, // NOSONAR
            final File atlasResource)
    {
        List<AtlasEntity> boundedEntities = null;
        if (!this.boundingWkts.isEmpty())
        {
            boundedEntities = entitiesBoundedByWktGeometry(this.boundingWkts, atlas);
        }

        Iterable<AtlasEntity> entitiesWeAreChecking = atlas.entities();
        if (boundedEntities != null)
        {
            entitiesWeAreChecking = boundedEntities;
        }

        /*
         * This loop is O(N) (where N is the number of atlas entities), assuming the lists of
         * provided evaluation properties are much smaller than the size of the entity set. We try
         * every condition to see if we can falsify this entity as a match candidate.
         */
        for (final AtlasEntity entity : entitiesWeAreChecking) // NOSONAR
        {
            boolean entityMatchesAllCriteriaSoFar = true;
            if (!this.impliedTypesToCheck.contains(entity.getType()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }
            if (entityMatchesAllCriteriaSoFar && !this.typesToCheckFromOption.isEmpty()
                    && !this.typesToCheckFromOption.contains(entity.getType()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }
            if (entityMatchesAllCriteriaSoFar && this.taggableFilter != null
                    && !this.taggableFilter.test(entity))
            {
                entityMatchesAllCriteriaSoFar = false;
            }
            if (entityMatchesAllCriteriaSoFar && this.taggableMatcher != null
                    && !this.taggableMatcher.test(entity))
            {
                entityMatchesAllCriteriaSoFar = false;
            }

            if (entityMatchesAllCriteriaSoFar && !this.ids.isEmpty()
                    && !this.ids.contains(entity.getIdentifier()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }
            if (entityMatchesAllCriteriaSoFar && !this.osmIds.isEmpty()
                    && !this.osmIds.contains(entity.getOsmIdentifier()))
            {
                entityMatchesAllCriteriaSoFar = false;
            }

            if (entityMatchesAllCriteriaSoFar && !this.geometryWkts.isEmpty())
            {
                boolean matchedAtLeastOneWktGeometry = false;
                for (final String wkt : this.geometryWkts)
                {
                    if (entityMatchesWktGeometry(entity, wkt))
                    {
                        matchedAtLeastOneWktGeometry = true;
                    }
                }
                if (!matchedAtLeastOneWktGeometry)
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
            }
            if (entityMatchesAllCriteriaSoFar && !this.subGeometryWkts.isEmpty())
            {
                boolean containedAtLeastOneWktGeometry = false;
                for (final String wkt : this.subGeometryWkts)
                {
                    if (entityContainsWktGeometry(entity, wkt))
                    {
                        containedAtLeastOneWktGeometry = true;
                    }
                }
                if (!containedAtLeastOneWktGeometry)
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
            }
            if (entityMatchesAllCriteriaSoFar && this.predicate != null
                    && !this.predicate.test(entity))
            {
                entityMatchesAllCriteriaSoFar = false;
            }
            if (entityMatchesAllCriteriaSoFar
                    && this.optionAndArgumentDelegate.getParserContext() == NODE_ONLY_CONTEXT)
            {
                final Node node = (Node) entity;
                final Set<Long> intersectingInEdgeIdentifiers = com.google.common.collect.Sets
                        .intersection(node.inEdges().stream().map(Edge::getIdentifier)
                                .collect(Collectors.toSet()), this.inEdgeIds);
                final Set<Long> intersectingOutEdgeIdentifiers = com.google.common.collect.Sets
                        .intersection(node.outEdges().stream().map(Edge::getIdentifier)
                                .collect(Collectors.toSet()), this.outEdgeIds);
                if (!this.inEdgeIds.isEmpty() && intersectingInEdgeIdentifiers.isEmpty())
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
                if (!this.outEdgeIds.isEmpty() && intersectingOutEdgeIdentifiers.isEmpty())
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
            }
            if (entityMatchesAllCriteriaSoFar
                    && this.optionAndArgumentDelegate.getParserContext() == EDGE_ONLY_CONTEXT)
            {
                final Edge edge = (Edge) entity;
                if (!this.startNodeIds.isEmpty()
                        && !this.startNodeIds.contains(edge.start().getIdentifier()))
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
                if (!this.endNodeIds.isEmpty()
                        && !this.endNodeIds.contains(edge.end().getIdentifier()))
                {
                    entityMatchesAllCriteriaSoFar = false;
                }
            }
            if (entityMatchesAllCriteriaSoFar
                    && this.optionAndArgumentDelegate.getParserContext() == RELATION_ONLY_CONTEXT
                    && !this.relationMemberConstraintsAND.isEmpty())
            {
                final Relation relation = (Relation) entity;
                for (final RelationMemberSearchConstraint constraint : this.relationMemberConstraintsAND)
                {
                    if (!constraint.matches(relation))
                    {
                        entityMatchesAllCriteriaSoFar = false;
                        break;
                    }
                }
            }
            if (entityMatchesAllCriteriaSoFar
                    && this.optionAndArgumentDelegate.getParserContext() == RELATION_ONLY_CONTEXT
                    && !this.relationMemberConstraintsOR.isEmpty())
            {
                final Relation relation = (Relation) entity;
                boolean foundMemberMatch = false;
                for (final RelationMemberSearchConstraint constraint : this.relationMemberConstraintsOR)
                {
                    if (constraint.matches(relation))
                    {
                        foundMemberMatch = true;
                        break;
                    }
                }
                if (!foundMemberMatch)
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
                this.matchingAtlases.add(atlas);
                if (this.optionAndArgumentDelegate.hasOption(JSON_OPTION_LONG))
                {
                    printEntityWithJSONFormat(entity, atlasResource, atlas);
                }
                else
                {
                    printEntityWithHumanReadableFormat(entity, atlasResource);
                }
            }
        }
    }

    private List<AtlasEntity> entitiesBoundedByWktGeometry(final Iterable<String> wkts,
            final Atlas atlas) // NOSONAR
    {
        final List<AtlasEntity> entities = new ArrayList<>();

        for (final String wkt : wkts) // NOSONAR
        {
            final Geometry geometry = parseWkt(wkt);
            if (geometry == null)
            {
                continue;
            }

            Polygon inputPolygon = null;
            if (geometry instanceof org.locationtech.jts.geom.Polygon)
            {
                inputPolygon = new JtsPolygonConverter()
                        .backwardConvert((org.locationtech.jts.geom.Polygon) geometry);
            }
            else
            {
                this.outputDelegate.printlnErrorMessage("--" + BOUNDING_POLYGON_OPTION_LONG
                        + " only supports POLYGON, found " + geometry.getClass().getName());
                continue;
            }

            for (final AtlasEntity withinEntity : atlas.entitiesWithin(inputPolygon))
            {
                entities.add(withinEntity);
            }
        }
        return entities;
    }

    private boolean entityContainsWktGeometry(final AtlasEntity entity, final String wkt) // NOSONAR
    {
        if (entity.getType() == ItemType.RELATION)
        {
            return false;
        }

        final Geometry geometry = parseWkt(wkt);
        if (geometry == null)
        {
            return false;
        }

        Location inputLocation = null;
        PolyLine inputPolyline = null;
        if (geometry instanceof Point)
        {
            inputLocation = new JtsPointConverter().backwardConvert((Point) geometry);
        }
        else if (geometry instanceof LineString)
        {
            inputPolyline = new JtsPolyLineConverter().backwardConvert((LineString) geometry);
        }
        else
        {
            this.outputDelegate.printlnErrorMessage(
                    "--" + SUB_GEOMETRY_OPTION_LONG + " only supports POINT and LINESTRING, found "
                            + geometry.getClass().getName());
            return false;
        }

        boolean matchedSomething;
        if (entity.getType() == ItemType.POINT || entity.getType() == ItemType.NODE)
        {
            final Location location = ((LocationItem) entity).getLocation();
            if (inputLocation != null)
            {
                matchedSomething = location.equals(inputLocation);
                return matchedSomething;
            }
        }
        else if (entity.getType() == ItemType.LINE || entity.getType() == ItemType.EDGE)
        {
            final PolyLine line = ((LineItem) entity).asPolyLine();
            if (inputLocation != null)
            {
                matchedSomething = line.contains(inputLocation);
                if (matchedSomething)
                {
                    return true;
                }
            }
            if (inputPolyline != null)
            {
                matchedSomething = line.overlapsShapeOf(inputPolyline);
                return matchedSomething;
            }
        }
        else if (entity.getType() == ItemType.AREA)
        {
            final Polygon polygon = ((Area) entity).asPolygon();
            if (inputLocation != null)
            {
                matchedSomething = polygon.contains(inputLocation);
                if (matchedSomething)
                {
                    return true;
                }
            }
            if (inputPolyline != null)
            {
                matchedSomething = polygon.overlapsShapeOf(inputPolyline);
                return matchedSomething;
            }
        }
        return false;
    }

    private boolean entityMatchesWktGeometry(final AtlasEntity entity, final String wkt) // NOSONAR
    {
        if (entity.getType() == ItemType.RELATION)
        {
            return false;
        }

        final Geometry geometry = parseWkt(wkt);
        if (geometry == null)
        {
            return false;
        }

        Location inputLocation = null;
        PolyLine inputPolyLine = null;
        Polygon inputPolygon = null;
        if (geometry instanceof Point)
        {
            inputLocation = new JtsPointConverter().backwardConvert((Point) geometry);
        }
        else if (geometry instanceof LineString)
        {
            inputPolyLine = new JtsPolyLineConverter().backwardConvert((LineString) geometry);
        }
        else if (geometry instanceof org.locationtech.jts.geom.Polygon)
        {
            inputPolygon = new JtsPolygonConverter()
                    .backwardConvert((org.locationtech.jts.geom.Polygon) geometry);
        }
        else
        {
            this.outputDelegate.printlnErrorMessage("--" + GEOMETRY_OPTION_LONG
                    + " only supports POINT, LINESTRING, and POLYGON, found "
                    + geometry.getClass().getName());
            return false;
        }

        final boolean matchedSomething;
        if (entity.getType() == ItemType.POINT || entity.getType() == ItemType.NODE)
        {
            final Location location = ((LocationItem) entity).getLocation();
            if (inputLocation != null)
            {
                matchedSomething = location.equals(inputLocation);
                return matchedSomething;
            }
        }
        else if (entity.getType() == ItemType.LINE || entity.getType() == ItemType.EDGE)
        {
            final PolyLine line = ((LineItem) entity).asPolyLine();
            if (inputPolyLine != null)
            {
                matchedSomething = line.equals(inputPolyLine);
                return matchedSomething;
            }
        }
        else if (entity.getType() == ItemType.AREA)
        {
            final Polygon polygon = ((Area) entity).asPolygon();
            if (inputPolygon != null)
            {
                matchedSomething = polygon.equals(inputPolygon);
                return matchedSomething;
            }
        }
        return false;
    }

    private Optional<Tuple<String, Long>> extractIdFromMemberElement(final String element,
            final String member)
    {
        if (WILDCARD.equals(element))
        {
            return Optional.of(new Tuple<>(WILDCARD, null));
        }

        final long identifier;
        try
        {
            identifier = Long.parseLong(element);
            return Optional.of(new Tuple<>(element, identifier));
        }
        catch (final NumberFormatException exception)
        {
            this.outputDelegate.printlnErrorMessage(
                    "could not parse ID `" + element + "' from member `" + member + "'");
            return Optional.empty();
        }
    }

    private Optional<Tuple<String, ItemType>> extractItemTypeFromMemberElement(final String element,
            final String member)
    {
        if (WILDCARD.equals(element))
        {
            return Optional.of(new Tuple<>(WILDCARD, null));
        }

        final ItemType type;
        try
        {
            type = ItemType.valueOf(element.toUpperCase());
            return Optional.of(new Tuple<>(element, type));
        }
        catch (final IllegalArgumentException exception)
        {
            this.outputDelegate.printlnErrorMessage(
                    "could not parse ItemType `" + element + "' from member `" + member + "'");
            return Optional.empty();
        }
    }

    private Optional<Tuple<String, String>> extractRoleFromMemberElement(final String element)
    {
        if (WILDCARD.equals(element))
        {
            return Optional.of(new Tuple<>(WILDCARD, null));
        }

        /*
         * Allow users to specify escape sequences. This way, they may escape the '*', or escape the
         * '\' if they want to include these characters literally.
         */
        final StringBuilder builder = new StringBuilder();
        for (final int codePoint : element.codePoints().toArray())
        {
            /*
             * Only check for backslash when we see a BMP code point. This way, our role string will
             * support emojis and other non-BMP characters.
             */
            if (Character.isBmpCodePoint(codePoint) && ((char) codePoint) == '\\')
            {
                continue;
            }

            builder.appendCodePoint(codePoint);
        }

        return Optional.of(new Tuple<>(element, builder.toString()));
    }

    private Set<String> parseColonSeparatedWkts(final String wktString)
    {
        final Set<String> wktSet = new HashSet<>();

        if (wktString.isEmpty())
        {
            return wktSet;
        }

        final WKTReader reader = new WKTReader();
        final String[] wktStringSplit = wktString.split(":");
        for (final String wkt : wktStringSplit)
        {
            try
            {
                reader.read(wkt);
                wktSet.add(wkt);
            }
            catch (final ParseException exception)
            {
                this.outputDelegate.printlnErrorMessage(String.format(COULD_NOT_PARSE, "wkt", wkt));
                return new HashSet<>();
            }
        }

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
                this.outputDelegate.printlnErrorMessage(
                        String.format(COULD_NOT_PARSE, "ItemType", typeElement));
                return new HashSet<>();
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
            final long identifier;
            try
            {
                identifier = Long.parseLong(idElement);
                idSet.add(identifier);
            }
            catch (final NumberFormatException exception)
            {
                this.outputDelegate
                        .printlnErrorMessage(String.format(COULD_NOT_PARSE, "id", idElement));
                return new HashSet<>();
            }
        }
        return idSet;
    }

    private Set<RelationMemberSearchConstraint> parseSemicolonSeparatedRelationMembers(
            final String memberString)
    {
        final Set<RelationMemberSearchConstraint> constraints = new HashSet<>();

        if (memberString.isEmpty())
        {
            return constraints;
        }

        final String[] memberStringSplit = memberString.split(";");
        for (final String member : memberStringSplit)
        {
            final int expectedElementLength = 3;
            final String[] memberElements = member.split(",");
            if (memberElements.length != expectedElementLength)
            {
                this.outputDelegate.printlnErrorMessage(
                        "invalid syntax for member string `" + memberString + "'");
                return new HashSet<>();
            }

            final Optional<Tuple<String, ItemType>> itemType = extractItemTypeFromMemberElement(
                    memberElements[0], member);
            final Optional<Tuple<String, Long>> identifier = extractIdFromMemberElement(
                    memberElements[1], member);
            final Optional<Tuple<String, String>> role = extractRoleFromMemberElement(
                    memberElements[2]);

            if (itemType.isEmpty() || identifier.isEmpty() || role.isEmpty())
            {
                return new HashSet<>();
            }

            final RelationMemberSearchConstraint constraint = new RelationMemberSearchConstraint();
            constraint.withType(itemType.get().getSecond()).withId(identifier.get().getSecond())
                    .withRole(role.get().getSecond());
            constraints.add(constraint);
        }

        return constraints;
    }

    private Geometry parseWkt(final String wkt)
    {
        final WKTReader reader = new WKTReader();
        final Geometry geometry;
        try
        {
            geometry = reader.read(wkt);
            return geometry;
        }
        catch (final ParseException exception)
        {
            this.outputDelegate.printlnErrorMessage("unable to parse `" + wkt + "' as WKT");
            return null;
        }
    }

    private void printEntityWithHumanReadableFormat(final AtlasEntity entity,
            final File atlasResource)
    {
        this.outputDelegate.printlnStdout(
                "Found entity matching criteria in " + atlasResource.getPathString() + ":",
                TTYAttribute.BOLD);
        this.outputDelegate.printlnStdout(((CompleteEntity) CompleteEntity.from(entity))
                .prettify(PrettifyStringFormat.MINIMAL_MULTI_LINE, false), TTYAttribute.GREEN);
        this.outputDelegate.printlnStdout("");
    }

    private void printEntityWithJSONFormat(final AtlasEntity entity, final File atlasResource,
            final Atlas atlas)
    {
        final JsonObject outputObject = new JsonObject();
        outputObject.addProperty("shard", atlas.metaData().getCountry().orElse("XUK") + "_"
                + atlas.metaData().getShardName().orElse("0-0-0"));
        outputObject.addProperty("path", atlasResource.getPathString());
        outputObject.add("entity", ((CompleteEntity) CompleteEntity.from(entity)).toJson());
        this.outputDelegate.printlnStdout(
                new GsonBuilder().disableHtmlEscaping().create().toJson(outputObject),
                TTYAttribute.GREEN);
    }
}
