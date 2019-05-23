package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * @author matthieun
 * @param <T>
 *            The type to convert to: {@link String} or byte[]
 */
public abstract class WkMultiPolygonConverter<T> implements TwoWayConverter<MultiPolygon, T>
{
    private static final JtsPolygonToMultiPolygonConverter POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsPolygonToMultiPolygonConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

    @Override
    public MultiPolygon backwardConvert(final T wkt)
    {
        try
        {
            final Geometry result = getGeometryConverter().convert(wkt);
            if (result instanceof org.locationtech.jts.geom.Polygon)
            {
                final org.locationtech.jts.geom.Polygon jtsPolygon = (org.locationtech.jts.geom.Polygon) result;
                return POLYGON_TO_MULTI_POLYGON_CONVERTER.convert(jtsPolygon);
            }
            else if (result instanceof org.locationtech.jts.geom.MultiPolygon)
            {
                return MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                        .convert((org.locationtech.jts.geom.MultiPolygon) result);
            }
            else
            {
                throw new CoreException("Unknown type: {}", result.getClass().getCanonicalName());
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt, e);
        }
    }

    @Override
    public T convert(final MultiPolygon multiPolygon)
    {
        final org.locationtech.jts.geom.MultiPolygon geometry = MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                .backwardConvert(multiPolygon);
        return getGeometryConverter().backwardConvert(geometry);
    }

    abstract TwoWayConverter<T, Geometry> getGeometryConverter();
}
