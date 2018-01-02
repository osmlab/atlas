package org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryEntity;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryPoint;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelationMember;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;

/**
 * Records any additions, updates and deletions that occurred during relation raw Atlas slicing. We
 * try to keep this class as light-weight as possible. For additions, we rely on the
 * {@link TemporaryEntity} objects to keep track of the bare minimum needed to create Atlas
 * entities. This change set definition assumes that all individual ways and points have already
 * been sliced. The following cases are handled.
 * <p>
 * <ul>
 * <li>1. Point creation - if a new Line needs to be created, we need to create new shape points
 * <li>2. Line creation - new line can be created when multipolygons are sliced by a country
 * boundary
 * <li>3. Relation creation - new relations are created if an existing relation is split into two or
 * more
 * <li>4. Relation deletion - relations are deleted when a relation is split into two or more and we
 * need to get rid of the original one
 * <li>5. Relation tag updates - a relation country code change or synthetic tag addition will be
 * recorded here *
 * <li>6. Relation member addition - a new line member could have been added to a relation to make
 * an open mulitpolygon closed again
 * <li>7. Relation member deletion - a relation member could have been deleted if two or more
 * members were merged into one
 * <li>8. Line deletion - in the rare case, we might delete a line that was added during country
 * slicing of ways and is no longer needed
 * </ul>
 * <p>
 *
 * @author mgostintsev
 */
public class RelationChangeSet
{
    // Created entities
    private final Set<TemporaryPoint> createdPoints;
    private final Map<Long, TemporaryLine> createdLines;
    private final Set<TemporaryRelation> createdRelations;

    // Relation updates
    private final Map<Long, Map<String, String>> updatedRelationTags;
    private final MultiMapWithSet<Long, TemporaryRelationMember> addedRelationMembers;
    private final MultiMapWithSet<Long, TemporaryRelationMember> deletedRelationMembers;

    // Deleted entities
    private final Set<Long> deletedLines;
    private final Set<Long> deletedRelations;

    public RelationChangeSet()
    {
        this.createdPoints = new HashSet<>();
        this.createdLines = new HashMap<>();
        this.createdRelations = new HashSet<>();
        this.updatedRelationTags = new HashMap<>();
        this.addedRelationMembers = new MultiMapWithSet<>();
        this.deletedRelationMembers = new MultiMapWithSet<>();
        this.deletedLines = new HashSet<>();
        this.deletedRelations = new HashSet<>();
    }

    public void addRelationMember(final Long relationIdentifier,
            final TemporaryRelationMember member)
    {
        this.addedRelationMembers.add(relationIdentifier, member);
    }

    public void createLine(final TemporaryLine line)
    {
        this.createdLines.put(line.getIdentifier(), line);
    }

    public void createPoint(final TemporaryPoint point)
    {
        this.createdPoints.add(point);
    }

    public void createRelation(final TemporaryRelation relation)
    {
        this.createdRelations.add(relation);
    }

    public void deleteLine(final Long identifier)
    {
        this.deletedLines.add(identifier);
    }

    public void deleteRelation(final long identifier)
    {
        this.deletedRelations.add(identifier);
    }

    public void deleteRelationMember(final Long relationIdentifier,
            final TemporaryRelationMember member)
    {
        this.deletedRelationMembers.add(relationIdentifier, member);
    }

    public MultiMapWithSet<Long, TemporaryRelationMember> getAddedRelationMembers()
    {
        return this.addedRelationMembers;
    }

    public Map<Long, TemporaryLine> getCreatedLines()
    {
        return this.createdLines;
    }

    public Set<TemporaryPoint> getCreatedPoints()
    {
        return this.createdPoints;
    }

    public Set<TemporaryRelation> getCreatedRelations()
    {
        return this.createdRelations;
    }

    public Set<Long> getDeletedLines()
    {
        return this.deletedLines;
    }

    public MultiMapWithSet<Long, TemporaryRelationMember> getDeletedRelationMembers()
    {
        return this.deletedRelationMembers;
    }

    public Set<Long> getDeletedRelations()
    {
        return this.deletedRelations;
    }

    public Map<Long, Map<String, String>> getUpdatedRelationTags()
    {
        return this.updatedRelationTags;
    }

    /**
     * Track any underlying {@link Relation} changes. This can be a relation deletion or member
     * updates.
     *
     * @param relationIdentifier
     *            The {@link Relation} we're interested in
     * @return {@code true} if this relation got deleted or had any member updates
     */
    public boolean relationGeometryModified(final long relationIdentifier)
    {
        return this.getDeletedRelations().contains(relationIdentifier)
                || this.getAddedRelationMembers().get(relationIdentifier) != null
                || this.getDeletedRelationMembers().get(relationIdentifier) != null;
    }

    public void updateRelationTags(final long relationIdentifier, final Map<String, String> newTags)
    {
        this.updatedRelationTags.put(relationIdentifier, newTags);
    }
}
