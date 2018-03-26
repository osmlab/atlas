package org.openstreetmap.atlas.geography.atlas.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * An OSM relation
 *
 * @author matthieun
 * @author Sid
 */
public abstract class Relation extends AtlasEntity implements Iterable<RelationMember>
{
    /**
     * The ring type of a {@link MultiPolygon} member.
     *
     * @author matthieun
     */
    public enum Ring
    {
        OUTER,
        INNER
    }

    private static final long serialVersionUID = -9013894610780915685L;

    public static final Comparator<Relation> RELATION_ID_COMPARATOR = (final Relation relation1,
            final Relation relation2) -> Long.compare(relation1.getIdentifier(),
                    relation2.getIdentifier());

    protected Relation(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * @return All the members of this relation's OSM ancestor. If this relation has not been
     *         sliced, then this will return the same as <code>members()</code>. If this relation is
     *         sliced, and is part of a pool of other relations that belong to the same OSM
     *         ancestor, this method will poll together all the members of all those relations in
     *         its Atlas.
     */
    public abstract RelationMemberList allKnownOsmMembers();

    public abstract List<Relation> allRelationsWithSameOsmIdentifier();

    @Override
    public Rectangle bounds()
    {
        return boundsInternal(new LinkedHashSet<>());
    }

    public String configurableString(final String betweenEachMemberAndRelation,
            final String betweenEachMember)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Relation: id=");
        builder.append(getIdentifier());
        builder.append(", [Members: \n\t\t\t\t");
        final StringList list = new StringList();
        for (final RelationMember member : this)
        {
            list.add(betweenEachMemberAndRelation + betweenEachMember + member.toString());
        }
        builder.append(list.join(", \n\t\t\t\t"));
        builder.append("\n\t\t\t");
        builder.append(betweenEachMemberAndRelation);
        builder.append("], ");
        builder.append(tagString());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public ItemType getType()
    {
        return ItemType.RELATION;
    }

    public boolean hasMultiPolygonMembers(final Ring ring)
    {
        if (isMultiPolygon())
        {
            for (final RelationMember member : members())
            {
                switch (ring)
                {
                    case OUTER:
                        if (RelationTypeTag.MULTIPOLYGON_ROLE_OUTER.equals(member.getRole()))
                        {
                            return true;
                        }
                        break;
                    case INNER:
                        if (RelationTypeTag.MULTIPOLYGON_ROLE_INNER.equals(member.getRole()))
                        {
                            return true;
                        }
                        break;
                    default:
                        throw new CoreException("Unknown ring type: {}", ring);
                }
            }
        }
        return false;
    }

    @Override
    public boolean intersects(final GeometricSurface surface)
    {
        return intersectsInternal(surface, new LinkedHashSet<>());
    }

    public boolean isMultiPolygon()
    {
        return Validators.isOfType(this, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON,
                RelationTypeTag.BOUNDARY);
    }

    @Override
    public Iterator<RelationMember> iterator()
    {
        return members().iterator();
    }

    /**
     * @return All the members of this specific (potentially sliced) relation.
     */
    public abstract RelationMemberList members();

    /**
     * In case a {@link Relation} is spanning multiple {@link Atlas}, keep track of the parent OSM
     * relation identifier to be able to match it back to other sliced relations.
     *
     * @return The OSM identifier
     */
    public abstract long osmRelationIdentifier();

    @Override
    public LocationIterableProperties toGeoJsonBuildingBlock()
    {
        final Map<String, String> tags = getTags();
        tags.put("identifier", String.valueOf(getIdentifier()));
        tags.put("osmIdentifier", String.valueOf(getOsmIdentifier()));
        tags.put("itemType", String.valueOf(getType()));
        tags.put("relation", this.toSimpleString());

        return new GeoJsonBuilder.LocationIterableProperties(Location.CENTER, tags);
    }

    public String toSimpleString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Relation: id=");
        builder.append(getIdentifier());
        builder.append(", [Members: ");
        final StringList list = new StringList();
        for (final RelationMember member : this)
        {
            list.add(member.toString());
        }
        builder.append(list.join(", "));
        builder.append("], ");
        builder.append(tagString());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String toString()
    {
        return configurableString("", "");
    }

    /**
     * Avoid stack overflows in case a relation has looping members. This should never happen with a
     * {@link PackedAtlas} but could happen when two {@link Atlas} are combined into a
     * {@link MultiAtlas}.
     *
     * @param parentRelationIdentifiers
     *            The identifiers of the parent relations that have already been visited.
     * @return The bounds
     */
    protected Rectangle boundsInternal(final Set<Long> parentRelationIdentifiers)
    {
        if (this.members().isEmpty())
        {
            return Rectangle.MINIMUM;
        }
        final List<Located> itemsToConsider = new ArrayList<>();
        for (final AtlasEntity member : Iterables.stream(this).map(RelationMember::getEntity)
                .filter(Objects::nonNull))
        {
            if (member instanceof Relation)
            {
                final long identifier = member.getIdentifier();
                if (parentRelationIdentifiers.contains(identifier))
                {
                    continue;
                }
                else
                {
                    parentRelationIdentifiers.add(identifier);
                    itemsToConsider
                            .add(((Relation) member).boundsInternal(parentRelationIdentifiers));
                }
            }
            else
            {
                final Rectangle bounds = member.bounds();
                if (bounds != null)
                {
                    itemsToConsider.add(bounds);
                }
            }
        }
        if (Iterables.size(itemsToConsider) == 0)
        {
            return Rectangle.MINIMUM;
        }
        return Rectangle.forLocated(itemsToConsider);
    }

    /**
     * Avoid stack overflows in case a relation has looping members. This should never happen with a
     * {@link PackedAtlas} but could happen when two {@link Atlas} are combined into a
     * {@link MultiAtlas}.
     *
     * @param surface
     *            The {@link GeometricSurface} to check for
     * @param parentRelationIdentifiers
     *            The identifiers of the parent relations that have already been visited.
     * @return True if the relation intersects the geometricSurface
     */
    protected boolean intersectsInternal(final GeometricSurface surface,
            final Set<Long> parentRelationIdentifiers)
    {
        for (final RelationMember member : this)
        {
            final AtlasEntity entity = member.getEntity();
            if (entity instanceof Relation)
            {
                final long identifier = entity.getIdentifier();
                if (parentRelationIdentifiers.contains(identifier))
                {
                    continue;
                }
                else
                {
                    parentRelationIdentifiers.add(identifier);
                    if (((Relation) entity).intersectsInternal(surface, parentRelationIdentifiers))
                    {
                        return true;
                    }
                }
            }
            else if (entity.intersects(surface))
            {
                return true;
            }
        }
        return false;
    }
}
