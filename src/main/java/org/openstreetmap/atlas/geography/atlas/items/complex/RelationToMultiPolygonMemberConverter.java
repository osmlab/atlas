package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * Get the {@link MultiPolygon} rings of a certain type from a {@link Relation} of type
 * multipolygon.
 *
 * @author matthieun
 */
public class RelationToMultiPolygonMemberConverter implements Converter<Relation, Iterable<Polygon>>
{
    private static final MultiplePolyLineToPolygonsConverter MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER = new MultiplePolyLineToPolygonsConverter();

    private final Ring ring;

    public RelationToMultiPolygonMemberConverter(final Ring ring)
    {
        this.ring = ring;
    }

    @Override
    public Iterable<Polygon> convert(final Relation relation)
    {
        final List<PolyLine> candidates = new ArrayList<>();
        final List<Polygon> alreadyFormed = new ArrayList<>();
        if (!relation.isMultiPolygon())
        {
            throw new CoreException("Not a MultiPolygon: {}", relation);
        }
        final ArrayList<RelationMember> members = new ArrayList<>();
        relation.members().iterator().forEachRemaining(members::add);
        Collections.sort(members);
        for (final RelationMember member : members)
        {
            final AtlasEntity entity = member.getEntity();
            switch (this.ring)
            {
                case OUTER:
                    if (RelationTypeTag.MULTIPOLYGON_ROLE_OUTER.equals(member.getRole()))
                    {
                        processEntity(entity, candidates, alreadyFormed);
                    }
                    break;
                case INNER:
                    if (RelationTypeTag.MULTIPOLYGON_ROLE_INNER.equals(member.getRole()))
                    {
                        processEntity(entity, candidates, alreadyFormed);
                    }
                    break;
                default:
                    throw new CoreException("Unknown ring type: {}", this.ring);
            }
        }
        return new MultiIterable<>(alreadyFormed,
                MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER.convert(candidates));
    }

    private void processEntity(final AtlasEntity entity, final List<PolyLine> candidates,
            final List<Polygon> alreadyFormed)
    {
        if (entity instanceof Area)
        {
            // Easy
            alreadyFormed.add(((Area) entity).asPolygon());
        }
        else if (entity instanceof LineItem)
        {
            // In case an Edge is an outer/inner, make sure to not double count it by looking at the
            // master edge only.
            if (!(entity instanceof Edge) || ((Edge) entity).isMasterEdge())
            {
                candidates.add(((LineItem) entity).asPolyLine());
            }
        }
    }
}
