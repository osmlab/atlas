package org.openstreetmap.atlas.geography.atlas;

import java.io.Serializable;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
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
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.gson.JsonObject;

/**
 * Atlas is a representation of an OpenStreetMap region in memory. It is a navigable collection of
 * unidirectional {@link Edge}s and {@link Node}s. It is designed to be close to the OpenStreetMap
 * model. It also contains a collection of non-navigable geolocated items that can be {@link Point}
 * s, {@link Line}s or {@link Area}s. All can be members of {@link Relation}s.
 *
 * @author matthieun
 * @author tony
 */
public interface Atlas extends Located, Iterable<AtlasEntity>, Serializable
{
    /**
     * @param identifier
     *            The {@link Area}'s identifier
     * @return The {@link Area} that corresponds to the provided identifier
     */
    Area area(long identifier);

    /**
     * @return All the {@link Area}s in this {@link Atlas}
     */
    Iterable<Area> areas();

    /**
     * Return all the {@link Area}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link Area}s matching the {@link Predicate}.
     */
    Iterable<Area> areas(Predicate<Area> matcher);

    /**
     * Return all the {@link Area}s covering some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Area}s covering the {@link Location}.
     */
    Iterable<Area> areasCovering(Location location);

    /**
     * Return all the {@link Area}s matching a {@link Predicate} and covering some {@link Location}.
     *
     * @param matcher
     *            The matcher to consider
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Area}s matching the {@link Predicate} and covering the
     *         {@link Location}.
     */
    Iterable<Area> areasCovering(Location location, Predicate<Area> matcher);

    /**
     * Return all the {@link Area}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Area}s within and/or intersecting the polygon.
     */
    Iterable<Area> areasIntersecting(Polygon polygon);

    /**
     * Return all the {@link Area}s matching a {@link Predicate}, and within and/or intersecting
     * some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Area}s matching the {@link Predicate}, and within and/or intersecting
     *         the polygon.
     */
    Iterable<Area> areasIntersecting(Polygon polygon, Predicate<Area> matcher);

    /**
     * Return all the {@link Area}s fully within some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Area}s fully within the polygon.
     */
    Iterable<Area> areasWithin(Polygon polygon);

    /**
     * @return A {@link GeoJsonObject} that contains all the features in this {@link Atlas}
     */
    GeoJsonObject asGeoJson();

    /**
     * @param matcher
     *            The matcher to consider
     * @return A {@link GeoJsonObject} that contains part the features in this {@link Atlas} which
     *         matches the given matcher
     */
    GeoJsonObject asGeoJson(Predicate<AtlasEntity> matcher);

    /**
     * Clone this {@link Atlas} to a {@link PackedAtlas}. Do not uses "clone" so as not to be
     * confused with the clone function in {@link Cloneable}, which this interface does not extend.
     *
     * @return The {@link PackedAtlas} copy of this {@link Atlas}.
     */
    default PackedAtlas cloneToPackedAtlas()
    {
        return new PackedAtlasCloner().cloneFrom(this);
    }

    /**
     * @param identifier
     *            The {@link Edge}'s identifier
     * @return The {@link Edge} that corresponds to the provided identifier
     */
    Edge edge(long identifier);

    /**
     * @return All the {@link Edge}s in this {@link Atlas}
     */
    Iterable<Edge> edges();

    /**
     * Return all the {@link Edge}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link Edge}s matching the {@link Predicate}.
     */
    Iterable<Edge> edges(Predicate<Edge> matcher);

    /**
     * Return all the {@link Edge}s containing some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Edge}s containing the {@link Location}.
     */
    Iterable<Edge> edgesContaining(Location location);

    /**
     * Return all the {@link Edge}s matching a {@link Predicate} and containing some
     * {@link Location}.
     *
     * @param matcher
     *            The matcher to consider
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Edge}s matching the {@link Predicate} and containing the
     *         {@link Location}.
     */
    Iterable<Edge> edgesContaining(Location location, Predicate<Edge> matcher);

    /**
     * Return all the {@link Edge}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Edge}s within and/or intersecting the polygon.
     */
    Iterable<Edge> edgesIntersecting(Polygon polygon);

