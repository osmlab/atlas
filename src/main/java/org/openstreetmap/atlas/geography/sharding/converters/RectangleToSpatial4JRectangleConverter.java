package org.openstreetmap.atlas.geography.sharding.converters;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * @author matthieun
 */
public class RectangleToSpatial4JRectangleConverter
        implements TwoWayConverter<org.locationtech.spatial4j.shape.Rectangle, Rectangle>
{
    @Override
    public org.locationtech.spatial4j.shape.Rectangle backwardConvert(final Rectangle other)
    {
        final Location lowerLeft = other.lowerLeft();
        final Location upperRight = other.upperRight();
        return new RectangleImpl(lowerLeft.getLongitude().asDegrees(),
                upperRight.getLongitude().asDegrees(), lowerLeft.getLatitude().asDegrees(),
                upperRight.getLatitude().asDegrees(), SpatialContext.GEO);
    }

    @Override
    public Rectangle convert(final org.locationtech.spatial4j.shape.Rectangle other)
    {
        return Rectangle.forCorners(
                new Location(Latitude.degrees(other.getMinY()), Longitude.degrees(other.getMinX())),
                new Location(Latitude.degrees(other.getMaxY()),
                        Longitude.degrees(other.getMaxX())));
    }
}
