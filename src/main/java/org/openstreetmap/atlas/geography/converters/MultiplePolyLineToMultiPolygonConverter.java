package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * From a map of {@link PolyLine}s for {@link Ring} types, try to stitch all the {@link PolyLine}s
 * together to form a single {@link MultiPolygon}.
 *
 * @author samg
 **/
public class MultiplePolyLineToMultiPolygonConverter
        implements Converter<Map<Ring, Iterable<PolyLine>>, MultiPolygon>
{

    private static final MultiplePolyLineToPolygonsConverter MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER = new MultiplePolyLineToPolygonsConverter();

    private static List<Polygon> buildOuters(final Iterable<PolyLine> outers)
    {
        final List<Polygon> outerPolygons = new ArrayList<>();
        Iterables.stream(outers).filter(line -> line instanceof Polygon)
                .forEach(polygon -> outerPolygons.add((Polygon) polygon));
        final List<PolyLine> outerPolyLines = new ArrayList<>();
        Iterables.stream(outers).filter(line -> !(line instanceof Polygon))
                .forEach(outerPolyLines::add);
        MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER.convert(outerPolyLines)
                .forEach(outerPolygons::add);
        return outerPolygons;
    }

    private static MultiMap<Polygon, Polygon> buildOutersToInnersMap(final List<Polygon> outers,
            final Iterable<PolyLine> inners)
    {
        final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
        outers.forEach(outer -> outersToInners.put(outer, new ArrayList<>()));
        final List<Polygon> innerPolygons = new ArrayList<>();
        final List<PolyLine> innerPolyLines = new ArrayList<>();
        Iterables.stream(inners).filter(line -> line instanceof Polygon)
                .forEach(polygon -> innerPolygons.add((Polygon) polygon));
        Iterables.stream(inners).filter(line -> !(line instanceof Polygon))
                .forEach(innerPolyLines::add);
        MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER.convert(innerPolyLines)
                .forEach(innerPolygons::add);
        final JtsPolygonConverter converter = new JtsPolygonConverter();
        final Map<Polygon, PreparedPolygon> preparedOuters = new HashMap<>();
        outersToInners.keySet().forEach(outer -> preparedOuters.put(outer,
                (PreparedPolygon) PreparedGeometryFactory.prepare(converter.convert(outer))));
        innerPolygons.forEach(inner ->
        {
            boolean added = false;
            final org.locationtech.jts.geom.Polygon inner2 = converter.convert(inner);
            for (final Map.Entry<Polygon, PreparedPolygon> entry : preparedOuters.entrySet())
            {
                if (entry.getValue().containsProperly(inner2))
                {
                    outersToInners.add(entry.getKey(), inner);
                    added = true;
                    break;
                }
            }
            if (!added)
            {
                throw new CoreException("Malformed MultiPolygon: inner has no outer host: {}",
                        inner);
            }
        });
        return outersToInners;
    }

    @Override
    public MultiPolygon convert(final Map<Ring, Iterable<PolyLine>> outersAndInners)
    {
        final List<Polygon> outers = buildOuters(outersAndInners.get(Ring.OUTER));
        if (outers.isEmpty())
        {
            throw new CoreException("Unable to find outer polygon.");
        }
        final MultiMap<Polygon, Polygon> outersToInners = buildOutersToInnersMap(outers,
                outersAndInners.get(Ring.INNER));
        return new MultiPolygon(outersToInners);
    }
}
