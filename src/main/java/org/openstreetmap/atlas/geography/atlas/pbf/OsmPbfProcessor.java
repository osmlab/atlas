package org.openstreetmap.atlas.geography.atlas.pbf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.converters.TagMapToTagCollectionConverter;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.CountrySlicingProcessor;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.WaySectionProcessor;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PaddingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfMemoryStore;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfOneWay;
import org.openstreetmap.atlas.geography.atlas.pbf.store.TagMap;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.LastEditChangesetTag;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;
import org.openstreetmap.atlas.tags.LastEditUserNameTag;
import org.openstreetmap.atlas.tags.LastEditVersionTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;
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

import com.vividsolutions.jts.geom.Geometry;

/**
 * This {@link OsmPbfProcessor} loads four different OSM entities (Node, Way, Relation and Bound),
 * and build a complete atlas with a multipolygon or bounding box. 'Complete' means if part of an
 * entity is inside of polygon, the whole entity will be loaded.
 * <p>
 * This is a two passes implementation for an OSM PBF file. In first pass it records all the
 * locations needed by {@link Edge}, {@link Line}, {@link Area}, {@link Node}, {@link Point}
 * {@link Relation} and the number of them, also identifies all the items partial outside. In second
 * pass it builds the {@link PackedAtlas}. This will load partial nodes into memory as well as all
 * ways and relations in first pass.
 *
 * @author tony
 */
