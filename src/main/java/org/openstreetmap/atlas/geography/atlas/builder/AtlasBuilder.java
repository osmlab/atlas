package org.openstreetmap.atlas.geography.atlas.builder;

import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Build an {@link Atlas} from {@link Node} and {@link Edge} data.
 *
 * @author matthieun
 */
public interface AtlasBuilder
{
    /**
     * Add a {@link Area} to the {@link Atlas}.
     *
     * @param identifier
     *            The {@link Area}'s identifier.
     * @param geometry
     *            The geometry of the {@link Area}
     * @param tags
     *            An arbitrary set of OSM key-value pairs that are attached to this {@link Area}
     * @throws IllegalAccessError
     *             In case the {@link Atlas} has already been generated and this builder is locked.
     */
    void addArea(long identifier, Polygon geometry, Map<String, String> tags);

    /**
     * Add an {@link Edge} to the {@link Atlas}. Its start and end {@link Node} should have been
     * added already when this is called.
     *
     * @param identifier
     *            The {@link Edge}'s identifier.
     * @param geometry
     *            The geometry of the {@link Edge}
     * @param tags
     *            An arbitrary set of OSM key-value pairs that are attached to this {@link Edge}
     * @throws IllegalAccessError
     *             In case the {@link Atlas} has already been generated and this builder is locked.
     */
    void addEdge(long identifier, PolyLine geometry, Map<String, String> tags);

    /**
     * Add a {@link Line} to the {@link Atlas}.
     *
     * @param identifier
     *            The {@link Line}'s identifier.
     * @param geometry
     *            The geometry of the {@link Line}
     * @param tags
     *            An arbitrary set of OSM key-value pairs that are attached to this {@link Line}
     * @throws IllegalAccessError
     *             In case the {@link Atlas} has already been generated and this builder is locked.
     */
    void addLine(long identifier, PolyLine geometry, Map<String, String> tags);

    /**
     * Add a {@link Node} to the {@link Atlas}
     *
     * @param identifier
     *            The {@link Node}'s identifier
     * @param geometry
     *            The {@link Node}'s {@link Location}
     * @param tags
     *            An arbitrary set of OSM key-value pairs that are attached to this {@link Node}
     * @throws IllegalAccessError
     *             In case the {@link Atlas} has already been generated and this builder is locked.
     */
    void addNode(long identifier, Location geometry, Map<String, String> tags);

    /**
     * Add a {@link Point} to the {@link Atlas}.
     *
     * @param identifier
     *            The {@link Point}'s identifier.
     * @param geometry
     *            The geometry of the {@link Point}
     * @param tags
     *            An arbitrary set of OSM key-value pairs that are attached to this {@link Point}
     * @throws IllegalAccessError
     *             In case the {@link Atlas} has already been generated and this builder is locked.
     */
    void addPoint(long identifier, Location geometry, Map<String, String> tags);

    /**
     * Add a {@link Relation} to the {@link Atlas}.
     *
     * @param identifier
     *            The {@link Relation}'s identifier.
     * @param osmIdentifier
     *            The {@link Relation}'s OSM identifier for split relations. The same identifier
     *            otherwise.
     * @param structure
     *            The structure of the {@link Relation}. This cannot be empty!
     * @param tags
     *            An arbitrary set of OSM key-value pairs that are attached to this {@link Relation}
     * @throws IllegalAccessError
     *             In case the {@link Atlas} has already been generated and this builder is locked.
     */
    void addRelation(long identifier, long osmIdentifier, RelationBean structure,
            Map<String, String> tags);

    /**
     * @return The {@link Atlas} comprising all the {@link Edge}s, {@link Node}s, {@link Area}s,
     *         {@link Line}s, and {@link Point}s added using this builder. Once this is called, the
     *         addEdge or addNode methods should throw an exception.
     */
    Atlas get();

    /**
     * Give the meta data of the {@link Atlas} to be created.
     *
     * @param metaData
     *            The meta data
     */
    void setMetaData(AtlasMetaData metaData);

    /**
     * Give an estimate of the size of the Atlas.
     *
     * @param estimates
     *            The estimates of the size of the Atlas
     */
    void setSizeEstimates(AtlasSize estimates);
}
