package org.openstreetmap.atlas.geography.converters.jts;

import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Transform a JTS {@link GeometryCollection} into a real {@link Iterable}.
 *
 * @author matthieun
 */
public final class GeometryStreamer
{
    /**
     * @param collection
     *            The collection to stream
     * @return The JTS {@link Geometry} {@link Iterable}
     */
    public static Iterable<Geometry> stream(final GeometryCollection collection)
    {
        final int size = collection.getNumGeometries();
        return () -> new Iterator<Geometry>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return this.index < size;
            }

            @Override
            public Geometry next()
            {
                return collection.getGeometryN(this.index++);
            }
        };
    }

    /**
     * @param collection
     *            The collection to stream
     * @return The JTS {@link Geometry} {@link Iterable} as JTS {@link Polygon}s
     */
    public static Iterable<Polygon> streamPolygons(final GeometryCollection collection)
    {
        return Iterables.translate(stream(collection), geometry -> (Polygon) geometry);
    }

    private GeometryStreamer()
    {
    }
}
