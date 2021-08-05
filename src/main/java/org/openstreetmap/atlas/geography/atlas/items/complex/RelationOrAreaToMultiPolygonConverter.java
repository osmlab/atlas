package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * Take in a {@link Relation} or an {@link Area} and return the corresponding {@link MultiPolygon}
 * if any.
 *
 * @author matthieun
 * @author bbreithaupt
 */
public class RelationOrAreaToMultiPolygonConverter implements Converter<AtlasEntity, MultiPolygon>
{
    private final RelationToMultiPolygonMemberConverter outerConverter;
    private final RelationToMultiPolygonMemberConverter innerConverter;
    private final JtsMultiPolygonToMultiPolygonConverter multipolygonConverter;

    public RelationOrAreaToMultiPolygonConverter()
    {
        this(false);
    }

    public RelationOrAreaToMultiPolygonConverter(final boolean usePolygonizer)
    {
        this.outerConverter = new RelationToMultiPolygonMemberConverter(Ring.OUTER, usePolygonizer);
        this.innerConverter = new RelationToMultiPolygonMemberConverter(Ring.INNER, usePolygonizer);
        this.multipolygonConverter = new JtsMultiPolygonToMultiPolygonConverter();
    }

    @Override
    public MultiPolygon convert(final AtlasEntity entity)
    {
        if (entity instanceof Relation)
        {
            final Relation relation = (Relation) entity;
            if (relation.isGeometric())
            {
                // Loop through the relation members, extract the inners and outers, and create the
                // outline.
                final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
                for (final Polygon outer : this.outerConverter.convert(relation))
                {
                    outerToInners.put(outer, new ArrayList<>());
                }
                if (outerToInners.isEmpty())
                {
                    throw new CoreException("Unable to find outer polygon.");
                }
                final Map<Polygon, PreparedPolygon> preparedOuters = new HashMap<>();
                final JtsPolygonConverter converter = new JtsPolygonConverter();
                outerToInners.keySet().forEach(
                        outer -> preparedOuters.put(outer, (PreparedPolygon) PreparedGeometryFactory
                                .prepare(converter.convert(outer))));
                for (final Polygon inner : this.innerConverter.convert(relation))
                {
                    boolean added = false;
                    for (final Map.Entry<Polygon, PreparedPolygon> entry : preparedOuters
                            .entrySet())
                    {
                        final org.locationtech.jts.geom.Polygon inner2 = converter.convert(inner);
                        if (entry.getValue().containsProperly(inner2))
                        {
                            outerToInners.add(entry.getKey(), inner);
                            added = true;
                            break;
                        }
                    }
                    if (!added)
                    {
                        throw new CoreException(
                                "Malformed MultiPolygon: inner has no outer host: {}", inner);
                    }
                }
                return new MultiPolygon(outerToInners);
            }
            else
            {
                throw new CoreException("This is not a multipolygon relation");
            }
        }
        else if (entity instanceof Area)
        {
            return MultiPolygon.forPolygon(((Area) entity).asPolygon());
        }
        else
        {
            throw new CoreException("The outline is not an area nor a relation: {}", entity);
        }
    }
}
