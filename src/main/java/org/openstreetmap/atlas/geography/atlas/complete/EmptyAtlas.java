package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Iterator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
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
import org.openstreetmap.atlas.geography.atlas.items.SnappedEdge;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.gson.JsonObject;

/**
 * Simple Atlas that supports single temporary entities. It does not do anything by design, as all
 * the Bloated entities are self-contained. They just need an Atlas to refer to, so they comply with
 * the Edge, Node, Area etc. definitions.
 *
 * @author matthieun
 */
public class EmptyAtlas implements Atlas
{
    private static final long serialVersionUID = 5265300513234306056L;

    @Override
    public Area area(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areas(final Predicate<Area> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areasCovering(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areasCovering(final Location location, final Predicate<Area> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon, final Predicate<Area> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areasWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeoJsonObject asGeoJson()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeoJsonObject asGeoJson(final Predicate<AtlasEntity> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle bounds()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Edge edge(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edges(final Predicate<Edge> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location, final Predicate<Edge> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final Polygon polygon, final Predicate<Edge> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Edge> edgesWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasEntity> entities()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <M extends AtlasEntity> Iterable<M> entities(final ItemType type,
            final Class<M> memberClass)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasEntity> entities(final Predicate<AtlasEntity> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasEntity> entitiesIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasEntity> entitiesIntersecting(final Polygon polygon,
            final Predicate<AtlasEntity> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public AtlasEntity entity(final long identifier, final ItemType type)
    {
        switch (type)
        {
            case NODE:
                return new CompleteNode(identifier);
            case EDGE:
                return new CompleteEdge(identifier);
            case AREA:
                return new CompleteArea(identifier);
            case LINE:
                return new CompleteLine(identifier);
            case POINT:
                return new CompletePoint(identifier);
            case RELATION:
                return new CompleteRelation(identifier);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    @Override
    public UUID getIdentifier()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName()
    {
        return "BloatedAtlas";
    }

    @Override
    public Iterable<AtlasItem> items()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasItem> items(final Predicate<AtlasItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasItem> itemsContaining(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasItem> itemsContaining(final Location location,
            final Predicate<AtlasItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasItem> itemsIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasItem> itemsIntersecting(final Polygon polygon,
            final Predicate<AtlasItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlasItem> itemsWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<AtlasEntity> iterator()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Line line(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItems()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItems(final Predicate<LineItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItemsContaining(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItemsContaining(final Location location,
            final Predicate<LineItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItemsIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItemsIntersecting(final Polygon polygon,
            final Predicate<LineItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LineItem> lineItemsWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> lines()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> lines(final Predicate<Line> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> linesContaining(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> linesContaining(final Location location, final Predicate<Line> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> linesIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> linesIntersecting(final Polygon polygon, final Predicate<Line> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> linesWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LocationItem> locationItems()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LocationItem> locationItems(final Predicate<LocationItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LocationItem> locationItemsWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LocationItem> locationItemsWithin(final Polygon polygon,
            final Predicate<LocationItem> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public AtlasMetaData metaData()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node node(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> nodes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> nodes(final Predicate<Node> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> nodesAt(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> nodesWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> nodesWithin(final Polygon polygon, final Predicate<Node> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfAreas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfEdges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfLines()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfNodes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfPoints()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfRelations()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point point(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Point> points()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Point> points(final Predicate<Point> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Point> pointsAt(final Location location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Point> pointsWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Point> pointsWithin(final Polygon polygon, final Predicate<Point> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Relation relation(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relations()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relations(final Predicate<Relation> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relationsLowerOrderFirst()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final Polygon polygon,
            final Predicate<Relation> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesWithin(final Polygon polygon)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(final WritableResource writableResource)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsGeoJson(final WritableResource resource)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsGeoJson(final WritableResource resource, final Predicate<AtlasEntity> matcher)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsLineDelimitedGeoJsonFeatures(final WritableResource resource,
            final BiConsumer<AtlasEntity, JsonObject> jsonMutator)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsLineDelimitedGeoJsonFeatures(final WritableResource resource,
            final Predicate<AtlasEntity> matcher,
            final BiConsumer<AtlasEntity, JsonObject> jsonMutator)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsList(final WritableResource resource)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsProto(final WritableResource resource)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsText(final WritableResource resource)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SnappedEdge snapped(final Location point, final Distance threshold)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<SnappedEdge> snaps(final Location point, final Distance threshold)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Atlas> subAtlas(final Polygon boundary, final AtlasCutType cutType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Atlas> subAtlas(final Predicate<AtlasEntity> matcher,
            final AtlasCutType cutType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String summary()
    {
        throw new UnsupportedOperationException();
    }
}