    /**
     * Return all the {@link Edge}s matching a {@link Predicate}, and within and/or intersecting
     * some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Edge}s matching the {@link Predicate}, and within and/or intersecting
     *         the polygon.
     */
    Iterable<Edge> edgesIntersecting(Polygon polygon, Predicate<Edge> matcher);

    /**
     * Return all the {@link Edge}s fully within some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Edge}s fully within the polygon.
     */
    Iterable<Edge> edgesWithin(Polygon polygon);

    /**
     * Return all the {@link AtlasEntity}s
     *
     * @return All the {@link AtlasEntity}s
     */
    Iterable<AtlasEntity> entities();

    /**
     * Return all the {@link AtlasEntity}s of a specific type
     *
     * @param type
     *            The type to restrain to
     * @param memberClass
     *            The class of the member
     * @param <M>
     *            The AtlasEntity type
     * @return All the {@link AtlasEntity}s of a specific type
     */
    <M extends AtlasEntity> Iterable<M> entities(ItemType type, Class<M> memberClass);

    /**
     * Return all the {@link AtlasEntity}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link AtlasEntity}s matching the {@link Predicate}.
     */
    Iterable<AtlasEntity> entities(Predicate<AtlasEntity> matcher);

    /**
     * Return all the {@link AtlasEntity}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The {@link Polygon} to consider
     * @return All the {@link AtlasEntity}s within and/or intersecting the {@link Polygon}.
     */
    Iterable<AtlasEntity> entitiesIntersecting(Polygon polygon);

    /**
     * Return all the {@link AtlasEntity}s matching a {@link Predicate}, and within and/or
     * intersecting some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link AtlasEntity}s matching the {@link Predicate}, and within and/or
     *         intersecting the polygon.
     */
    Iterable<AtlasEntity> entitiesIntersecting(Polygon polygon, Predicate<AtlasEntity> matcher);

    /**
     * Get an entity from identifier and type
     *
     * @param identifier
     *            the identifier
     * @param type
     *            The type
     * @return The corresponding AtlasEntity, null if any.
     */
    AtlasEntity entity(long identifier, ItemType type);

    /**
     * @return This Atlas' identifier
     */
    UUID getIdentifier();

    /**
     * @return This Atlas' optional name. If not specified, should return a String version of the
     *         identifier.
     */
    String getName();

    /**
     * Return all the {@link AtlasItem}s
     *
     * @return All the {@link AtlasItem}s
     */
    Iterable<AtlasItem> items();

    /**
     * Return all the {@link AtlasItem}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link AtlasItem}s matching the {@link Predicate}.
     */
    Iterable<AtlasItem> items(Predicate<AtlasItem> matcher);

    /**
     * Return all the {@link AtlasItem}s containing some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link AtlasItem}s containing the {@link Location}.
     */
    Iterable<AtlasItem> itemsContaining(Location location);

    /**
     * Return all the {@link AtlasItem}s matching a {@link Predicate} and containing some
     * {@link Location}.
     *
     * @param matcher
     *            The matcher to consider
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link AtlasItem}s matching the {@link Predicate} and containing the
     *         {@link Location}.
     */
    Iterable<AtlasItem> itemsContaining(Location location, Predicate<AtlasItem> matcher);

    /**
     * Return all the {@link AtlasItem}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The {@link Polygon} to consider
     * @return All the {@link AtlasItem}s within and/or intersecting the {@link Polygon}.
     */
    Iterable<AtlasItem> itemsIntersecting(Polygon polygon);

    /**
     * Return all the {@link AtlasItem}s matching a {@link Predicate}, and within and/or
     * intersecting some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link AtlasItem}s matching the {@link Predicate}, and within and/or
     *         intersecting the polygon.
     */
    Iterable<AtlasItem> itemsIntersecting(Polygon polygon, Predicate<AtlasItem> matcher);

    /**
     * Return all the {@link AtlasItem}s fully within some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link AtlasItem}s fully within the polygon.
     */
    Iterable<AtlasItem> itemsWithin(Polygon polygon);

    /**
     * @param identifier
     *            The {@link Line}'s identifier
     * @return The {@link Line} that corresponds to the provided identifier
     */
    Line line(long identifier);

    /**
     * Return all the {@link LineItem}s
     *
     * @return All the {@link LineItem}s
     */
    Iterable<LineItem> lineItems();

