package org.openstreetmap.atlas.geography.clipping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Wrapper around JTS for geometry operations.
 *
 * @author matthieun
 */
public final class GeometryOperation
{
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final JtsMultiPolygonConverter JTS_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonConverter();
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();

    public static Optional<GeometricSurface> intersection(final Iterable<Polygon> polygons)
    {
        final List<Geometry> toIntersect = new ArrayList<>();
        for (final Polygon polygon : polygons)
        {
            toIntersect.add(JTS_POLYGON_CONVERTER.convert(polygon));
        }
        try
        {
            return intersection(toIntersect);
        }
        catch (final Exception e)
        {
            throw new CoreException("Error computing intersection of {}!",
                    Iterables.asList(polygons), e);
        }
    }

    public static Optional<GeometricSurface> union(final Iterable<MultiPolygon> multiPolygons)
    {
        final List<Geometry> toUnion = new ArrayList<>();
        for (final MultiPolygon multiPolygon : multiPolygons)
        {
            toUnion.add(JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER.backwardConvert(multiPolygon));
        }
        try
        {
            return union(toUnion);
        }
        catch (final Exception e)
        {
            throw new CoreException("Error computing union of {}!", Iterables.asList(multiPolygons),
                    e);
        }
    }

    public static Optional<GeometricSurface> union(final MultiPolygon... multiPolygons)
    {
        return union(Iterables.asList(multiPolygons));
    }

    private static Optional<GeometricSurface> handleMultiPolygon(
            final org.locationtech.jts.geom.MultiPolygon result)
    {
        return Optional.of(JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER.convert(result));
    }

    private static Optional<GeometricSurface> handlePolygon(
            final org.locationtech.jts.geom.Polygon result)
    {
        final Set<org.locationtech.jts.geom.Polygon> resultSet = new HashSet<>();
        resultSet.add(result);
        final MultiPolygon resultMultiPolygon = JTS_MULTI_POLYGON_CONVERTER
                .backwardConvert(resultSet);
        if (resultMultiPolygon.inners().isEmpty() && resultMultiPolygon.outers().size() == 1)
        {
            return Optional.of(resultMultiPolygon.outers().iterator().next());
        }
        else
        {
            return Optional.of(resultMultiPolygon);
        }
    }

    private static Optional<GeometricSurface> handlePolygonOrMultipolygon(final Geometry result)
    {
        if (result.isEmpty())
        {
            return Optional.empty();
        }
        else if (result instanceof org.locationtech.jts.geom.MultiPolygon)
        {
            return handleMultiPolygon((org.locationtech.jts.geom.MultiPolygon) result);
        }
        else if (result instanceof org.locationtech.jts.geom.Polygon)
        {
            return handlePolygon((org.locationtech.jts.geom.Polygon) result);
        }
        else
        {
            throw new CoreException("Result is neither empty, MultiPolygon or Polygon");
        }
    }

    private static Optional<GeometricSurface> intersection(final List<Geometry> toIntersect)
    {
        Geometry result = null;
        for (final Geometry geometry : toIntersect)
        {
            if (result == null)
            {
                result = geometry;
            }
            else
            {
                result = result.intersection(geometry);
            }
        }
        if (result == null || result.isEmpty())
        {
            return Optional.empty();
        }
        else
        {
            return handlePolygonOrMultipolygon(result);
        }
    }

    private static Optional<GeometricSurface> union(final List<Geometry> toUnion)
    {
        final Geometry result = UnaryUnionOp.union(toUnion);
        return handlePolygonOrMultipolygon(result);
    }

    private GeometryOperation()
    {
    }
}
