package org.openstreetmap.atlas.geography.atlas.lightweight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.complete.EmptyAtlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

import com.google.common.collect.Iterables;

/**
 * A minimal relation class. Anything that does not need to be stored in memory isn't stored in
 * memory.
 *
 * @author Taylor Smock
 */
public class LightRelation extends Relation implements LightEntity<LightRelation>
{
    private static final byte HASH_BYTE = 31;
    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final ItemType[] EMPTY_ITEM_TYPE_ARRAY = {};
    private static final Location[][] EMPTY_LOCATIONS_ARRAY = {};
    private final long identifier;
    private final long[] relationIdentifiers;
    private final long[] memberIdentifiers;
    private final ItemType[] memberTypes;
    private final String[] memberRoles;
    private final Location[][] memberLocations;

    /**
     * Create a new LightRelation from a relation
     *
     * @param relation
     *            The relation to copy
     * @return The generated light relation
     */
    static LightRelation from(final Relation relation)
    {
        return new LightRelation(relation);
    }

    /**
     * Create a new LightRelation with just an identifier
     *
     * @param identifier
     *            The relation identifier
     */
    LightRelation(final long identifier)
    {
        super(new EmptyAtlas());
        this.identifier = identifier;
        this.relationIdentifiers = EMPTY_LONG_ARRAY;
        this.memberIdentifiers = EMPTY_LONG_ARRAY;
        this.memberRoles = EMPTY_STRING_ARRAY;
        this.memberTypes = EMPTY_ITEM_TYPE_ARRAY;
        this.memberLocations = EMPTY_LOCATIONS_ARRAY;
    }

    /**
     * Create a new LightRelation with basic information from another relation
     *
     * @param from
     *            The relation to copy from
     */
    LightRelation(final Relation from)
    {
        super(new EmptyAtlas());
        this.identifier = from.getIdentifier();
        this.relationIdentifiers = from.relations().stream().mapToLong(Relation::getIdentifier)
                .toArray();
        this.memberIdentifiers = from.allKnownOsmMembers().stream()
                .mapToLong(member -> member.getEntity().getIdentifier()).toArray();
        this.memberTypes = from.allKnownOsmMembers().stream()
                .map(member -> member.getEntity().getType()).toArray(ItemType[]::new);
        this.memberRoles = from.allKnownOsmMembers().stream().map(RelationMember::getRole)
                .toArray(String[]::new);
        this.memberLocations = from.allKnownOsmMembers().stream().map(RelationMember::getEntity)
                .map(entity ->
                {
                    if (entity instanceof AtlasItem)
                    {
                        return Iterables.toArray(((AtlasItem) entity).getRawGeometry(),
                                Location.class);
                    }
                    else if (entity instanceof Relation)
                    {
                        // Don't store sub relation entity at this time -- we aren't storing the
                        // necessary information for
                        // sub relations anyway (we would need to store the sub relation member
                        // information).
                        return EMPTY_LOCATION_ARRAY;
                    }
                    else
                    {
                        throw new CoreException("{0} type not understood", entity.getType());
                    }
                }).toArray(Location[][]::new);
    }

    @Override
    public RelationMemberList allKnownOsmMembers()
    {
        return this.members();
    }

    @Override
    public List<Relation> allRelationsWithSameOsmIdentifier()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || this.getClass() != other.getClass())
        {
            return false;
        }
        final var that = (LightRelation) other;
        return this.identifier == that.identifier
                && Arrays.equals(this.relationIdentifiers, that.relationIdentifiers)
                && Arrays.equals(this.memberIdentifiers, that.memberIdentifiers)
                && Arrays.equals(this.memberTypes, that.memberTypes)
                && Arrays.equals(this.memberRoles, that.memberRoles)
                && Arrays.deepEquals(this.memberLocations, that.memberLocations);
    }

    @Nullable
    @Override
    public Iterable<Location> getGeometry()
    {
        return Stream.of(this.memberLocations).flatMap(Stream::of).collect(Collectors.toList());
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public long[] getRelationIdentifiers()
    {
        return this.relationIdentifiers.clone();
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = HASH_BYTE * result + Long.hashCode(this.identifier);
        result = HASH_BYTE * result + Arrays.hashCode(this.relationIdentifiers);
        result = HASH_BYTE * result + Arrays.hashCode(this.memberIdentifiers);
        result = HASH_BYTE * result + Arrays.hashCode(this.memberTypes);
        result = HASH_BYTE * result + Arrays.hashCode(this.memberRoles);
        result = HASH_BYTE * result + Arrays.deepHashCode(this.memberLocations);
        return result;
    }

    /**
     * Please note that the entities returned from this method should *only* be used for
     * identifiers.
     *
     * @see Relation#members()
     * @return A set of identifier only entities
     */
    @Override
    public RelationMemberList members()
    {
        final List<RelationMember> relationMemberList = new ArrayList<>(
                this.memberIdentifiers.length);
        for (var index = 0; index < this.memberIdentifiers.length; index++)
        {
            final AtlasEntity entity;
            final long memberIdentifier = this.memberIdentifiers[index];
            final ItemType type = this.memberTypes[index];
            if (type == ItemType.RELATION)
            {
                entity = new LightRelation(memberIdentifier);
            }
            else if (type == ItemType.AREA)
            {
                entity = new LightArea(memberIdentifier, this.memberLocations[index]);
            }
            else if (type == ItemType.EDGE)
            {
                entity = new LightEdge(memberIdentifier, this.memberLocations[index]);
            }
            else if (type == ItemType.LINE)
            {
                entity = new LightLine(memberIdentifier, this.memberLocations[index]);
            }
            else if (type == ItemType.NODE)
            {
                entity = new LightNode(memberIdentifier, this.memberLocations[index][0]);
            }
            else if (type == ItemType.POINT)
            {
                entity = new LightPoint(memberIdentifier, this.memberLocations[index][0]);
            }
            else
            {
                throw new CoreException("{0} is not a known type", this.memberTypes[index]);
            }
            relationMemberList
                    .add(new RelationMember(this.memberRoles[index], entity, this.getIdentifier()));
        }
        return new RelationMemberList(relationMemberList);
    }

    @Override
    public Long osmRelationIdentifier()
    {
        return this.getOsmIdentifier();
    }

    /**
     * Please note that the relations returned from this method should *only* be used for
     * identifiers.
     *
     * @see Relation#relations()
     * @return A set of identifier only relations
     */
    @Override
    public Set<Relation> relations()
    {
        return LongStream.of(this.getRelationIdentifiers()).mapToObj(LightRelation::new)
                .collect(Collectors.toSet());
    }

}
