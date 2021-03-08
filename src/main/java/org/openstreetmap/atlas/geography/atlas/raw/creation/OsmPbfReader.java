package org.openstreetmap.atlas.geography.atlas.raw.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PaddingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.TagMap;
import org.openstreetmap.atlas.tags.AtlasTag;
import org.openstreetmap.atlas.tags.LastEditChangesetTag;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;
import org.openstreetmap.atlas.tags.LastEditUserNameTag;
import org.openstreetmap.atlas.tags.LastEditVersionTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OsmPbfReader} is responsible for reading OSM PBF entities and converting them to Atlas
 * Entities. It will map a PBF {@link Node} to an Atlas {@link Point}, a PBF {@link Way} to an Atlas
 * {@link Line} and a PBF {@link Relation} to an Atlas
 * {@link org.openstreetmap.atlas.geography.atlas.items.Relation}. Any exclusions based on tags are
 * allowed via the passed in {@link AtlasLoadingOption} configuration. There is no support for
 * Atlas @ {@link org.openstreetmap.atlas.geography.atlas.items.Node}s, {@link Edge}s or
 * {@link Area}s. This is also assuming that the Osmosis --completeWays flag is used during PBF
 * creation.
 *
 * @author mgostintsev
 */
public class OsmPbfReader implements Sink
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfReader.class);
    private static final String MISSING_MEMBER_MESSAGE = "Relation {} contains {} {} as a member, but it's either filtered out or this PBF shard does not contain it.";
    private static final int MINIMUM_CLOSED_WAY_LENGTH = 4;

    private final PackedAtlasBuilder builder;
    private final AtlasLoadingOption loadingOption;
    private final Set<Long> nodeIdentifiersToInclude = new HashSet<>();
    private final Set<Long> wayIdentifiersToInclude = new HashSet<>();
    private final Set<Long> pointIdentifiersFromFilteredLines = new HashSet<>();
    private final List<Relation> stagedRelations = new ArrayList<>();
    private final RawAtlasStatistic statistics = new RawAtlasStatistic(logger);

    /**
     * Determines if the given {@link Entity} should be brought into the {@link Atlas}. Ideally, all
     * features will be brought in. However, to make {@link Atlas} generation flexible and fit all
     * use cases, this is configurable.
     *
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use for configuration lookup.
     * @param entity
     *            The candidate {@link Entity}
     * @return {@code true} if this {@link Entity} should be brought into the {@link Atlas}.
     */
    public static boolean shouldProcessEntity(final AtlasLoadingOption loadingOption,
            final Entity entity)
    {
        // The keepAll option is primarily used for QA purposes. Everything must stay.
        if (loadingOption.isKeepAll())
        {
            return true;
        }
        else if (entity instanceof Node)
        {
            return loadingOption.getOsmPbfNodeFilter().test(Taggable.with(entity.getTags()));
        }
        else if (entity instanceof Way)
        {
            return loadingOption.getOsmPbfWayFilter().test(Taggable.with(entity.getTags()));
        }
        else if (entity instanceof Relation)
        {
            return loadingOption.getOsmPbfRelationFilter().test(Taggable.with(entity.getTags()));
        }
        else
        {
            // No Bound filtering
            return true;
        }
    }

    /**
     * Default constructor
     *
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     * @param builder
     *            The {@link PackedAtlasBuilder} to construct the raw Atlas
     */
    public OsmPbfReader(final AtlasLoadingOption loadingOption, final PackedAtlasBuilder builder)
    {
        this.builder = builder;
        this.loadingOption = loadingOption;
    }

    @Override
    public void complete()
    {
        // No-op
    }

    @Override
    public void initialize(final Map<String, Object> metaData)
    {
        logger.info("Initialized OSM PBF Reader successfully");
    }

    @Override
    public void process(final EntityContainer entityContainer)
    {
        final Entity rawEntity = entityContainer.getEntity();

        if (shouldProcessEntity(this.loadingOption, rawEntity))
        {
            if (rawEntity instanceof Node
                    && this.nodeIdentifiersToInclude.contains(rawEntity.getId()))
            {
                processNode(rawEntity);
            }
            else if (rawEntity instanceof Way
                    && this.wayIdentifiersToInclude.contains(rawEntity.getId()))
            {
                processWay(rawEntity);
            }
            else if (this.loadingOption.isLoadOsmRelation() && rawEntity instanceof Relation)
            {
                processRelation(rawEntity);
            }
            else if (rawEntity instanceof Bound)
            {
                logger.trace("Encountered PBF Bound {}, skipping over it.", rawEntity.getId());
            }
        }
        else
        {
            recordNodeIdentifiersFromFilteredEntity(rawEntity);
            logFilteredStatistics(rawEntity);
            logger.trace("Filtering out OSM {} {} from Raw Atlas", rawEntity.getType(),
                    rawEntity.getId());
        }
    }

    @Override
    public void release()
    {
        // We've processed all Nodes, Ways and shallow Relations to this point. Now, we need to
        // handle Relations that contain Relation members properly.
        processStagedRelations();
        this.statistics.summary();
        logger.info("Released OSM PBF Reader");
    }

    /**
     * Sets all the Node identifiers marked for inclusion.
     *
     * @param nodesToInclude
     *            The set of Node identifiers to include in the atlas
     */
    public void setIncludedNodes(final Set<Long> nodesToInclude)
    {
        this.nodeIdentifiersToInclude.addAll(nodesToInclude);
    }

    /**
     * Sets all the Way identifiers marked for inclusion.
     *
     * @param waysToInclude
     *            The set of Way identifiers to include in the atlas
     */
    public void setIncludedWays(final Set<Long> waysToInclude)
    {
        this.wayIdentifiersToInclude.addAll(waysToInclude);
    }

    /**
     * @return all the {@link Point} identifiers that make up {@link Line}s that were filtered. We
     *         process all PBF {@link Node}s first and add them as Atlas {@link Point}s. After this,
     *         we may filter out some PBF {@link Way}s. As a result, our Atlas file may contain
     *         points which aren't used by any lines. We want to record these and see if we can
     *         filter them out downstream.
     */
    protected Set<Long> getPointIdentifiersFromFilteredLines()
    {
        return this.pointIdentifiersFromFilteredLines;
    }

    /**
     * Constructs an Atlas {@link org.openstreetmap.atlas.geography.atlas.items.Relation}. In the
     * process, a relation can be dropped if it doesn't contain any members. The members could be
     * empty, if they were filtered out by the PBF ingest criteria or if this PBF file doesn't
     * contain them.
     *
     * @param relation
     *            The {@link Relation} to add
     */
    private void addRelation(final Relation relation)
    {
        final RelationBean bean = constructRelationBean(relation);
        if (!bean.isEmpty())
        {
            this.builder.addRelation(padIdentifier(relation.getId()), relation.getId(), bean,
                    populateEntityTags(relation).getTags());
            this.statistics.recordCreatedRelation();
        }
        else
        {
            this.statistics.recordDroppedRelation();
            logger.debug("Cannot add empty Relation {} to the Atlas. We're either filtering"
                    + " out the members that make up the Relation or none of the "
                    + "members are present in this PBF shard.", relation.getId());
        }
    }

    /**
     * Creates a {@link RelationBean} for the given {@link Relation}. Note: The returned bean can be
     * empty.
     *
     * @param relation
     *            The {@link Relation} for which to create the {@link RelationBean}
     * @return the created {@link RelationBean}
     */
    private RelationBean constructRelationBean(final Relation relation)
    {
        final RelationBean bean = new RelationBean();
        for (final RelationMember member : relation.getMembers())
        {
            final long memberIdentifier = padIdentifier(member.getMemberId());
            final EntityType memberType = member.getMemberType();
            final String role = member.getMemberRole();

            if (memberType == EntityType.Node)
            {
                if (this.builder.peek().point(memberIdentifier) != null)
                {
                    bean.addItem(memberIdentifier, role, ItemType.POINT);
                }
                else
                {
                    logger.trace(MISSING_MEMBER_MESSAGE, relation.getId(), EntityType.Node,
                            memberIdentifier);
                }
            }
            else if (memberType == EntityType.Way)
            {
                if (this.builder.peek().line(memberIdentifier) != null)
                {
                    bean.addItem(memberIdentifier, role, ItemType.LINE);
                }
                else if (this.builder.peek().area(memberIdentifier) != null)
                {
                    bean.addItem(memberIdentifier, role, ItemType.AREA);
                }
                else
                {
                    logger.trace(MISSING_MEMBER_MESSAGE, relation.getId(), EntityType.Way,
                            memberIdentifier);
                }
            }
            else if (memberType == EntityType.Relation)
            {
                if (this.builder.peek().relation(memberIdentifier) != null)
                {
                    bean.addItem(memberIdentifier, role, ItemType.RELATION);
                }
                else
                {
                    logger.trace(MISSING_MEMBER_MESSAGE, relation.getId(), EntityType.Relation,
                            memberIdentifier);
                }
            }
            else
            {
                // A Bound should never be a Relation member; if this is the case, log and continue
                logger.error("Invalid Bound member {} found for Relation {}", member.getMemberId(),
                        relation.getId());
            }
        }

        return bean;
    }

    private Polygon constructWayPolygon(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();
        final List<Location> wayLocations = new ArrayList<>();
        wayNodes.forEach(
                node -> wayLocations.add(getNodeLocation(padIdentifier(node.getNodeId()))));
        wayLocations.remove(wayLocations.size() - 1);
        return new Polygon(wayLocations);
    }

    /**
     * Constructs a {@link PolyLine} given an OSM PBF {@link Way}. The {@link Way} object doesn't
     * contain the coordinates of its geometry, only the references to the {@link Node}s
     * identifiers. We need to look up the {@link Location} of each {@link Node} and re-construct
     * the {@link PolyLine} manually.
     *
     * @param way
     *            The {@link Way} for which to construct the {@link PolyLine}
     * @return the constructed {@link PolyLine}
     */
    private PolyLine constructWayPolyline(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();
        final List<Location> wayLocations = new ArrayList<>();
        wayNodes.forEach(
                node -> wayLocations.add(getNodeLocation(padIdentifier(node.getNodeId()))));
        return new PolyLine(wayLocations);
    }

    /**
     * Checks if the given {@link Relation} contains an un-indexed member {@link Relation}.
     *
     * @param relation
     *            The {@link Relation} to check
     * @return {@code true} if the given {@link Relation} contains a member {@link Relation} that
     *         hasn't yet been indexed.
     */
    private boolean containsUnindexedSubRelation(final Relation relation)
    {
        return relation.getMembers().stream()
                .anyMatch(member -> member.getMemberType() == EntityType.Relation && this.builder
                        .peek().relation(padIdentifier(member.getMemberId())) == null);
    }

    /**
     * Looks up the {@link Location} of a {@link Node}, given its identifier. This identifier is
     * used to look up the corresponding Atlas {@link Point}.
     *
     * @param identifier
     *            The {@link Node} identifier, in whose {@link Location} we're interested in.
     * @return the extracted {@link Location}
     */
    private Location getNodeLocation(final long identifier)
    {
        final Point point = this.builder.peek().point(identifier);
        if (point != null)
        {
            return point.getLocation();
        }
        else
        {
            throw new CoreException(
                    "Unable to find Point {} in Atlas. "
                            + "It was either filtered out or the PBF file is malformed.",
                    identifier);
        }
    }

    /**
     * A {@link Way} is invalid if it's of size 0 or 1; or if it's of size 2 and has the same start
     * and end node.
     *
     * @param way
     *            The {@link Way} to validate
     * @return {@code true} if the given {@link Way} is invalid
     */
    private boolean isInvalidWay(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();
        return wayNodes.size() < 2
                || wayNodes.size() == 2
                        && wayNodes.get(0).getNodeId() == wayNodes.get(1).getNodeId()
                || wayNodes.size() < MINIMUM_CLOSED_WAY_LENGTH && way.isClosed();
    }

    /**
     * Log any {@link Entity}s that got filtered by ingest configuration.
     *
     * @param entity
     *            The filtered {@link Entity}
     */
    private void logFilteredStatistics(final Entity entity)
    {
        if (entity instanceof Node)
        {
            this.statistics.recordFilteredNode();
        }
        else if (entity instanceof Way)
        {
            this.statistics.recordFilteredWay();
        }
        else if (entity instanceof Relation)
        {
            this.statistics.recordFilteredRelation();
        }
        else
        {
            // No-Op. We don't log bounds.
        }
    }

    /**
     * @return {@code true} if we need to pad identifiers when creating atlas entities.
     */
    private boolean needsPadding()
    {
        return this.loadingOption.isCountrySlicing() || this.loadingOption.isWaySectioning();
    }

    /**
     * Pads the given OSM identifier, by appending 6 digits to it. The first 3 appended digits are
     * the country code identifier and the last 3 digits are the way-section identifier.
     *
     * @param identifier
     *            The original OSM identifier
     * @return a padded identifier
     */
    private long padIdentifier(final long identifier)
    {
        if (needsPadding())
        {
            return PaddingIdentifierFactory.pad(identifier);
        }
        else
        {
            return identifier;
        }
    }

    /**
     * First, creates an {@link Entity} {@link Tag} for specific OSM attributes we're interested in
     * propagating to the {@link AtlasEntity}. Secondly, converts the given {@link Entity}'s
     * collection of {@link Tag}s to a {@link Map} of key/value pairs used to build an
     * {@link AtlasEntity}'s tag set.
     *
     * @param entity
     *            The {@link Entity} being processed
     * @return a {@link Map} of key/value tags
     */
    private TagMap populateEntityTags(final Entity entity)
    {
        // Update the entity's tags to contain specific OSM attributes we care about, so that these
        // get translated to Atlas Entity tags.
        storeOsmEntityAttributesAsTags(entity);
        return new TagMap(entity.getTags());
    }

    /**
     * Uses the {@link Node} OSM identifier, geometry and tags to create an Atlas {@link Point}.
     *
     * @param entity
     *            The {@link Entity} that will become an Atlas {@link Point}
     */
    private void processNode(final Entity entity)
    {
        final Node node = (Node) entity;
        this.builder.addPoint(padIdentifier(node.getId()),
                new Location(Latitude.degrees(node.getLatitude()),
                        Longitude.degrees(node.getLongitude())),
                populateEntityTags(node).getTags());
        this.statistics.recordCreatedPoint();
    }

    /**
     * Tries to create an Atlas {@link org.openstreetmap.atlas.geography.atlas.items.Relation}. If
     * the {@link Entity} contains a member that's also a relation and that member hasn't been
     * processed yet, then we add the given {@link Relation} to a Collection of staged relations to
     * process later (see {@link #release()} method). Otherwise, we add it.
     *
     * @param entity
     *            The {@link Entity} that will become an Atlas
     *            {@link org.openstreetmap.atlas.geography.atlas.items.Relation}
     */
    private void processRelation(final Entity entity)
    {
        final Relation relation = (Relation) entity;
        if (containsUnindexedSubRelation(relation))
        {
            // Stage this Relation, it has a member relation that we haven't processed yet
            this.stagedRelations.add(relation);
        }
        else
        {
            addRelation(relation);
        }
    }

    /**
     * Processes all non-shallow {@link Relation}s - any {@link Relation} that has another
     * {@link Relation} as a member. This is called near the end of the processing, once we've
     * successfully added all {@link Node}s, {@link Way}s and shallow {@link Relation}s. If the
     * number of staged relations doesn't change from one iteration to the next, then we know that
     * any future iteration will not un-stage any of the relations. Therefore, we can try to add the
     * relations in the current state.
     */
    private void processStagedRelations()
    {
        int previousStagedRelationSize = Integer.MAX_VALUE;
        List<Relation> stagedRelations = this.stagedRelations;
        int currentStagedRelationSize = stagedRelations.size();

        while (!stagedRelations.isEmpty() && currentStagedRelationSize < previousStagedRelationSize)
        {
            final List<Relation> updatedStagedRelations = new ArrayList<>();
            for (final Relation relation : stagedRelations)
            {
                if (containsUnindexedSubRelation(relation))
                {
                    updatedStagedRelations.add(relation);
                }
                else
                {
                    addRelation(relation);
                }
            }
            stagedRelations = updatedStagedRelations;
            previousStagedRelationSize = currentStagedRelationSize;
            currentStagedRelationSize = stagedRelations.size();
        }

        // If we weren't able to process any of the staged relations and there are still some left,
        // go ahead and try to add these in their current state. We can potentially add a relation
        // that has an incomplete member list, however we are relying on the fact that a neighboring
        // shard will contain this relation and the missing members. When the two are combined, the
        // relation will become whole again.
        if (currentStagedRelationSize == previousStagedRelationSize && !stagedRelations.isEmpty())
        {
            stagedRelations.forEach(this::addRelation);
        }
    }

    /**
     * Uses the {@link Way} identifier, re-constructed geometry and tags to create an Atlas
     * {@link Line}.
     *
     * @param entity
     *            The {@link Entity} that will become an Atlas {@link Line}
     */
    private void processWay(final Entity entity)
    {
        final Way way = (Way) entity;
        if (isInvalidWay(way))
        {
            this.statistics.recordDroppedWay();
        }
        else
        {
            final PolyLine wayPolyLine = constructWayPolyline(way);
            final TagMap wayTags = populateEntityTags(way);
            if (wayPolyLine.first().equals(wayPolyLine.last()))
            {
                boolean kept = false;
                if (this.loadingOption.getAreaFilter().test(wayTags))
                {
                    this.builder.addArea(padIdentifier(way.getId()), constructWayPolygon(way),
                            wayTags.getTags());
                    this.statistics.recordCreatedLine();
                    kept = true;
                }
                if (this.loadingOption.getEdgeFilter().test(wayTags))
                {
                    this.builder.addLine(padIdentifier(way.getId()), wayPolyLine,
                            wayTags.getTags());
                    this.statistics.recordCreatedLine();
                    kept = true;
                }
                if (!kept)
                {
                    this.statistics.recordDroppedWay();
                }
            }
            else
            {
                this.builder.addLine(padIdentifier(way.getId()), wayPolyLine, wayTags.getTags());
                this.statistics.recordCreatedLine();
            }
        }
    }

    /**
     * Because of how an Atlas is constructed, we add all PBF {@link Node}s as {@link Point}s.
     * However, if we filter out a PBF {@link Way}, we want to keep track of all {@link Node}s that
     * make up that {@link Way}, in case we need to remove them as well. We are going to keep track
     * of all filtered PBF {@link Node}/Atlas {@link Point} identifiers, and use subAtlas
     * functionality to filter them out after the Atlas is built. For now, ignore filtering any
     * Nodes that come from filtered Relations. This will be handled in the way-sectioning code.
     *
     * @param entity
     *            The {@link Entity} whose Node (Atlas Point) identifiers we want to filter out
     */
    private void recordNodeIdentifiersFromFilteredEntity(final Entity entity)
    {
        if (entity instanceof Way)
        {
            final List<WayNode> wayNodes = ((Way) entity).getWayNodes();
            wayNodes.forEach(node ->
            {
                this.pointIdentifiersFromFilteredLines.add(padIdentifier(node.getNodeId()));
                this.statistics.recordFilteredNode();
            });
        }
    }

    /**
     * Grabs desired OSM attributes (such as last edited time) from given {@link Entity} and creates
     * a corresponding {@link Tag} value for each.
     *
     * @param entity
     *            The {@link Entity}, whose attributes we want to save.
     */
    private void storeOsmEntityAttributesAsTags(final Entity entity)
    {
        final Collection<Tag> tags = entity.getTags();
        for (final String tag : AtlasTag.TAGS_FROM_OSM)
        {
            if (tag.equals(LastEditTimeTag.KEY))
            {
                tags.add(new Tag(tag, String.valueOf(entity.getTimestamp().getTime())));
            }
            else if (tag.equals(LastEditUserIdentifierTag.KEY))
            {
                tags.add(new Tag(tag, String.valueOf(entity.getUser().getId())));
            }
            else if (tag.equals(LastEditUserNameTag.KEY))
            {
                tags.add(new Tag(tag, entity.getUser().getName()));
            }
            else if (tag.equals(LastEditVersionTag.KEY))
            {
                tags.add(new Tag(tag, String.valueOf(entity.getVersion())));
            }
            else if (tag.equals(LastEditChangesetTag.KEY))
            {
                tags.add(new Tag(tag, String.valueOf(entity.getChangesetId())));
            }
            else
            {
                logger.error(
                        "Trying to add mandatory tag {}, but no behavior defined for getting the value.",
                        tag);
            }
        }
    }
}
