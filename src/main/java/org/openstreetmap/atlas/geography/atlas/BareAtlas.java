package org.openstreetmap.atlas.geography.atlas;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
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
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.atlas.sub.SubAtlasCreator;
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

import com.google.gson.JsonObject;

/**
 * @author matthieun
 * @author tony
 * @author mgostintsev
 * @author hallahan
 */
public abstract class BareAtlas implements Atlas
{
    private static final long serialVersionUID = 4733707438968864018L;
    public static final int MAXIMUM_RELATION_DEPTH = 500;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    static
    {
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    // Transient name
    private transient String name;
    private final UUID identifier;

    private final transient SubAtlasCreator subAtlas;

    protected BareAtlas()
    {
        this.identifier = UUID.randomUUID();
        this.subAtlas = new SubAtlasCreator(this);
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
    @SuppressWarnings("unchecked")
    public <M extends AtlasEntity> Iterable<M> entities(final ItemType type,
            final Class<M> memberClass)
    {
        if (type.getMemberClass() != memberClass)
        {
            throw new CoreException("ItemType {} and class {} do not match!", type,
                    memberClass.getSimpleName());
        }
        switch (type)
        {
            case NODE:
                return (Iterable<M>) nodes();
            case EDGE:
                return (Iterable<M>) edges();
            case AREA:
                return (Iterable<M>) areas();
            case LINE:
                return (Iterable<M>) lines();
            case POINT:
                return (Iterable<M>) points();
            case RELATION:
                return (Iterable<M>) relations();
            default:
                throw new CoreException("ItemType {} unknown.", type);
        }
    }

    @Override
    public Iterable<AtlasEntity> entities(final Predicate<AtlasEntity> matcher)
    {
        return Iterables.filter(this, matcher);
    }

    @Override
    public Iterable<AtlasEntity> entitiesIntersecting(final GeometricSurface surface)
    {
        return new MultiIterable<>(itemsIntersecting(surface),
                relationsWithEntitiesIntersecting(surface));
    }

    @Override
    public Iterable<AtlasEntity> entitiesIntersecting(final GeometricSurface surface,
            final Predicate<AtlasEntity> matcher)
    {
        return Iterables.filter(entitiesIntersecting(surface), matcher);
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
    public UUID getIdentifier()
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
    public Iterable<AtlasItem> itemsIntersecting(final GeometricSurface surface)
    {
        return new MultiIterable<>(edgesIntersecting(surface), nodesWithin(surface),
                areasIntersecting(surface), linesIntersecting(surface), pointsWithin(surface));
    }

    @Override
    public Iterable<AtlasItem> itemsIntersecting(final GeometricSurface surface,
            final Predicate<AtlasItem> matcher)
    {
        return Iterables.filter(itemsIntersecting(surface), matcher);
    }

    @Override
    public Iterable<AtlasItem> itemsWithin(final GeometricSurface surface)
    {
        return new MultiIterable<>(locationItemsWithin(surface), lineItemsWithin(surface),
                areasWithin(surface));
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
    public Iterable<LineItem> lineItemsIntersecting(final GeometricSurface surface)
    {
        return new MultiIterable<>(edgesIntersecting(surface), linesIntersecting(surface));
    }

    @Override
    public Iterable<LineItem> lineItemsIntersecting(final GeometricSurface surface,
            final Predicate<LineItem> matcher)
    {
        return Iterables.filter(lineItemsIntersecting(surface), matcher);
    }

    @Override
    public Iterable<LineItem> lineItemsWithin(final GeometricSurface surface)
    {
        return new MultiIterable<>(edgesWithin(surface), linesWithin(surface));
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
    public Iterable<LocationItem> locationItemsWithin(final GeometricSurface surface)
    {
        return new MultiIterable<>(nodesWithin(surface), pointsWithin(surface));
    }

    @Override
    public Iterable<LocationItem> locationItemsWithin(final GeometricSurface surface,
            final Predicate<LocationItem> matcher)
    {
        return Iterables.filter(locationItemsWithin(surface), matcher);
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
                    if (member.getEntity() instanceof Relation
                            && !result.contains(member.getEntity()))
                    {
                        stageable = true;
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
        try (JsonWriter writer = new JsonWriter(resource))
        {
            writer.write(this.asGeoJson(matcher).jsonObject());
        }
    }

    @Override
    public void saveAsLineDelimitedGeoJsonFeatures(final WritableResource resource,
            final BiConsumer<AtlasEntity, JsonObject> jsonMutator)
    {
        saveAsLineDelimitedGeoJsonFeatures(resource, item -> true, jsonMutator);
    }

    @Override
    public void saveAsLineDelimitedGeoJsonFeatures(final WritableResource resource,
            final Predicate<AtlasEntity> matcher,
            final BiConsumer<AtlasEntity, JsonObject> jsonMutator)
    {
        try (JsonWriter writer = new JsonWriter(resource))
        {
            entities(matcher).forEach(entity ->
            {
                final JsonObject feature = entity.asGeoJsonGeometry();
                jsonMutator.accept(entity, feature);
                writer.writeLine(feature);
            });
        }
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
    public Optional<Atlas> subAtlas(final GeometricSurface boundary, final AtlasCutType cutType)
    {
        switch (cutType)
        {
            case SILK_CUT:
                return this.subAtlas.silkCut(boundary);
            case SOFT_CUT:
                return this.subAtlas.softCut(boundary, false);
            case HARD_CUT_ALL:
                return this.subAtlas.hardCutAllEntities(boundary);
            case HARD_CUT_RELATIONS_ONLY:
                return this.subAtlas.softCut(boundary, true);
            default:
                throw new CoreException("Unsupported Atlas cut type: {}", cutType);
        }
    }

    @Override
    public Optional<Atlas> subAtlas(final Predicate<AtlasEntity> matcher,
            final AtlasCutType cutType)
    {
        switch (cutType)
        {
            case SOFT_CUT:
                return this.subAtlas.softCut(matcher);
            case HARD_CUT_ALL:
                return this.subAtlas.hardCutAllEntities(matcher);
            case HARD_CUT_RELATIONS_ONLY:
                return this.subAtlas.hardCutRelationsOnly(matcher);
            default:
                throw new CoreException("Unsupported Atlas cut type: {}", cutType);
        }
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
        final String newLineAfterFeature = ",\n\t\t";
        final StringBuilder builder = new StringBuilder();
        builder.append("[Atlas <");
        builder.append(getName());
        builder.append(">: ");
        final StringList list = new StringList();
        list.add(Iterables.toString(this.nodes(), "Nodes", newLineAfterFeature));
        list.add(Iterables.toString(this.edges(), "Edges", newLineAfterFeature));
        list.add(Iterables.toString(this.areas(), "Areas", newLineAfterFeature));
        list.add(Iterables.toString(this.lines(), "Lines", newLineAfterFeature));
        list.add(Iterables.toString(this.points(), "Points", newLineAfterFeature));
        list.add(Iterables.toString(this.relations(), "Relations", newLineAfterFeature));
        builder.append(list.join(",\n\t"));
        builder.append("]");
        return builder.toString();
    }

    protected void setName(final String name)
    {
        this.name = name;
    }

}