    /**
     * Return all the {@link LineItem}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link LineItem}s matching the {@link Predicate}.
     */
    Iterable<LineItem> lineItems(Predicate<LineItem> matcher);

    /**
     * Return all the {@link LineItem}s containing some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link LineItem}s containing the {@link Location}.
     */
    Iterable<LineItem> lineItemsContaining(Location location);

    /**
     * Return all the {@link LineItem}s matching a {@link Predicate} and containing some
     * {@link Location}.
     *
     * @param matcher
     *            The matcher to consider
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link LineItem}s matching the {@link Predicate} and containing the
     *         {@link Location}.
     */
    Iterable<LineItem> lineItemsContaining(Location location, Predicate<LineItem> matcher);

    /**
     * Return all the {@link LineItem}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The {@link Polygon} to consider
     * @return All the {@link LineItem}s within and/or intersecting the {@link Polygon}.
     */
    Iterable<LineItem> lineItemsIntersecting(Polygon polygon);

    /**
     * Return all the {@link LineItem}s matching a {@link Predicate}, and within and/or intersecting
     * some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link LineItem}s matching the {@link Predicate}, and within and/or
     *         intersecting the polygon.
     */
    Iterable<LineItem> lineItemsIntersecting(Polygon polygon, Predicate<LineItem> matcher);

    /**
     * Return all the {@link LineItem}s fully within some polygon.
     *
     * @param polygon
     *            The {@link Polygon} to consider
     * @return All the {@link LineItem}s within and/or intersecting the {@link Polygon}.
     */
    Iterable<LineItem> lineItemsWithin(Polygon polygon);

    /**
     * @return All the {@link Line}s in this {@link Atlas}
     */
    Iterable<Line> lines();

    /**
     * Return all the {@link Line}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link Line}s matching the {@link Predicate}.
     */
    Iterable<Line> lines(Predicate<Line> matcher);

    /**
     * Return all the {@link Line}s containing some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Line}s containing the location.
     */
    Iterable<Line> linesContaining(Location location);

    /**
     * Return all the {@link Line}s matching a {@link Predicate} and containing some
     * {@link Location}.
     *
     * @param matcher
     *            The matcher to consider
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Line}s matching the {@link Predicate} and containing the
     *         {@link Location}.
     */
    Iterable<Line> linesContaining(Location location, Predicate<Line> matcher);

    /**
     * Return all the {@link Line}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Line}s within and/or intersecting the polygon.
     */
    Iterable<Line> linesIntersecting(Polygon polygon);

    /**
     * Return all the {@link Line}s matching a {@link Predicate}, and within and/or intersecting
     * some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Line}s matching the {@link Predicate}, and within and/or intersecting
     *         the polygon.
     */
    Iterable<Line> linesIntersecting(Polygon polygon, Predicate<Line> matcher);

    /**
     * Return all the {@link Line}s fully within some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Line}s fully within the polygon.
     */
    Iterable<Line> linesWithin(Polygon polygon);

    /**
     * Return all the {@link LocationItem}s
     *
     * @return All the {@link LocationItem}s
     */
    Iterable<LocationItem> locationItems();

    /**
     * Return all the {@link LocationItem}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link LocationItem}s matching the {@link Predicate}.
     */
    Iterable<LocationItem> locationItems(Predicate<LocationItem> matcher);

    /**
     * Return all the {@link LocationItem}s within and/or intersecting some polygon.
     *
     * @param polygon
     *            The {@link Polygon} to consider
     * @return All the {@link LocationItem}s within and/or intersecting the {@link Polygon}.
     */
    Iterable<LocationItem> locationItemsWithin(Polygon polygon);

    /**
     * Return all the {@link LocationItem}s matching a {@link Predicate}, and within and/or
     * intersecting some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link LocationItem}s matching the {@link Predicate}, and within and/or
     *         intersecting the polygon.
     */
    Iterable<LocationItem> locationItemsWithin(Polygon polygon, Predicate<LocationItem> matcher);

    /**
     * @return The meta data for this {@link Atlas}.
     */
    AtlasMetaData metaData();

    /**
     * @param identifier
     *            The {@link Node}'s identifier
     * @return The {@link Node} that corresponds to the provided identifier
     */
    Node node(long identifier);

