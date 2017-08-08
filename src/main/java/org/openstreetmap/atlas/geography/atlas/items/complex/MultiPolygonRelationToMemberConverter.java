package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * @author Sid
 */
public class MultiPolygonRelationToMemberConverter
        implements Converter<Relation, Iterable<AtlasEntity>>
{
    /**
     * @author Sid
     */
    public enum Ring
    {
        OUTER,
        INNER
    }

    private final Ring ring;

    public MultiPolygonRelationToMemberConverter(final Ring ring)
    {
        this.ring = ring;
    }

    @Override
    public Iterable<AtlasEntity> convert(final Relation relation)
    {
        if (!relation.isMultiPolygon())
        {
            throw new CoreException("Not a MultiPolygon: {}", relation);
        }

        final List<AtlasEntity> list = new ArrayList<>();
        for (final RelationMember member : relation.members())
        {
            switch (this.ring)
            {
                case OUTER:
                    if (RelationTypeTag.MULTIPOLYGON_ROLE_OUTER.equals(member.getRole()))
                    {
                        list.add(member.getEntity());
                    }
                    break;
                case INNER:
                    if (RelationTypeTag.MULTIPOLYGON_ROLE_INNER.equals(member.getRole()))
                    {
                        list.add(member.getEntity());
                    }
                    break;
                default:
                    throw new CoreException("Unknown ring type: {}", this.ring);
            }
        }
        return list;
    }
}
