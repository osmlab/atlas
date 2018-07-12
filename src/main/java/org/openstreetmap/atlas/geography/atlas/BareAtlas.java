package org.openstreetmap.atlas.geography.atlas;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.items.SnappedEdge;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.proto.builder.ProtoAtlasBuilder;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author tony
 * @author mgostintsev
 */
public abstract class BareAtlas implements Atlas
{
    private static final long serialVersionUID = 4733707438968864018L;
    private static final Logger logger = LoggerFactory.getLogger(BareAtlas.class);
    private static final int MAXIMUM_RELATION_DEPTH = 500;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    private static final AtomicInteger ATLAS_IDENTIFIER_FACTORY = new AtomicInteger();
    static
    {
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    // Transient name
    private transient String name;

    private final transient int identifier;

    protected BareAtlas()
    {
        this.identifier = ATLAS_IDENTIFIER_FACTORY.getAndIncrement();
    }

    @Override
    public Iterable<Area> areas(final Predicate<Area> matcher)
    {
        return Iterables.filter(areas(), matcher);
    }

    @Override
    public GeoJsonObject asGeoJson()
    {
        return asGeoJson(entity -> true);
    }

    @Override
    public GeoJsonObject asGeoJson(final Predicate<AtlasEntity> matcher)
    {
        return new GeoJsonBuilder().create(Iterables.filterTranslate(entities(),
                atlasEntity -> atlasEntity.toGeoJsonBuildingBlock(), matcher));
    }

    @Override
    public Iterable<Edge> edges(final Predicate<Edge> matcher)
    {
        return Iterables.filter(edges(), matcher);
    }

    @Override
    public Iterable<AtlasEntity> entities()
    {
        return new MultiIterable<>(items(), relations());
    }

    @Override
    public Iterable<AtlasEntity> entities(final Predicate<AtlasEntity> matcher)
    {
        return Iterables.filter(this, matcher);
    }

    @Override
    public Iterable<AtlasEntity> entitiesIntersecting(final Polygon polygon)
    {
        return new MultiIterable<>(itemsIntersecting(polygon),
                relationsWithEntitiesIntersecting(polygon));
    }

    @Override
    public Iterable<AtlasEntity> entitiesIntersecting(final Polygon polygon,
            final Predicate<AtlasEntity> matcher)
    {
        return Iterables.filter(entitiesIntersecting(polygon), matcher);
    }

    @Override
    public AtlasEntity entity(final long identifier, final ItemType type)
    {
        switch (type)
        {
            case NODE:
                return node(identifier);
            case EDGE:
                return edge(identifier);
            case AREA:
                return area(identifier);
            case LINE:
                return line(identifier);
            case POINT:
                return point(identifier);
            case RELATION:
                return relation(identifier);
            default:
                throw new CoreException("Unknown type {}", type);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Atlas)
        {
            if (this == other)
            {
                // Avoid comparing each item.
                return true;
            }
            final Atlas that = (Atlas) other;
            for (final AtlasEntity thisEntity : this)
            {
                final AtlasEntity thatEntity = that.entity(thisEntity.getIdentifier(),
                        thisEntity.getType());
                if (thatEntity == null || !thisEntity.getTags().equals(thatEntity.getTags()))
                {
                    return false;
                }
                if (thisEntity instanceof Area)
                {
                    final Polygon thisPolygon = ((Area) thisEntity).asPolygon();
                    final Polygon thatPolygon = ((Area) thatEntity).asPolygon();
                    if (!thisPolygon.equals(thatPolygon))
                    {
                        return false;
                    }
                }
                else if (thisEntity instanceof LineItem)
                {
                    final PolyLine thisPolyLine = ((LineItem) thisEntity).asPolyLine();
                    final PolyLine thatPolyLine = ((LineItem) thatEntity).asPolyLine();
                    if (!thisPolyLine.equals(thatPolyLine))
                    {
                        return false;
                    }
                }
                else if (thisEntity instanceof LocationItem)
                {
                    final Location thisLocation = ((LocationItem) thisEntity).getLocation();
                    final Location thatLocation = ((LocationItem) thatEntity).getLocation();
                    if (!thisLocation.equals(thatLocation))
                    {
                        return false;
                    }
                }
                else if (thisEntity instanceof Relation)
                {
                    final RelationMemberList thisMembers = ((Relation) thisEntity).members();
                    final RelationMemberList thatMembers = ((Relation) thatEntity).members();
                    if (!thisMembers.equals(thatMembers))
                    {
                        return false;
                    }
                }
                else
                {
                    throw new CoreException("Unknown type: {}", thisEntity.getClass().getName());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public String getName()
    {
        if (this.name == null)
        {
            return String.valueOf(this.getIdentifier());
        }
        else
        {
            return this.name;
        }
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.numberOfNodes() + this.numberOfEdges() + this.numberOfAreas()
                + this.numberOfLines() + this.numberOfPoints() + this.numberOfRelations());
    }

    @Override
    public Iterable<AtlasItem> items()
    {
        return new MultiIterable<>(nodes(), edges(), areas(), lines(), points());
    }

    @Override
    public Iterable<AtlasItem> items(final Predicate<AtlasItem> matcher)
    {
        return Iterables.filter(items(), matcher);
    }

    @Override
    public Iterable<AtlasItem> itemsContaining(final Location location)
    {
        return new MultiIterable<>(edgesContaining(location), nodesAt(location),
                areasCovering(location), linesContaining(location), pointsAt(location));
    }

    @Override
    public Iterable<AtlasItem> itemsContaining(final Location location,
            final Predicate<AtlasItem> matcher)
    {
        return Iterables.filter(itemsContaining(location), matcher);
    }

    @Override
    public Iterable<AtlasItem> itemsIntersecting(final Polygon polygon)
    {
        return new MultiIterable<>(edgesIntersecting(polygon), nodesWithin(polygon),
                areasIntersecting(polygon), linesIntersecting(polygon), pointsWithin(polygon));
    }

    @Override
    public Iterable<AtlasItem> itemsIntersecting(final Polygon polygon,
            final Predicate<AtlasItem> matcher)
    {
        return Iterables.filter(itemsIntersecting(polygon), matcher);
    }

    @Override
    public Iterator<AtlasEntity> iterator()
    {
        return new MultiIterable<AtlasEntity>(nodes(), edges(), areas(), lines(), points(),
                relations()).iterator();
    }

    @Override
    public Iterable<LineItem> lineItems()
    {
        return new MultiIterable<>(edges(), lines());
    }

    @Override
    public Iterable<LineItem> lineItems(final Predicate<LineItem> matcher)
    {
        return Iterables.filter(lineItems(), matcher);
    }

    @Override
    public Iterable<LineItem> lineItemsContaining(final Location location)
    {
        return new MultiIterable<>(edgesContaining(location), linesContaining(location));
    }

    @Override
    public Iterable<LineItem> lineItemsContaining(final Location location,
            final Predicate<LineItem> matcher)
    {
        return Iterables.filter(lineItemsContaining(location), matcher);
    }

    @Override
    public Iterable<LineItem> lineItemsIntersecting(final Polygon polygon)
    {
        return new MultiIterable<>(edgesIntersecting(polygon), linesIntersecting(polygon));
    }

    @Override
    public Iterable<LineItem> lineItemsIntersecting(final Polygon polygon,
            final Predicate<LineItem> matcher)
    {
        return Iterables.filter(lineItemsIntersecting(polygon), matcher);
    }

    @Override
    public Iterable<Line> lines(final Predicate<Line> matcher)
    {
        return Iterables.filter(lines(), matcher);
    }

    @Override
    public Iterable<LocationItem> locationItems()
    {
        return new MultiIterable<>(nodes(), points());
    }

    @Override
    public Iterable<LocationItem> locationItems(final Predicate<LocationItem> matcher)
    {
        return Iterables.filter(locationItems(), matcher);
    }

    @Override
    public Iterable<LocationItem> locationItemsWithin(final Polygon polygon)
    {
        return new MultiIterable<>(nodesWithin(polygon), pointsWithin(polygon));
    }

    @Override
    public Iterable<LocationItem> locationItemsWithin(final Polygon polygon,
            final Predicate<LocationItem> matcher)
    {
        return Iterables.filter(locationItemsWithin(polygon), matcher);
    }

    @Override
    public Iterable<Node> nodes(final Predicate<Node> matcher)
    {
        return Iterables.filter(nodes(), matcher);
    }

    @Override
    public Iterable<Point> points(final Predicate<Point> matcher)
    {
        return Iterables.filter(points(), matcher);
    }

    @Override
    public Iterable<Relation> relations(final Predicate<Relation> matcher)
    {
        return Iterables.filter(relations(), matcher);
    }

    @Override
    public Iterable<Relation> relationsLowerOrderFirst()
    {
        List<Relation> stagedRelations = new ArrayList<>();
        final Set<Relation> result = new LinkedHashSet<>();
        // First pass
        for (final Relation relation : relations())
        {
            boolean stageable = false;
            final RelationMemberList members = relation.members();
            for (final RelationMember member : members)
            {
                if (member.getEntity() instanceof Relation)
                {
                    stageable = true;
                }
            }
            if (stageable)
            {
                stagedRelations.add(relation);
            }
            else
            {
                result.add(relation);
            }
        }
        // Second pass
        int depth = 0;
        while (!stagedRelations.isEmpty() && depth < MAXIMUM_RELATION_DEPTH)
        {
            final List<Relation> newStagedRelations = new ArrayList<>();
            for (final Relation relation : stagedRelations)
            {
                boolean stageable = false;
                final RelationMemberList members = relation.members();
                for (final RelationMember member : members)
                {
                    if (member.getEntity() instanceof Relation)
                    {
                        if (!result.contains(member.getEntity()))
                        {
                            stageable = true;
                        }
                    }
                }
                if (stageable)
                {
                    newStagedRelations.add(relation);
                }
                else
                {
                    result.add(relation);
                }
            }
            stagedRelations = newStagedRelations;
            depth++;
        }
        return result;
    }

    @Override
    public void saveAsGeoJson(final WritableResource resource)
    {
        saveAsGeoJson(resource, item -> true);
    }

    @Override
    public void saveAsGeoJson(final WritableResource resource, final Predicate<AtlasEntity> matcher)
    {
        final JsonWriter writer = new JsonWriter(resource);
        writer.write(this.asGeoJson(matcher).jsonObject());
        writer.close();
    }

    @Override
    public void saveAsList(final WritableResource resource)
    {
        final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(resource.write(), StandardCharsets.UTF_8));
        try
        {
            writer.write(this.toString());
            Streams.close(writer);
        }
        catch (final IOException e)
        {
            Streams.close(writer);
            throw new CoreException("Could not save atlas as list", e);
        }
    }

    @Override
    public void saveAsProto(final WritableResource resource)
    {
        new ProtoAtlasBuilder().write(this, resource);
    }

    @Override
    public void saveAsText(final WritableResource resource)
    {
        new TextAtlasBuilder().write(this, resource);
    }

    @Override
    public SnappedEdge snapped(final Location point, final Distance threshold)
    {
        SnappedEdge result = null;
        for (final Edge edge : this.edgesIntersecting(point.boxAround(threshold)))
        {
            final SnappedEdge candidate = new SnappedEdge(point.snapTo(edge.asPolyLine()), edge);
            if (result == null || candidate.getDistance().isLessThan(result.getDistance()))
            {
                result = candidate;
            }
        }
        return result;
    }

    @Override
    public SortedSet<SnappedEdge> snaps(final Location point, final Distance threshold)
    {
        final SortedSet<SnappedEdge> snaps = new TreeSet<>();
        for (final Edge edge : this.edgesIntersecting(point.boxAround(threshold)))
        {
            final SnappedEdge candidate = new SnappedEdge(point.snapTo(edge.asPolyLine()), edge);
            snaps.add(candidate);
        }
        return snaps;
    }

    @Override
    public Optional<Atlas> subAtlas(final Polygon boundary)
    {
        final Time begin = Time.now();

        // Generate the size estimates, then the builder.
        // Nodes estimating is a bit tricky. We want to include all the nodes within the polygon,
        // but we also want to include those attached to edges that span outside the polygon.
        // Instead of doing a count to have an exact number, we choose here to have an arbitrary 20%
        // buffer on top of the nodes inside the polygon. This mostly avoids resizing.
        final double nodeRatioBuffer = 1.2;
        final long nodeNumber = Math.round(Iterables.size(nodesWithin(boundary)) * nodeRatioBuffer);
        final long edgeNumber = Iterables.size(edgesIntersecting(boundary));
        final long areaNumber = Iterables.size(areasIntersecting(boundary));
        final long lineNumber = Iterables.size(linesIntersecting(boundary));
        final long pointNumber = Iterables.size(pointsWithin(boundary));
        final long relationNumber = Iterables.size(relationsWithEntitiesIntersecting(boundary));
        final AtlasSize size = new AtlasSize(edgeNumber, nodeNumber, areaNumber, lineNumber,
                pointNumber, relationNumber);
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(size)
                .withMetaData(metaData());

        // Predicates to test if some items have already been added.
        final Predicate<Node> hasNode = item -> builder.peek().node(item.getIdentifier()) != null;
        final Predicate<Edge> hasEdge = item -> builder.peek().edge(item.getIdentifier()) != null;
        final Predicate<Area> hasArea = item -> builder.peek().area(item.getIdentifier()) != null;
        final Predicate<Line> hasLine = item -> builder.peek().line(item.getIdentifier()) != null;
        final Predicate<Point> hasPoint = item -> builder.peek()
                .point(item.getIdentifier()) != null;
        final Predicate<Relation> hasRelation = item -> builder.peek()
                .relation(item.getIdentifier()) != null;

        // Add the nodes needed for Edges.
        edgesIntersecting(boundary).forEach(edge ->
        {
            final Node start = edge.start();
            final Node end = edge.end();
            if (!hasNode.test(start))
            {
                builder.addNode(start.getIdentifier(), start.getLocation(), start.getTags());
            }
            if (!hasNode.test(end))
            {
                builder.addNode(end.getIdentifier(), end.getLocation(), end.getTags());
            }
        });

        // Add the remaining Nodes if any.
        nodesWithin(boundary, hasNode.negate()).forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));

        // Add the edges
        edgesIntersecting(boundary).forEach(
                edge -> builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags()));

        // Add the Areas
        areasIntersecting(boundary).forEach(
                area -> builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));

