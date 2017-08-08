package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.ArrayList;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * Take in a {@link Relation} or an {@link Area} and return the corresponding {@link MultiPolygon}
 * if any.
 *
 * @author matthieun
 */
public class RelationOrAreaToMultiPolygonConverter implements Converter<AtlasEntity, MultiPolygon>
{
    private static final RelationToMultiPolygonMemberConverter OUTER_CONVERTER = new RelationToMultiPolygonMemberConverter(
            Ring.OUTER);
    private static final RelationToMultiPolygonMemberConverter INNER_CONVERTER = new RelationToMultiPolygonMemberConverter(
            Ring.INNER);

    @Override
    public MultiPolygon convert(final AtlasEntity entity)
    {
        if (entity instanceof Relation)
        {
            final Relation relation = (Relation) entity;
            if (relation.isMultiPolygon())
            {
                // Loop through the relation members, extract the inners and outers, and create the
                // outline.
                final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
                for (final Polygon outer : OUTER_CONVERTER.convert(relation))
                {
                    outerToInners.put(outer, new ArrayList<>());
                }
                if (outerToInners.isEmpty())
                {
                    throw new CoreException("Unable to find outer polygon.");
                }
                for (final Polygon inner : INNER_CONVERTER.convert(relation))
                {
                    boolean added = false;
                    for (final Polygon outer : outerToInners.keySet())
                    {
                        if (outer.overlaps(inner))
                        {
                            outerToInners.add(outer, inner);
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
