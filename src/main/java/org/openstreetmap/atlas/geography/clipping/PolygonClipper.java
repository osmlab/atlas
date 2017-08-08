package org.openstreetmap.atlas.geography.clipping;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

/**
 * Clip {@link Polygon}s using the JTS library
 *
 * @author matthieun
 */
public class PolygonClipper
{
    private final Geometry jtsClipping;

    public static Geometry getJts(final PolyLine polyLine)
    {
        if (polyLine instanceof Polygon)
        {
            return new JtsPolygonConverter().convert((Polygon) polyLine);
        }
        return new JtsPolyLineConverter().convert(polyLine);
    }

    /**
     * Construct
     *
     * @param clipping
     *            The clipping {@link Polygon}
     */
    public PolygonClipper(final Polygon clipping)
    {
        this.jtsClipping = getJts(clipping);
    }

    public List<? extends PolyLine> and(final PolyLine subject)
    {
        return processResult(getJts(subject).intersection(this.jtsClipping));
    }

    public List<? extends PolyLine> not(final PolyLine subject)
    {
        return processResult(getJts(subject).difference(this.jtsClipping));
    }

    public List<? extends PolyLine> union(final PolyLine subject)
    {
        return processResult(getJts(subject).union(this.jtsClipping));
    }

    public List<? extends PolyLine> xor(final PolyLine subject)
    {
        return processResult(getJts(subject).symDifference(this.jtsClipping));
    }

    private List<? extends PolyLine> processResult(final Geometry intersections)
    {
        final List<PolyLine> result = new ArrayList<>();
        if (intersections instanceof GeometryCollection)
        {
            final GeometryCollection collection = (GeometryCollection) intersections;
            final int numGeometries = collection.getNumGeometries();
            for (int n = 0; n < numGeometries; n++)
            {
                final Geometry geometry = collection.getGeometryN(n);
                result.addAll(processResult(geometry));
            }
        }
        else if (intersections instanceof com.vividsolutions.jts.geom.Polygon)
        {
            result.add(new JtsPolygonConverter()
                    .backwardConvert((com.vividsolutions.jts.geom.Polygon) intersections));
        }
        else if (intersections instanceof LineString)
        {
            result.add(new JtsPolyLineConverter().backwardConvert((LineString) intersections));
        }
        return result;
    }
}
