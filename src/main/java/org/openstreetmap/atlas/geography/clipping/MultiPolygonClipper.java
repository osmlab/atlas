package org.openstreetmap.atlas.geography.clipping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * Wrapper around the JTS library for {@link Polygon} and {@link PolyLine} clipping with
 * {@link Polygon}s and {@link MultiPolygon}s.
 *
 * @author matthieun
 */
public class MultiPolygonClipper
{
    private final Set<org.locationtech.jts.geom.Polygon> jtsClippings;

    protected MultiPolygonClipper(final MultiPolygon clipping)
    {
        this.jtsClippings = new JtsMultiPolygonConverter().convert(clipping);
    }

    protected MultiPolygon and(final MultiPolygon subject)
    {
        return runMultiPolygonClipping(subject, (sub, clipping) -> sub.intersection(clipping));
    }

    protected MultiPolygon and(final Polygon subject)
    {
        return runPolygonClipping(subject, (sub, clipping) -> sub.intersection(clipping));
    }

    protected List<PolyLine> and(final PolyLine subject)
    {
        return runPolyLineClipping(subject, (sub, clipping) -> sub.intersection(clipping));
    }

    protected MultiPolygon not(final MultiPolygon subject)
    {
        return runMultiPolygonClipping(subject, (sub, clipping) -> sub.difference(clipping));
    }

    protected MultiPolygon not(final Polygon subject)
    {
        return runPolygonClipping(subject, (sub, clipping) -> sub.difference(clipping));
    }

    protected List<PolyLine> not(final PolyLine subject)
    {
        return runPolyLineClipping(subject, (sub, clipping) -> sub.difference(clipping));
    }

    protected MultiPolygon union(final MultiPolygon subject)
    {
        return runMultiPolygonClipping(subject, (sub, clipping) -> sub.union(clipping));
    }

    protected MultiPolygon union(final Polygon subject)
    {
        return runPolygonClipping(subject, (sub, clipping) -> sub.union(clipping));
    }

    protected List<PolyLine> union(final PolyLine subject)
    {
        return runPolyLineClipping(subject, (sub, clipping) -> sub.union(clipping));
    }

    protected MultiPolygon xor(final MultiPolygon subject)
    {
        return runMultiPolygonClipping(subject, (sub, clipping) -> sub.symDifference(clipping));
    }

    protected MultiPolygon xor(final Polygon subject)
    {
        return runPolygonClipping(subject, (sub, clipping) -> sub.symDifference(clipping));
    }

    protected List<PolyLine> xor(final PolyLine subject)
    {
        return runPolyLineClipping(subject, (sub, clipping) -> sub.symDifference(clipping));
    }

    private MultiPolygon processMultiPolygon(final Geometry intersections)
    {
        MultiPolygon result = new MultiPolygon(new MultiMap<>());
        if (intersections instanceof GeometryCollection)
        {
            final GeometryCollection collection = (GeometryCollection) intersections;
            final int numGeometries = collection.getNumGeometries();
            for (int n = 0; n < numGeometries; n++)
            {
                final Geometry geometry = collection.getGeometryN(n);
                result = result.merge(processMultiPolygon(geometry));
            }
        }
        else if (intersections instanceof org.locationtech.jts.geom.Polygon)
        {
            final Set<org.locationtech.jts.geom.Polygon> set = new HashSet<>();
            set.add((org.locationtech.jts.geom.Polygon) intersections);
            result = result.merge(new JtsMultiPolygonConverter().backwardConvert(set));
        }
        return result;
    }

    private List<PolyLine> processPolyLine(final Geometry intersections)
    {
        final List<PolyLine> result = new ArrayList<>();
        if (intersections instanceof GeometryCollection)
        {
            final GeometryCollection collection = (GeometryCollection) intersections;
            final int numGeometries = collection.getNumGeometries();
            for (int n = 0; n < numGeometries; n++)
            {
                final Geometry geometry = collection.getGeometryN(n);
                result.addAll(processPolyLine(geometry));
            }
        }
        else if (intersections instanceof LineString)
        {
            result.add(new JtsPolyLineConverter().backwardConvert((LineString) intersections));
        }
        return result;
    }

    private MultiPolygon runMultiPolygonClipping(final MultiPolygon subject,
            final BiFunction<Geometry, Geometry, Geometry> application)
    {
        MultiPolygon result = new MultiPolygon(new MultiMap<>());
        final Set<org.locationtech.jts.geom.Polygon> jtsSubjects = new JtsMultiPolygonConverter()
                .convert(subject);
        for (final org.locationtech.jts.geom.Polygon jtsClipping : this.jtsClippings)
        {
            for (final org.locationtech.jts.geom.Polygon jtsSubject : jtsSubjects)
            {
                result = result
                        .merge(processMultiPolygon(application.apply(jtsSubject, jtsClipping)));
            }
        }
        return result;
    }

    private List<PolyLine> runPolyLineClipping(final PolyLine subject,
            final BiFunction<Geometry, Geometry, Geometry> application)
    {
        final List<PolyLine> result = new ArrayList<>();
        for (final org.locationtech.jts.geom.Polygon jtsClipping : this.jtsClippings)
        {
            result.addAll(processPolyLine(
                    application.apply(PolygonClipper.getJts(subject), jtsClipping)));
        }
        return result;
    }

    private MultiPolygon runPolygonClipping(final Polygon subject,
            final BiFunction<Geometry, Geometry, Geometry> application)
    {
        MultiPolygon result = new MultiPolygon(new MultiMap<>());
        for (final org.locationtech.jts.geom.Polygon jtsClipping : this.jtsClippings)
        {
            result = result.merge(processMultiPolygon(
                    application.apply(PolygonClipper.getJts(subject), jtsClipping)));
        }
        return result;
    }
}