    /**
     * @return All the {@link Node}s in this Atlas
     */
    Iterable<Node> nodes();

    /**
     * Return all the {@link Node}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link Node}s matching the {@link Predicate}.
     */
    Iterable<Node> nodes(Predicate<Node> matcher);

    /**
     * Return all the {@link Node}s at some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Node}s at the {@link Location}.
     */
    Iterable<Node> nodesAt(Location location);

    /**
     * Return all the {@link Node}s within and/or intersecting some polygon. Note: results may vary,
     * for an identical boundary, depending on the type, {@link Rectangle} or {@link Polygon} of the
     * input. This is due to an underlying dependency on the awt definition of insideness.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Node}s within and/or intersecting the polygon.
     */
    Iterable<Node> nodesWithin(Polygon polygon);

    /**
     * Return all the {@link Node}s matching a {@link Predicate}, and within and/or intersecting
     * some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Node}s matching the {@link Predicate}, and within and/or intersecting
     *         the polygon.
     */
    Iterable<Node> nodesWithin(Polygon polygon, Predicate<Node> matcher);

    /**
     * @return The number of {@link Area}s
     */
    long numberOfAreas();

    /**
     * @return The number of {@link Edge}s
     */
    long numberOfEdges();

    /**
     * @return The number of {@link Line}s
     */
    long numberOfLines();

    /**
     * @return The number of {@link Node}s
     */
    long numberOfNodes();

    /**
     * @return The number of {@link Point}s
     */
    long numberOfPoints();

    /**
     * @return The number of {@link Relation}s
     */
    long numberOfRelations();

    /**
     * @param identifier
     *            The {@link Point}'s identifier
     * @return The {@link Point} that corresponds to the provided identifier
     */
    Point point(long identifier);

    /**
     * @return All the {@link Point}s in this Atlas
     */
    Iterable<Point> points();

    /**
     * Return all the {@link Point}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link Point}s matching the {@link Predicate}.
     */
    Iterable<Point> points(Predicate<Point> matcher);

    /**
     * Return all the {@link Point}s at some {@link Location}.
     *
     * @param location
     *            The {@link Location} to consider
     * @return All the {@link Point}s at the {@link Location}.
     */
    Iterable<Point> pointsAt(Location location);

    /**
     * Return all the {@link Point}s within some polygon. Note: results may vary, for an identical
     * boundary, depending on the type, {@link Rectangle} or {@link Polygon} of the input. This is
     * due to an underlying dependency on the awt definition of insideness.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Point}s within the polygon.
     */
    Iterable<Point> pointsWithin(Polygon polygon);

    /**
     * Return all the {@link Point}s matching a {@link Predicate}. Note: results may vary, for an
     * identical boundary, depending on the type, {@link Rectangle} or {@link Polygon} of the input.
     * This is due to an underlying dependency on the awt definition of insideness.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Point}s matching the {@link Predicate}, and within and/or intersecting
     *         the polygon.
     */
    Iterable<Point> pointsWithin(Polygon polygon, Predicate<Point> matcher);

    /**
     * @param identifier
     *            The {@link Relation}'s identifier
     * @return The {@link Relation} that corresponds to the provided identifier
     */
    Relation relation(long identifier);

    /**
     * @return All the {@link Relation}s in this Atlas
     */
    Iterable<Relation> relations();

    /**
     * Return all the {@link Relation}s matching a {@link Predicate}.
     *
     * @param matcher
     *            The matcher to consider
     * @return All the {@link Relation}s matching the {@link Predicate}.
     */
    Iterable<Relation> relations(Predicate<Relation> matcher);

    /**
     * @return All the {@link Relation}s in this Atlas, with the lower order relations first. This
     *         means at any point, if a relation is returned by this {@link Iterable}, then all the
     *         relations that belong to this relation have already been returned.
     */
    Iterable<Relation> relationsLowerOrderFirst();

    /**
     * Return all the {@link Relation}s which have at least one feature intersecting some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Relation}s which have at least one feature intersecting the polygon.
     */
    Iterable<Relation> relationsWithEntitiesIntersecting(Polygon polygon);

