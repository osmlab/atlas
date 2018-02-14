package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * {@link AtlasItem} that is in shape of a {@link PolyLine}
 *
 * @author matthieun
 * @author mgostintsev
 */
public abstract class LineItem extends AtlasItem
{
    private static final long serialVersionUID = -2053566750957119655L;

    protected LineItem(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * @return The {@link PolyLine} that represents this {@link LineItem}
     */
    public abstract PolyLine asPolyLine();

    @Override
    public Rectangle bounds()
    {
        return asPolyLine().bounds();
    }

    @Override
    public Iterable<Location> getRawGeometry()
    {
        return asPolyLine();
    }

    @Override
    public boolean intersects(final Polygon polygon)
    {
        return polygon.overlaps(asPolyLine());
    }

    /**
     * Check if this {@link LineItem} is closed. Closed is defined when the first {@link Location}
     * is the same as the last {@link Location}.
     *
     * @return {@code true} if it's closed.
     */
    public boolean isClosed()
    {
        final PolyLine polyLine = asPolyLine();
        return polyLine.first().equals(polyLine.last());
    }

    /**
     * @return flag to denote zero length line items
     */
    public boolean isZeroLength()
    {
        return length().equals(Distance.ZERO);
    }

    /**
     * @return The length of this item
     */
    public Distance length()
    {
        return asPolyLine().length();
    }

    /**
     * @return the number of shape-points for this item, including start and end points.
     */
    public int numberOfShapePoints()
    {
        return asPolyLine().size();
    }

    /**
     * @return The overall heading of the {@link PolyLine}: the heading between the start point and
     *         the end point.
     */
    public Optional<Heading> overallHeading()
    {
        return this.asPolyLine().overallHeading();
    }

    @Override
    public LocationIterableProperties toGeoJsonBuildingBlock()
    {
        final Map<String, String> tags = getTags();
        tags.put("identifier", String.valueOf(getIdentifier()));
        tags.put("osmIdentifier", String.valueOf(getOsmIdentifier()));
        tags.put("itemType", String.valueOf(getType()));

        if (this instanceof Edge)
        {
            tags.put("startNode", String.valueOf(((Edge) this).start().getIdentifier()));
            tags.put("endNode", String.valueOf(((Edge) this).end().getIdentifier()));
        }

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
