package org.openstreetmap.atlas.geography.atlas.lightweight;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.Nullable;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.complete.EmptyAtlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A lightweight area.
 *
 * @author Taylor Smock
 */
public class LightArea extends Area implements LightLineItem<LightArea>
{
    private static final byte HASH_BYTE = 31;
    private final long identifier;
    private final long[] relationIdentifiers;
    private final Location[] locations;

    /**
     * Create a new area from another area
     *
     * @param from
     *            The area to clone
     * @return A new LightArea
     */
    static LightArea from(final Area from)
    {
        return new LightArea(from);
    }

    /**
     * Create a new LightArea with just an identifier
     *
     * @param identifier
     *            The identifier
     */
    LightArea(final long identifier)
    {
        this(identifier, EMPTY_LOCATION_ARRAY);
    }

    /**
     * Create a new LightArea with just an identifier and points
     *
     * @param identifier
     *            The identifier
     * @param points
     *            The points of the area
     */
    LightArea(final long identifier, final Location... points)
    {
        super(new EmptyAtlas());
        this.identifier = identifier;
        this.relationIdentifiers = EMPTY_LONG_ARRAY;
        this.locations = points.length > 0 ? points.clone() : points;
    }

    /**
     * Create a new LightArea from another Area
     *
     * @param from
     *            The area to copy from
     */
    LightArea(final Area from)
    {
        super(new EmptyAtlas());
        this.identifier = from.getIdentifier();
        this.relationIdentifiers = from.relations().stream().mapToLong(Relation::getIdentifier)
                .toArray();
        this.locations = from.asPolygon().toArray(EMPTY_LOCATION_ARRAY);
    }

    @Override
    @Nullable
    public PolyLine asPolyLine()
    {
        if (this.locations.length > 0)
        {
            return new PolyLine(this.locations);
        }
        return null;
    }

    @Override
    @Nullable
    public Polygon asPolygon()
    {
        if (this.locations.length > 0)
        {
            return new Polygon(this.locations);
        }
        return null;
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
        if (!super.equals(other))
        {
            return false;
        }
        final var lightArea = (LightArea) other;
        return this.identifier == lightArea.identifier
                && Arrays.equals(this.relationIdentifiers, lightArea.relationIdentifiers)
                && Arrays.equals(this.locations, lightArea.locations);
    }

    @Nullable
    @Override
    public Iterable<Location> getGeometry()
    {
        if (this.locations.length > 0)
        {
            return Iterables.asList(this.locations);
        }
        return null;
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
        result = HASH_BYTE * result + Arrays.hashCode(this.locations);
        return result;
    }

    /**
     * Please note that the relations returned from this method should *only* be used for
     * identifiers.
     *
     * @see Area#relations()
     * @return A set of identifier only relations
     */
    @Override
    public Set<Relation> relations()
    {
        return LongStream.of(this.relationIdentifiers).mapToObj(LightRelation::new)
                .collect(Collectors.toSet());
    }
}