        // Add the Lines
        linesIntersecting(boundary).forEach(
                line -> builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));

        // Add the Points
        pointsWithin(boundary).forEach(point -> builder.addPoint(point.getIdentifier(),
                point.getLocation(), point.getTags()));

        // Add the Relations: Because relations can also be members of other relations, this is
        // trickier. There is a while loop re-checking if each relation added needs to trigger the
        // loading of another relation if it is member in another relation.
        final List<Boolean> relationAdded = new ArrayList<>();
        relationAdded.add(true);
        while (numberOfRelations() > Iterables.size(builder.peek().relations())
                && relationAdded.get(0))
        {
            relationAdded.set(0, false);
            // Check all the relations that are not in the sub atlas
            Iterables.stream(relations()).filter(hasRelation.negate()).forEach(relation ->
            {
                final RelationMemberList members = relation.members();
                final List<RelationMember> validMembers = new ArrayList<>();
                // And consider them only if they have members that have already been added to the
                // sub atlas.
                members.forEach(member ->
                {
                    final AtlasEntity entity = member.getEntity();
                    // Non-Relation members
                    if (entity instanceof Node && hasNode.test((Node) entity)
                            || entity instanceof Edge && hasEdge.test((Edge) entity)
                            || entity instanceof Area && hasArea.test((Area) entity)
                            || entity instanceof Line && hasLine.test((Line) entity)
                            || entity instanceof Point && hasPoint.test((Point) entity))
                    {
                        validMembers.add(member);
                    }
                    // Relation members
                    if (entity instanceof Relation && hasRelation.test((Relation) entity))
                    {
                        relationAdded.set(0, true);
                        validMembers.add(member);
                    }
                });
                if (!validMembers.isEmpty())
                {
                    // If there are legitimate members, we need to add the relation to the sub atlas
                    final RelationBean structure = new RelationBean();
                    validMembers.forEach(validMember ->
                    {
                        structure.addItem(validMember.getEntity().getIdentifier(),
                                validMember.getRole(), ItemType.forEntity(validMember.getEntity()));
                    });
                    builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                            structure, relation.getTags());
                }
            });
        }
        logger.info("Cut sub-atlas in {}", begin.elapsedSince());
        return Optional.ofNullable(builder.get());
    }

    @Override
    public Optional<Atlas> subAtlas(final Predicate<AtlasEntity> matcher)
    {
        logger.debug("Filtering Atlas {} with meta-data {}", this.getName(), this.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity numbers. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(size())
                .withMetaData(metaData());

        // First, index all the nodes contained by relations and all start/stop nodes from edges
        // contained by relations
        for (final AtlasEntity entity : relations(relation -> matcher.test(relation)))
        {
            indexAllNodesFromRelation((Relation) entity, builder, 0);
        }

        // Next, index all the individual nodes and edge start/stop nodes coming from the predicate
        final Iterable<AtlasEntity> nodes = Iterables.stream(nodes(item -> matcher.test(item)))
                .map(item -> (AtlasEntity) item);
        final Iterable<AtlasEntity> edges = Iterables.stream(edges(item -> matcher.test(item)))
                .map(item -> (AtlasEntity) item);
        for (final AtlasEntity entity : new MultiIterable<>(nodes, edges))
        {
            addNodesToAtlas(entity, builder);
        }

        // Next, add the Lines, Points, Areas and Edges. Edges are a little trickier - 1) They rely
        // on their start/end nodes to have already been added 2) They can potentially pull in nodes
        // that weren't matched by the given predicate. These two cases are handled above.
        // Similarly, Relations depend on all other entities to have been added, since they make up
        // the member list. For this pass, add all entities, except Relations, that match the given
        // Predicate to the builder.
        for (final Edge edge : edges(item -> matcher.test(item)))
        {
            indexEdge(edge, builder);
        }
        for (final Area area : areas(item -> matcher.test(item)))
        {
            builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags());
        }
        for (final Line line : lines(item -> matcher.test(item)))
        {
            builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags());
        }
        for (final Point point : points(item -> matcher.test(item)))
        {
            builder.addPoint(point.getIdentifier(), point.getLocation(), point.getTags());
        }

        // It's now safe to add Relations. There are two caveats: 1. A relation member may not
        // have been pulled in by the given predicate. In order to maintain relation validity, we
        // need to pull in the un-indexed members. 2. The member may be a relation that hasn't been
        // indexed yet. We check if any of the members are un-indexed relations and if so, we add
        // the parent relation to a staged set to be processed later (after the child member
        // relation has been processed). This guarantees that anything we add to the index has all
        // of its members indexed already.
        Set<Long> stagedRelationIdentifiers = new HashSet<>();
        final Iterable<Relation> relations = relations(item -> matcher.test(item));
        for (final Relation relation : relations)
        {
            checkRelationMembersAndIndexRelation(relation, matcher, stagedRelationIdentifiers,
                    builder);
        }

        // Process all staged relations
        int iterations = 0;
        while (++iterations < MAXIMUM_RELATION_DEPTH && !stagedRelationIdentifiers.isEmpty())
        {
            logger.trace("Copying relations level {} deep.", iterations);
            final Set<Long> stagedRelationIdentifiersCopy = new HashSet<>();
            for (final Long relationIdentifier : stagedRelationIdentifiers)
            {
                // Apply the same logic to all staged relations - if any member is an
                // un-indexed relations, re-stage the parent relation.
                final Relation relation = this.relation(relationIdentifier);
                checkRelationMembersAndIndexRelation(relation, matcher,
                        stagedRelationIdentifiersCopy, builder);
            }
            stagedRelationIdentifiers = stagedRelationIdentifiersCopy;
        }

        if (iterations >= MAXIMUM_RELATION_DEPTH)
        {
            throw new CoreException(
                    "There might be a loop in relations! It took more than {} loops to update the relation from atlas {}.",
                    MAXIMUM_RELATION_DEPTH, this.getName());
        }

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info("Cut sub-atlas in {}", begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    @Override
    public String summary()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(this.getClass().getSimpleName());
        builder.append(": Nodes = ");
        builder.append(NUMBER_FORMAT.format(this.numberOfNodes()));
        builder.append(", Edges = ");
        builder.append(NUMBER_FORMAT.format(this.numberOfEdges()));
        builder.append(", Areas = ");
        builder.append(NUMBER_FORMAT.format(this.numberOfAreas()));
        builder.append(", Lines = ");
        builder.append(NUMBER_FORMAT.format(this.numberOfLines()));
        builder.append(", Points = ");
        builder.append(NUMBER_FORMAT.format(this.numberOfPoints()));
        builder.append(", Relations = ");
        builder.append(NUMBER_FORMAT.format(this.numberOfRelations()));
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Atlas <");
        builder.append(getName());
        builder.append(">: ");
        final StringList list = new StringList();
        list.add(Iterables.toString(this.nodes(), "Nodes", ",\n\t\t"));
        list.add(Iterables.toString(this.edges(), "Edges", ",\n\t\t"));
        list.add(Iterables.toString(this.areas(), "Areas", ",\n\t\t"));
        list.add(Iterables.toString(this.lines(), "Lines", ",\n\t\t"));
        list.add(Iterables.toString(this.points(), "Points", ",\n\t\t"));
        list.add(Iterables.toString(this.relations(), "Relations", ",\n\t\t"));
        builder.append(list.join(",\n\t"));
        builder.append("]");
        return builder.toString();
    }

    protected void setName(final String name)
    {
        this.name = name;
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} method to add all {@link Node}s to the
     * sub-atlas. If the given {@link AtlasEntity} is a {@link Node}, it will be added. If it's an
     * {@link Edge}, its start and end {@link Node}s will be added. Note: the {@link Edge} itself
     * will NOT be added here due to the constraint that all {@link Node}s must be indexed before
     * any {@link Edge}s can be indexed.
     *
     * @param entity
     *            The {@link AtlasEntity} whose {@link Node}s we intend to add to the sub-atlas
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void addNodesToAtlas(final AtlasEntity entity, final PackedAtlasBuilder builder)
    {
        final long identifier = entity.getIdentifier();
        if (entity instanceof Node)
        {
            final Node node = (Node) entity;
            if (builder.peek().node(identifier) == null)
            {
                builder.addNode(identifier, node.getLocation(), node.getTags());
            }
        }
        else if (entity instanceof Edge)
        {
            final Edge edge = (Edge) entity;
            final Node start = edge.start();
            final Node end = edge.end();
            final long startIdentifier = start.getIdentifier();
            final long endIdentifier = end.getIdentifier();

            if (builder.peek().node(startIdentifier) == null)
            {
                builder.addNode(startIdentifier, start.getLocation(), start.getTags());
            }

            if (builder.peek().node(endIdentifier) == null)
            {
                builder.addNode(endIdentifier, end.getLocation(), end.getTags());
            }

            // Note: Don't add the Edge here, only care about Nodes.
        }
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} method to add a {@link Relation} to the
     * sub-atlas. We loop through each {@link RelationMemmber} of the given {@link Relation}, and
     * add any member that hasn't been added. If the given {@link Relation} contains an un-added
     * {@link Relation} member, then we can't process this {@link Relation} and we store its
     * identifier in the given {@link Set} of staged identifiers. If we've looked at all members and
     * didn't find any un-added {@link Relation}s, then we safely add the given {@link Relation} to
     * the sub-atlas.
     *
     * @param relation
     *            The {@link Relation} we intend to add
     * @param stagedRelationIdentifiers
     *            The {@link Set} of {@link Relation} identifiers used to stage {@link Relation}s
     *            that contain an un-added {@link Relation} member.
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void checkRelationMembersAndIndexRelation(final Relation relation,
            final Predicate<AtlasEntity> matcher, final Set<Long> stagedRelationIdentifiers,
            final PackedAtlasBuilder builder)
    {
        boolean allRelationsMembersAreIndexed = true;
        final long relationIdentifier = relation.getIdentifier();
        for (final RelationMember member : relation.members())
        {
            final ItemType memberType = member.getEntity().getType();

            // If the relation member wasn't pulled in by the given predicate, we still want
            // to add it to maintain relation integrity
            final long memberIdentifier = member.getEntity().getIdentifier();
            switch (memberType)
            {
                case EDGE:
                    final Edge edgeMember = (Edge) member.getEntity();
                    indexEdge(edgeMember, builder);
                    break;
                case AREA:
                    final Area areaMember = (Area) member.getEntity();
                    if (builder.peek().area(memberIdentifier) == null)
                    {
                        builder.addArea(memberIdentifier, areaMember.asPolygon(),
                                areaMember.getTags());
                    }
                    break;
                case LINE:
                    final Line lineMember = (Line) member.getEntity();
                    if (builder.peek().line(memberIdentifier) == null)
                    {
                        builder.addLine(memberIdentifier, lineMember.asPolyLine(),
                                lineMember.getTags());
                    }
                    break;
                case POINT:
                    final Point pointMember = (Point) member.getEntity();
                    if (builder.peek().point(memberIdentifier) == null)
                    {
                        builder.addPoint(memberIdentifier, pointMember.getLocation(),
                                pointMember.getTags());
                    }
                    break;
                case RELATION:
                    final Relation relationMember = (Relation) member.getEntity();
                    // If the built Atlas does not have that member relation yet
                    if (builder.peek().relation(memberIdentifier) == null)
                    {
                        if (!matcher.test(relationMember))
                        {
                            // If the relation member is not there because the matcher excluded it,
                            // then add it back to make sure the relation structure is not broken
                            stagedRelationIdentifiers.add(memberIdentifier);
                        }
                        stagedRelationIdentifiers.add(relationIdentifier);
                        allRelationsMembersAreIndexed = false;
                    }
                    break;
                default:
                    // We already handled all Relation member Nodes
                    break;
            }
        }

        // All members of this relation have been indexed, it's safe to index it.
        if (allRelationsMembersAreIndexed && builder.peek().relation(relationIdentifier) == null)
        {
            final RelationBean bean = new RelationBean();
            relation.members().forEach(member -> bean.addItem(member.getEntity().getIdentifier(),
                    member.getRole(), member.getEntity().getType()));
            builder.addRelation(relationIdentifier, relation.getOsmIdentifier(), bean,
                    relation.getTags());
        }
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} method to index all {@link Node}s contained
     * by the given {@link Relation} and the start/end {@link Node}s for all {@link Edge}s contained
     * by the given {@link Relation} and recursively do this for all sub-{@link Relation}s.
     *
     * @param relation
     *            The {@link Relation} which we're exploring
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     * @param relationDepth
     *            The depth of the {@link Relation} to guard against loops and extremely large
     *            {@link Relation}s
     */
    private void indexAllNodesFromRelation(final Relation relation,
            final PackedAtlasBuilder builder, final int relationDepth)
    {
        if (relationDepth > MAXIMUM_RELATION_DEPTH)
        {
            throw new CoreException(
                    "Relation depth is greater than {} for the relation from atlas {}.",
                    MAXIMUM_RELATION_DEPTH, this.getName());
        }

        for (final RelationMember member : relation.members())
        {
            final ItemType type = member.getEntity().getType();
            if (ItemType.NODE == type || ItemType.EDGE == type)
            {
                // Add all individual nodes and nodes from edges for this relation
                addNodesToAtlas(member.getEntity(), builder);
            }
            else if (ItemType.RELATION == type)
            {
                // Recursively get the nodes for the sub-relation
                final Relation relationMember = (Relation) member.getEntity();
                indexAllNodesFromRelation(relationMember, builder, relationDepth + 1);
            }
            else
            {
                // Do nothing. We only care about walking the relations tree and indexing all Nodes.
            }
        }
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} to add the given {@link Edge} and its
     * reverse {@link Edge}.
     *
     * @param edge
     *            The {@link Edge} whose reverse {@link Edge} we intend to add
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void indexEdge(final Edge edge, final PackedAtlasBuilder builder)
    {
        // Add the given edge
        if (builder.peek().edge(edge.getIdentifier()) == null)
        {
            builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
        }

        // Add the reverse
        if (edge.hasReverseEdge())
        {
            final Edge reverseEdge = edge.reversed().get();
            final long reverseEdgeIdentifier = reverseEdge.getIdentifier();
            if (builder.peek().edge(reverseEdgeIdentifier) == null)
            {
                builder.addEdge(reverseEdgeIdentifier, reverseEdge.asPolyLine(),
                        reverseEdge.getTags());
            }
        }
    }
}
