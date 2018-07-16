package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Map;

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Area from an {@link Atlas}
 *
 * @author matthieun
 */
public abstract class Area extends AtlasItem
{
    private static final long serialVersionUID = 5244165133018408045L;

    protected Area(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * @return The {@link PolyLine} that represents this {@link LineItem}
     */
    public abstract Polygon asPolygon();

    @Override
    public Rectangle bounds()
    {
        return asPolygon().bounds();
    }

    /**
     * @return The closed {@link Polygon}, with the end {@link Location} equal to the start
     *         {@link Location}.
     */
    public Polygon getClosedGeometry()
    {
        return new Polygon(asPolygon().closedLoop());
    }

    /**
     * @return The underlying {@link PolyLine}, which will not have the end {@link Location} equal
     *         to the start {@link Location}. i.e. the area will not be closed.
     */
    @Override
    public Iterable<Location> getRawGeometry()
    {
        return asPolygon();
    }

    @Override
    public ItemType getType()
    {
        return ItemType.AREA;
    }

    @Override
    public boolean intersects(final GeometricSurface surface)
    {
        return surface.overlaps(asPolygon());
    }

    @Override
    public String toDiffViewFriendlyString()
    {
        final String relationsString = this.parentRelationsAsDiffViewFriendlyString();

        return "[Area: id=" + this.getIdentifier() + ", polygon=" + this.asPolygon()
                + ", relations=(" + relationsString + "), " + tagString() + "]";
    }

    @Override
    public LocationIterableProperties toGeoJsonBuildingBlock()
    {
        final Map<String, String> tags = getTags();
        tags.put("identifier", String.valueOf(getIdentifier()));
        tags.put("osmIdentifier", String.valueOf(getOsmIdentifier()));
        tags.put("itemType", String.valueOf(getType()));

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

        return new GeoJsonBuilder.LocationIterableProperties(getClosedGeometry(), tags);
    }

    @Override
    public String toString()
    {
        return "[Area: id=" + this.getIdentifier() + ", polygon=" + this.asPolygon() + ", "
                + tagString() + "]";
    }
}