public class OsmPbfProcessor implements Sink
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfProcessor.class);

    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();
    private static final int MAXIMUM_NETWORK_EXTENSION = 100;
    private static final TagMapToTagCollectionConverter TAG_MAP_TO_TAG_COLLECTION_CONVERTER = new TagMapToTagCollectionConverter();

    private final PackedAtlasBuilder builder;
    private final AtlasLoadingOption loadingOption;
    private final MultiPolygon multiPolygon;
    private final PbfMemoryStore store;
    private final OsmPbfStatistic statistic = new OsmPbfStatistic(logger);
    private Set<Long> nodesOutsideOfPolygon = new HashSet<>();
    private Map<Long, Way> allWays = new HashMap<>();
    private Map<Long, Relation> allRelations = new HashMap<>();
    private boolean firstPass;
    private boolean secondPass;
    private final Set<Long> nodesOutsideOfCountry = new HashSet<>();
    private final Set<Long> nodeIdentifiersAtNetworkBoundary = new HashSet<>();

    public OsmPbfProcessor(final PackedAtlasBuilder builder, final AtlasLoadingOption loadingOption,
            final MultiPolygon multiPolygon)
    {
        this.builder = builder;
        this.loadingOption = loadingOption;
        this.multiPolygon = multiPolygon;
        this.firstPass = true;
        this.secondPass = false;
        this.store = new PbfMemoryStore(this.loadingOption);
    }

    @Override
    public void complete()
    {
    }

    @Override
    public void initialize(final Map<String, Object> metaData)
    {
        if (this.firstPass)
        {
            logger.trace("Start loading atlas with polygon or bound {}", this.multiPolygon);
            logger.trace("With country code {}",
                    String.join(",", this.loadingOption.getCountryCodes()));
        }
    }

    @Override
    public void process(final EntityContainer entityContainer)
    {
        final Entity rawEntity = entityContainer.getEntity();

        if (shouldProcessEntity(rawEntity))
        {
            this.statistic.incrementOsmEntity();
            final Entity entity = preProcess(rawEntity);

            // Process OSM node
            if (this.loadingOption.isLoadOsmNode() && entity instanceof Node)
            {
                processNode(entity);
            }

            // Process OSM way
            else if (this.loadingOption.isLoadOsmWay() && entity instanceof Way)
            {
                processWay(entity);
            }

            // Process OSM Relation
            else if (this.loadingOption.isLoadOsmRelation() && entity instanceof Relation)
            {
                processRelation(entity);
            }
            // Process OSM Bound
            else if (this.loadingOption.isLoadOsmBound() && entity instanceof Bound)
            {
                this.statistic.pauseOsmRelationCounter();
            }
        }
    }

    @Override
    public void release()
    {
        if (this.firstPass)
        {
            calculateRelationsInsideTargetPolygon();
            calculateWaysAndNodes();
            keepOutsideWaysThatAreConnected();

            this.allWays = null;
            this.allRelations = null;
        }

        else if (this.secondPass)
        {
            if (this.loadingOption.isCountrySlicing())
            {
                logger.info("Running country slicing");
                new CountrySlicingProcessor(this.store, this.loadingOption.getCountryBoundaryMap(),
                        this.loadingOption.getCountryCodes()).run();
                this.store.removeOutsideWays();
                this.store.rebuildNodesAtEndOfEdges();
                this.store.rebuildNodesInRelations();
            }

            if (this.loadingOption.isWaySectioning())
            {
                logger.info("Running way sectioning");
                new WaySectionProcessor(this.store).run();
            }

            final long atlasNodeNumber = this.store.atlasNodeCount();
            final long atlasPointNumber = this.store.atlasPointCount();
            final long atlasEdgeNumber = this.store.atlasEdgeCount();
            final long atlasLineNumber = this.store.atlasLineCount();
            final long atlasAreaNumber = this.store.atlasAreaCount();
            final long atlasRelationNumber = this.store.relationCount();

            this.builder.setSizeEstimates(new AtlasSize(atlasEdgeNumber, atlasNodeNumber,
                    atlasAreaNumber, atlasLineNumber, atlasPointNumber, atlasRelationNumber));

            logger.info(
                    "Initialize Atlas builder with {} nodes, {} points, {} edges, {} lines, {} areas and {} relations",
                    atlasNodeNumber, atlasPointNumber, atlasEdgeNumber, atlasLineNumber,
                    atlasAreaNumber, atlasRelationNumber);

            // Build Atlas
            buildNodes();
            buildWays();
            buildRelations();

            this.nodesOutsideOfPolygon = null;
            this.store.clear();
        }
        this.statistic.summary();
        this.statistic.clear();

        logger.info("{} pass loading done", this.firstPass ? "First" : "Second");
        if (this.firstPass)
        {
            this.firstPass = false;
            this.secondPass = true;
        }
    }

    private void addWayNodes(final Set<Long> set, final Way way)
    {
        way.getWayNodes().forEach(wayNode -> set.add(wayNode.getNodeId()));
    }

    private void buildNodes()
    {
        logger.trace("Precheck all OSM ways and relations");
        final Set<Long> nodesNeedBuild = new HashSet<>();
        this.store.atlasEdges().forEach(edge -> addWayNodes(nodesNeedBuild, edge));
        this.store.atlasLines().forEach(line -> addWayNodes(nodesNeedBuild, line));
        this.store.atlasAreas().forEach(area -> addWayNodes(nodesNeedBuild, area));
        nodesNeedBuild.addAll(this.store.getNodesInRelations());

        logger.trace("Building Atlas for nodes");
        this.store.atlasNodes().forEach(node ->
        {
            final long identifier = node.getId();
            if (nodesNeedBuild.contains(identifier) || this.store.hasSameCountryCode(node))
            {
                this.builder.addNode(identifier, this.store.getNodeLocation(node),
                        this.store.tagsWithCountryCode(node));
                this.statistic.incrementAtlasNode();
            }
            else
            {
                this.nodesOutsideOfCountry.add(node.getId());
            }
        });

        this.store.atlasPoints().forEach(point ->
        {
            final long identifier = point.getId();
            if (nodesNeedBuild.contains(identifier) || this.store.hasSameCountryCode(point))
            {
                this.builder.addPoint(identifier, this.store.getNodeLocation(point),
                        this.store.tagsWithCountryCode(point));
                this.statistic.incrementAtlasPoint();
            }
            else
            {
                this.nodesOutsideOfCountry.add(point.getId());
            }
        });
    }

    /**
     * @return true if successfully built sub relation
     */
    private boolean buildRelation(final long identifier, final Set<Long> visited,
            final List<Long> parentTree)
    {
        if (!visited.contains(identifier))
        {
            visited.add(identifier);
            parentTree.add(identifier);
            final Relation relation = this.store.getRelation(identifier);

            // corrupted PBF, relation is not complete
            if (relation == null)
            {
                return false;
            }

            final TagMap tags = new TagMap(relation.getTags());
            final RelationBean bean = new RelationBean();
            for (final RelationMember member : relation.getMembers())
            {
                final long memberIdentifier = member.getMemberId();
                final EntityType memberType = member.getMemberType();
                final String role = member.getMemberRole();

                if (memberType == EntityType.Node && this.store.containsNode(memberIdentifier)
                        && !this.nodesOutsideOfCountry.contains(memberIdentifier))
                {
                    if (this.store.isAtlasNode(memberIdentifier))
                    {
                        bean.addItem(memberIdentifier, role, ItemType.NODE);
                    }
                    else if (this.store.isAtlasPoint(memberIdentifier))
                    {
                        bean.addItem(memberIdentifier, role, ItemType.POINT);
                    }
                    else
                    {
                        logger.warn(
                                "Ignoring node {} for relation {} since it was not defined by Atlas node/point",
                                memberIdentifier, identifier);
                    }
                }

                else if (memberType == EntityType.Way && this.store.containsWay(memberIdentifier))
                {
                    if (this.store.isAtlasEdge(memberIdentifier))
                    {
                        bean.addItem(memberIdentifier, role, ItemType.EDGE);

                        // Any bidirectional edge in a relation will be stored twice
                        if (this.builder.peek().edge(-memberIdentifier) != null)
                        {
                            bean.addItem(-memberIdentifier, role, ItemType.EDGE);
                        }
                    }
                    else if (this.store.isAtlasLine(memberIdentifier))
                    {
                        bean.addItem(memberIdentifier, role, ItemType.LINE);
                    }
                    else if (this.store.isAtlasArea(memberIdentifier))
                    {
                        bean.addItem(memberIdentifier, role, ItemType.AREA);
                    }
                    else
                    {
                        logger.warn(
                                "Ignoring way {} for relation {} since it was not defined by Atlas edge/line/area",
                                memberIdentifier, identifier);
                    }
                }

                else if (memberType == EntityType.Relation)
                {
                    if (!parentTree.contains(memberIdentifier))
                    {
                        // Build nested relation first
                        if (!buildRelation(memberIdentifier, visited, parentTree))
                        {
                            logger.warn(
                                    "Ignoring member relation {} with role {} within relation {} since it was not defined by Atlas relation",
                                    memberIdentifier, role, identifier);
                        }
                        if (this.builder.peek().relation(memberIdentifier) != null)
                        {
                            // Make sure that the relations that are members of this relation that
                            // have been omitted because they themselves had no members are removed
                            // from the current relation's members.
                            bean.addItem(memberIdentifier, role, ItemType.RELATION);
                        }
                    }
                    else
                    {
                        logger.error(
                                "Ignoring member relation {} within parent relation {} since there is a loop! Parent Tree: {}",
                                memberIdentifier, identifier, parentTree);
                    }
                }
            }

            if (!bean.isEmpty())
            {
                this.builder.addRelation(identifier, identifier, bean, tags.getTags());
                this.statistic.incrementAtlasRelation();
            }
            else
            {
                logger.warn("Dropping relation {} because no members were found.", identifier);
            }
        }
        return true;
    }

    private void buildRelations()
    {
        logger.trace("Building Atlas for relations");
        // Visited set: global, all relations
        final Set<Long> visited = new HashSet<>();
        // Parent list: local to each initial relation call, create it in the lambda.
        this.store.forEachRelation(
                (identifier, relation) -> buildRelation(identifier, visited, new ArrayList<>()));
    }

    private void buildWays()
    {
        logger.trace("Building Atlas for ways");

        this.store.atlasEdges().forEach(edge ->
        {
            final long identifier = edge.getId();
            final PolyLine polyline = this.store.createPolyline(edge);
            final TagMap tags = new TagMap(edge.getTags());
            final PbfOneWay oneWay = tags.getOneWay();
            switch (oneWay)
            {
                case NO:
                    this.builder.addEdge(identifier, polyline, tags.getTags());
                    this.builder.addEdge(-identifier, polyline.reversed(), tags.getTags());
                    this.statistic.incrementAtlasEdge(2);
                    break;
                case YES:
                    this.builder.addEdge(identifier, polyline, tags.getTags());
                    this.statistic.incrementAtlasEdge();
                    break;
                case REVERSED:
                    this.builder.addEdge(identifier, polyline.reversed(), tags.getTags());
                    this.statistic.incrementAtlasEdge();
                    break;
                default:
                    break;
            }
        });

        this.store.atlasLines().forEach(line ->
        {
            final long identifier = line.getId();
            this.builder.addLine(identifier, this.store.createPolyline(line),
                    this.store.getTagMap(line));
            this.statistic.incrementAtlasLine();
        });

        this.store.atlasAreas().forEach(area ->
        {
            final long identifier = area.getId();
            this.builder.addArea(identifier, this.store.createPolygon(area),
                    this.store.getTagMap(area));
            this.statistic.incrementAtlasArea();
        });
    }

    private void calculateRelationsInsideTargetPolygon()
    {
        final Set<Way> waysToBeAdded = new HashSet<>();
        final Set<Relation> relationsToBeAdded = new HashSet<>();

        for (final Relation parent : this.allRelations.values())
        {
            final long parentIdentifier = parent.getId();
            final boolean isShallow = isShallow(parent);

            // This relation and all its children have been marked as inside before
            if (this.store.containsRelation(parentIdentifier))
            {
                continue;
            }

            // Check if there is any portion of the relation is inside
            final Queue<Long> nested = new LinkedList<>();
            final Set<Long> visited = new HashSet<>();
            boolean inside = false;
            nested.offer(parentIdentifier);

            while (!nested.isEmpty())
            {
                final long childIdentifier = nested.poll();
                final Relation child = this.allRelations.get(childIdentifier);

                // Corrupted data
                if (child == null)
                {
                    logger.warn(
                            "Missing Data in PBF: Parent relation {} references child relation {} which is not present.",
                            parentIdentifier, childIdentifier);
                    if (isShallow)
                    {
                        inside = false;
                    }
                    break;
                }

                // This child relation and all its children have been marked as inside before
                if (this.store.containsRelation(childIdentifier))
                {
                    inside = true;
                    continue;
                }

                if (visited.contains(child.getId()))
                {
                    logger.warn("Corrupted data, found loop relations: parent {}, child {}", parent,
                            child);
                    inside = false;
                    break;
                }

                // Check if any member is inside of polygon
                for (final RelationMember member : child.getMembers())
                {
                    final long memberIdentifier = member.getMemberId();
                    final EntityType memberType = member.getMemberType();
                    if (memberType == EntityType.Node && this.store.containsNode(memberIdentifier)
                            || memberType == EntityType.Way
                                    && this.store.containsWay(memberIdentifier))
                    {
                        inside = true;
                    }
                    else if (memberType == EntityType.Relation)
                    {
                        nested.offer(memberIdentifier);
                    }
                }
                visited.add(child.getId());
            }

            // If any portion of relation is inside, mark all the outside portion as well as the
            // relation
            if (inside)
            {
                // 'visited' contains all non-marked nested relations including the parent
                visited.forEach(relationIdentifier ->
                {
                    // Mark all outside nodes and edges
                    final Relation relation = this.allRelations.get(relationIdentifier);
                    for (final RelationMember member : relation.getMembers())
                    {
                        final long memberIdentifier = member.getMemberId();
                        final EntityType memberType = member.getMemberType();

                        if (memberType == EntityType.Node)
                        {
                            if (!this.store.containsNode(memberIdentifier))
                            {
                                this.nodesOutsideOfPolygon.add(memberIdentifier);
                            }
                            this.store.addNodeInRelations(memberIdentifier);
                        }

                        if (memberType == EntityType.Way
                                && !this.store.containsWay(memberIdentifier))
                        {
                            if (this.allWays.containsKey(memberIdentifier))
                            {
                                waysToBeAdded.add(this.allWays.get(memberIdentifier));
                            }
                        }
                    }
                    // Mark this relation as inside
                    relationsToBeAdded.add(relation);
                    this.statistic.incrementAtlasRelation();
                });
            }
        }
        waysToBeAdded.forEach(this.store::addWay);
        relationsToBeAdded.forEach(this.store::addRelation);
    }

    private void calculateWaysAndNodes()
    {
        this.store.forEachWay((wayIdentifier, way) ->
        {
            final List<WayNode> wayNodes = way.getWayNodes();
            final TagMap tags = new TagMap(way.getTags());

            /* Mark the node outside */
            for (final WayNode node : wayNodes)
            {
                final long nodeIdentifier = node.getNodeId();
                if (!this.store.containsNode(nodeIdentifier))
                {
                    this.nodesOutsideOfPolygon.add(nodeIdentifier);
                }
            }

            /* Store end nodes of edge to identify atlas node in second pass */
            if (this.store.isAtlasEdge(wayIdentifier))
            {
                wayNodes.stream().map(WayNode::getNodeId).forEach(this.store::addNodeInEdge);
                this.store.addNodeAtEndOfEdge(wayNodes.get(0).getNodeId());
                this.store.addNodeAtEndOfEdge(wayNodes.get(wayNodes.size() - 1).getNodeId());
                final PbfOneWay oneWay = tags.getOneWay();
                if (oneWay == PbfOneWay.NO)
                {
                    this.statistic.incrementAtlasEdge(2);
                }
                else
                {
                    this.statistic.incrementAtlasEdge();
                }
            }
            else if (this.store.isAtlasLine(wayIdentifier))
            {
                this.statistic.incrementAtlasLine();
            }
            else if (this.store.isAtlasArea(wayIdentifier))
            {
                this.statistic.incrementAtlasArea();
            }
        });
    }

    private boolean isHighwayOrFerry(final Way way)
    {
        final Taggable taggableWay = new Taggable()
        {
            private Map<String, String> tags = null;

            @Override
            public Optional<String> getTag(final String key)
            {
                if (this.tags == null)
                {
                    this.tags = new HashMap<>();
                    for (final Tag tag : way.getTags())
                    {
                        this.tags.put(tag.getKey(), tag.getValue());
                    }
                }
                return Optional.ofNullable(this.tags.get(key));
            }
        };
        return HighwayTag.isCoreWay(taggableWay) || RouteTag.isFerry(taggableWay);
    }

    /**
     * @param node
     *            The node to test for
     * @return True only if the node is completely outside of any boundary. In that case, it has
     *         either no country (COUNTRY_MISSING) or its country is assigned using the nearest
     *         neighbor logic.
     */
    private boolean isOutsideBoundary(final Node node)
    {
        final Geometry nodeLocation = JTS_POINT_CONVERTER.convert(new Location(
                Latitude.degrees(node.getLatitude()), Longitude.degrees(node.getLongitude())));
        final CountryCodeProperties countryCodeProperties = this.loadingOption
                .getCountryBoundaryMap().getCountryCodeISO3(nodeLocation, false);
        return ISOCountryTag.COUNTRY_MISSING.equals(countryCodeProperties.getIso3CountryCode())
                || countryCodeProperties.usingNearestNeighbor();
    }

    /**
     * Find relations that have a member tree that does not contain any member that is not a
     * relation, or that contain no members at all.
     *
     * @param relation
     *            The relation to test
     * @return True if the relation has no members, or if it has only shallow relation members.
     */
    private boolean isShallow(final Relation relation)
    {
        final Queue<Long> nested = new LinkedList<>();
        final Set<Long> visited = new HashSet<>();
        nested.offer(relation.getId());
        while (!nested.isEmpty())
        {
            final Relation candidate = this.allRelations.get(nested.poll());
            if (candidate == null)
            {
                continue;
            }
            visited.add(candidate.getId());
            for (final RelationMember member : candidate.getMembers())
            {
                final long memberIdentifier = member.getMemberId();
                final EntityType memberType = member.getMemberType();
                if (memberType == EntityType.Node && this.store.containsNode(memberIdentifier)
                        || memberType == EntityType.Way && this.store.containsWay(memberIdentifier))
                {
                    // The relation is not shallow, after all!
                    return false;
                }
                else if (!visited.contains(memberIdentifier))
                {
                    nested.offer(memberIdentifier);
                }
                else
                {
                    logger.warn("Loop in the tree of relation {}. Offending member: {}",
                            relation.getId(), memberIdentifier);
                }
            }
        }
        return true;
    }

    /**
     * This function's job is to make sure that ways that are connected to the road network but
     * outside of country boundaries (bridges, ferry routes, etc) are still included. The steps are
     * the following:
     * <p>
     * <ul>
     * <li>While loop: as long as there has been less than MAXIMUM_NETWORK_EXTENSION iterations, and
     * as long as the last iteration actually attached new edges
     * <li>Loop through all the ways that are not included
     * <li>If the way is connected add it
     * </ul>
     * <p>
     * In a sense, each iteration grows the network of missing way one level.
     */
    private void keepOutsideWaysThatAreConnected()
    {
        if (this.loadingOption.isLoadWaysSpanningCountryBoundaries())
        {
            int counter = 0;
            final List<Boolean> lastAddedRoads = new ArrayList<>();
            final Set<Long> alreadyAddedWays = new HashSet<>();
            lastAddedRoads.add(true);
            while (counter++ < MAXIMUM_NETWORK_EXTENSION && lastAddedRoads.get(0))
            {
                logger.trace("keepOutsideWaysThatAreConnected while loop iteration {}", counter);
                lastAddedRoads.set(0, false);
                this.allWays.values().stream().filter(this::isHighwayOrFerry).forEach(way ->
                {
                    if (!this.store.containsWay(way.getId())
                            || !partialInside(way) && !alreadyAddedWays.contains(way.getId()))
                    {
                        final List<WayNode> wayNodes = way.getWayNodes();
                        for (final WayNode wayNode : wayNodes)
                        {
                            final long identifier = wayNode.getNodeId();
                            if (this.store.containsNodeInEdges(identifier))
                            {
                                this.store.addWay(way);
                                logger.info("Adding outside way {} connected at node {}",
                                        way.getId(), identifier);
                                lastAddedRoads.set(0, true);
                                alreadyAddedWays.add(way.getId());
                                this.nodeIdentifiersAtNetworkBoundary.remove(new Long(identifier));
                                for (final WayNode subWayNode : wayNodes)
                                {
                                    this.store.addNodeInEdge(subWayNode.getNodeId());
                                    if (!this.store.containsNode(subWayNode.getNodeId()))
                                    {
                                        final long subWayNodeIdentifier = subWayNode.getNodeId();
                                        if (subWayNodeIdentifier != identifier)
                                        {
                                            // The node is a new node outside of the network
                                            this.nodeIdentifiersAtNetworkBoundary
                                                    .add(subWayNodeIdentifier);
                                        }
                                        this.nodesOutsideOfPolygon.add(subWayNode.getNodeId());
                                    }
                                }
                                break;
                            }
                        }
                    }
                });
            }
        }
    }

    private boolean needPadding()
    {
        return this.loadingOption.isCountrySlicing() || this.loadingOption.isWaySectioning();
    }

    private boolean partialInside(final Way way)
    {
        for (final WayNode node : way.getWayNodes())
        {
            if (this.store.containsNode(node.getNodeId()))
            {
                return true;
            }
        }
        return false;
    }

    private Entity preProcess(final Entity entity)
    {
        // Put OSM Entity attributes into atlas tags
        final Collection<Tag> tags = entity.getTags();
        tags.add(new Tag(LastEditTimeTag.KEY, String.valueOf(entity.getTimestamp().getTime())));
        tags.add(new Tag(LastEditUserIdentifierTag.KEY, String.valueOf(entity.getUser().getId())));
        tags.add(new Tag(LastEditUserNameTag.KEY, entity.getUser().getName()));
        tags.add(new Tag(LastEditVersionTag.KEY, String.valueOf(entity.getVersion())));
        tags.add(new Tag(LastEditChangesetTag.KEY, String.valueOf(entity.getChangesetId())));

        return entity;
    }

    private void processNode(final Entity entity)
    {
        final Node node = (Node) entity;
        // Pad node identifier
        if (needPadding())
        {
            node.setId(PaddingIdentifierFactory.pad(node.getId()));
        }

        // First pass
        final Location location = new Location(Latitude.degrees(node.getLatitude()),
                Longitude.degrees(node.getLongitude()));
        if (this.firstPass && this.multiPolygon.fullyGeometricallyEncloses(location))
        {
            this.store.addNode(node);
        }

        // Second pass
        else if (this.secondPass)
        {
            // Record outside location for later edge and line process
            if (this.nodesOutsideOfPolygon.contains(node.getId()))
            {
                tagOutsideBoundaryNodes(node);
                this.store.addNode(node);
            }
        }

        this.statistic.incrementOsmNode();
    }

    private void processRelation(final Entity entity)
    {
        this.statistic.pauseOsmWayCounter();

        // Pad the identifier of relation as well as its members
        final Relation reference = (Relation) entity;
        final Relation relation;
        if (needPadding())
        {
            final List<RelationMember> list = new ArrayList<>();
            for (final RelationMember member : reference.getMembers())
            {
                list.add(new RelationMember(PaddingIdentifierFactory.pad(member.getMemberId()),
                        member.getMemberType(), member.getMemberRole()));
            }
            relation = this.store.createRelation(reference,
                    PaddingIdentifierFactory.pad(reference.getId()), list);
        }
        else
        {
            relation = reference;
        }

        // First pass
        if (this.firstPass)
        {
            this.allRelations.put(relation.getId(), relation);
        }
        this.statistic.incrementOsmRelation();
    }

    private void processWay(final Entity entity)
    {
        this.statistic.pauseOsmNodeCounter();

        // Pad the identifier of way as well as its waynodes
        final Way reference = (Way) entity;
        final Way way;
        if (needPadding())
        {
            final List<WayNode> list = new ArrayList<>();
            for (final WayNode wayNode : reference.getWayNodes())
            {
                list.add(new WayNode(PaddingIdentifierFactory.pad(wayNode.getNodeId())));
            }
            way = this.store.createWay(reference, PaddingIdentifierFactory.pad(reference.getId()),
                    list);
        }
        else
        {
            way = reference;
        }

        // First pass
        if (this.firstPass)
        {
            this.allWays.put(way.getId(), way);

            // Mark the ways inside
            if (partialInside(way))
            {
                this.store.addWay(way);
            }
        }
        this.statistic.incrementOsmWay();
    }

    private boolean shouldProcessEntity(final Entity entity)
    {
        if (entity instanceof Node)
        {
            return this.loadingOption.getOsmPbfNodeFilter().test(Taggable.with(entity.getTags()));
        }
        else if (entity instanceof Way)
        {
            return this.loadingOption.getOsmPbfWayFilter().test(Taggable.with(entity.getTags()));
        }
        else if (entity instanceof Relation)
        {
            return this.loadingOption.getOsmPbfRelationFilter()
                    .test(Taggable.with(entity.getTags()));
        }
        else
        {
            // No Bound filtering
            return true;
        }
    }

    private void tagOutsideBoundaryNodes(final Node node)
    {
        if (this.nodeIdentifiersAtNetworkBoundary.contains(node.getId()) && isOutsideBoundary(node))
        {
            final Map<String, String> boundaryNodeTags = Maps.hashMap(SyntheticBoundaryNodeTag.KEY,
                    SyntheticBoundaryNodeTag.EXISTING.name().toLowerCase());
            final Collection<Tag> tags = new ArrayList<>();
            tags.addAll(node.getTags());

            // Still add the boundary node tags
            tags.addAll(TAG_MAP_TO_TAG_COLLECTION_CONVERTER.convert(boundaryNodeTags));
            node.getTags().clear();
            node.getTags().addAll(tags);
        }
    }
}