    /**
     * Return all the {@link Relation}s which have at least one feature intersecting some polygon.
     *
     * @param matcher
     *            The matcher to consider
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Relation}s matching the {@link Predicate}, and which have at least one
     *         feature intersecting the polygon.
     */
    Iterable<Relation> relationsWithEntitiesIntersecting(Polygon polygon,
            Predicate<Relation> matcher);

    /**
     * Return all the {@link Relation}s which have all features within some polygon.
     *
     * @param polygon
     *            The polygon to consider
     * @return All the {@link Relation}s which have all features within the polygon.
     */
    Iterable<Relation> relationsWithEntitiesWithin(Polygon polygon);

    /**
     * Serialize this {@link Atlas} to a {@link WritableResource}
     *
     * @param writableResource
     *            The resource to write to
     */
    void save(WritableResource writableResource);

    /**
     * Save as GeoJSON
     *
     * @param resource
     *            The resource to write to
     */
    void saveAsGeoJson(WritableResource resource);

    /**
     * Save as GeoJSON with matcher
     *
     * @param resource
     *            The resource to write to
     * @param matcher
     *            The matcher to consider
     */
    void saveAsGeoJson(WritableResource resource, Predicate<AtlasEntity> matcher);

    /**
     * Save as line-delimited GeoJSON. This is one feature per line, with no wrapping
     * FeatureCollection.
     *
     * @param resource
     *            The resource to write to
     * @param jsonMutator
     *            The callback function that will let you change what is in the Feature's JSON.
     */
    void saveAsLineDelimitedGeoJsonFeatures(WritableResource resource,
            BiConsumer<AtlasEntity, JsonObject> jsonMutator);

    /**
     * Save as line-delimited GeoJSON with a matcher. This is one feature per line, with no wrapping
     * FeatureCollection.
     *
     * @param resource
     *            The resource to write to
     * @param matcher
     *            The matcher to consider
     * @param jsonMutator
     *            The callback function that will let you change what is in the Feature's JSON.
     */
    void saveAsLineDelimitedGeoJsonFeatures(WritableResource resource,
            Predicate<AtlasEntity> matcher, BiConsumer<AtlasEntity, JsonObject> jsonMutator);

    /**
     * Save as list of items
     *
     * @param resource
     *            The resource to write to
     */
    void saveAsList(WritableResource resource);

    /**
     * Save as a naive proto file
     *
     * @param resource
     *            The resource to write to
     */
    void saveAsProto(WritableResource resource);

    /**
     * Save as a text file
     *
     * @param resource
     *            The resource to write to
     */
    void saveAsText(WritableResource resource);

    /**
     * @return The size for this {@link Atlas}.
     */
    default AtlasSize size()
    {
        return new AtlasSize(numberOfEdges(), numberOfNodes(), numberOfAreas(), numberOfLines(),
                numberOfPoints(), numberOfRelations());
    }

    /**
     * @param point
     *            A {@link Location} to snap
     * @param threshold
     *            A {@link Distance} threshold to look for edges around the {@link Location}
     * @return The best snapped result, or null if there is no valid snap
     */
    SnappedEdge snapped(Location point, Distance threshold);

    /**
     * @param point
     *            A {@link Location} to snap
     * @param threshold
     *            A {@link Distance} threshold to look for edges around the {@link Location}
     * @return A {@link SortedSet} of all the candidate snaps. The set is empty if there are no
     *         candidates.
     */
    SortedSet<SnappedEdge> snaps(Location point, Distance threshold);

    /**
     * Return a sub-atlas from this Atlas.
     *
     * @param boundary
     *            The boundary within which the sub atlas will be built
     * @param cutType
     *            The type of cut to perform
     * @return An optional sub-atlas. The optional will be empty in case there is nothing in the
     *         {@link Polygon} after the cut was applied. Returning an empty atlas is not allowed.
     */
    Optional<Atlas> subAtlas(Polygon boundary, AtlasCutType cutType);

    /**
     * Return a sub-atlas from this Atlas.
     *
     * @param matcher
     *            The matcher to consider
     * @param cutType
     *            The type of cut to perform
     * @return An optional sub-atlas. The optional will be empty in case the matcher and cut-type
     *         return an empty atlas, which is not allowed.
     */
    Optional<Atlas> subAtlas(Predicate<AtlasEntity> matcher, AtlasCutType cutType);

    /**
     * @return A summary of this {@link Atlas}
     */
    String summary();
}
