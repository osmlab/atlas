package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Map;

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * An {@link AtlasItem} that is represented by one single location
 *
 * @author matthieun
 */
public abstract class LocationItem extends AtlasItem
{
    private static final long serialVersionUID = -2616559591051747286L;

    protected LocationItem(final Atlas atlas)
    {
        super(atlas);
    }

    @Override
    public Rectangle bounds()
    {
        return getLocation().bounds();
    }

    /**
     * @return The item's {@link Location}
     */
    public abstract Location getLocation();

    @Override
    public Iterable<Location> getRawGeometry()
    {
        return getLocation();
    }

    @Override
    public boolean intersects(final GeometricSurface surface)
    {
        return surface.fullyGeometricallyEncloses(getLocation());
    }

    public SnappedLocation snapTo(final Area other)
    {
        return this.getLocation().snapTo(other.asPolygon());
    }

    public SnappedLocation snapTo(final LineItem other)
    {
        return this.getLocation().snapTo(other.asPolyLine());
    }

    @Override
    public LocationIterableProperties toGeoJsonBuildingBlock()
    {
        final Map<String, String> tags = getTags();
        tags.put("identifier", String.valueOf(getIdentifier()));
        tags.put("osmIdentifier", String.valueOf(getOsmIdentifier()));
        tags.put("itemType", String.valueOf(getType()));

        if (this instanceof Node)
        {
            final StringList inEdges = new StringList();
            final StringList outEdges = new StringList();
            ((Node) this).inEdges()
                    .forEach(edge -> inEdges.add(edge != null ? edge.getIdentifier() : "null"));
            ((Node) this).outEdges()
                    .forEach(edge -> outEdges.add(edge != null ? edge.getIdentifier() : "null"));
            tags.put("inEdges", inEdges.join(", "));
            tags.put("outEdges", outEdges.join(", "));
        }

        final Location location = this.getLocation();
        tags.put("latitude", String.valueOf(location.getLatitude().asDm7()));
        tags.put("longitude", String.valueOf(location.getLongitude().asDm7()));

        final StringList parentRelations = new StringList();
        this.relations().forEach(relation ->
        {
            final RelationMember member = relation.members().get(getIdentifier(), getType());
            parentRelations.add(member.getRelationIdentifier() + "-" + member.getRole());
        });

        if (!parentRelations.isEmpty())
        {
            tags.put("parentRelations", parentRelations.join(", "));
        }

        return new GeoJsonBuilder.LocationIterableProperties(getRawGeometry(), tags);
    }
}
