package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries;

import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.AdministrativeLevelTag;
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * Finder for {@link ComplexBoundary}(ies).
 *
 * @author matthieun
 */
public class ComplexBoundaryFinder implements Finder<ComplexBoundary>
{
    // If set to true, the boundaries will be structured with their sub-areas, and any
    // sub-area that has a parent area will not be standalone. If set to false, all
    // boundaries at all levels will be returned independently.
    private boolean withSubAreas;
    // If any, the administrative level to focus on only.
    private Optional<Integer> administrativeLevel;

    /**
     * Construct the finder. All boundaries at all levels will be returned independently.
     */
    public ComplexBoundaryFinder()
    {
        this.withSubAreas = false;
        this.administrativeLevel = Optional.empty();
    }

    @Override
    public Iterable<ComplexBoundary> find(final Atlas atlas)
    {
        return Iterables.stream(new MultiIterable<>(atlas.relations(), atlas.areas()))
                .filter(BoundaryTag::isAdministrative)
                // Filter out the relations that are part of a larger admin boundary, as those will
                // be brought in as part of the larger boundary.
                .filter(this::subAreaFilter).map(entity -> new ComplexBoundary(entity,
                        this.withSubAreas, this.administrativeLevel));
    }

    /**
     * @param administrativeLevel
     *            If any, the administrative level to focus on only.
     */
    public void setAdministrativeLevel(final int administrativeLevel)
    {
        final long minimum = AdministrativeLevelTag.minimumAdministrativeLevelValue();
        final long maximum = AdministrativeLevelTag.maximumAdministrativeLevelValue();
        if (administrativeLevel >= minimum && administrativeLevel <= maximum)
        {
            this.administrativeLevel = Optional.of(administrativeLevel);
        }
        else
        {
            throw new CoreException(
                    "Invalid administrative level: {}. Should be between {} and {} included.",
                    administrativeLevel, minimum, maximum);
        }
    }

    /**
     * @param withSubAreas
     *            If set to true, the boundaries will be structured with their sub-areas, and any
     *            sub-area that has a parent area will not be standalone. If set to false, all
     *            boundaries at all levels will be returned independently.
     */
    public void setWithSubAreas(final boolean withSubAreas)
    {
        this.withSubAreas = withSubAreas;
    }

    /**
     * @param entity
     *            An Atlas entity
     * @return True when the entity is has a role subarea within one of its administrative boundary
     *         parent relations.
     */
    private boolean isSubArea(final AtlasEntity entity)
    {
        final Set<Relation> parentRelations = entity.relations();
        for (final Relation parentRelation : parentRelations)
        {
            if (BoundaryTag.isAdministrative(parentRelation))
            {
                final RelationMemberList children = parentRelation.members();
                for (final RelationMember child : children)
                {
                    final AtlasEntity childEntity = child.getEntity();
                    if (childEntity.getClass().equals(entity.getClass())
                            && childEntity.getIdentifier() == entity.getIdentifier())
                    {
                        if (RelationTypeTag.ADMINISTRATIVE_BOUNDARY_ROLE_SUB_AREA
                                .equals(child.getRole()))
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Filter out the sub areas only if the finder is looking for boundaries with sub areas
     * included.
     *
     * @param entity
     *            The entity to filter
     * @return True if the entity is not filtered out
     */
    private boolean subAreaFilter(final AtlasEntity entity)
    {
        return this.withSubAreas ? isSubArea(entity) : true;
    }
}
