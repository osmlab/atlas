package org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryEntity;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryPoint;

/**
 * Records any additions, updates and deletions that occurred during point and way raw Atlas
 * slicing. We try to keep this class as light-weight as possible. For additions, we rely on the
 * {@link TemporaryEntity} objects to keep track of the bare minimum needed to create Atlas
 * entities. For tag updates, we store a mapping between the identifier of the object and the added
 * tags. For deletions, we store the set of identifiers to delete. We also keep a mapping between
 * deleted and created lines to properly update any relations. No relation slicing is done with this
 * class. We only update existing relations if a member line was deleted and split into two or more
 * lines.
 *
 * @author mgostintsev
 */
public class SimpleChangeSet
{
    // TODO Use TagMap instead of Map<String, String>

    // Points
    private final Set<TemporaryPoint> createdPoints;
    private final Map<Long, Map<String, String>> updatedPointTags;

    // Lines
    private final Set<TemporaryLine> createdLines;
    private final Map<Long, Map<String, String>> updatedLineTags;
    private final Set<Long> deletedLines;

    // Lines that were sliced will be deleted and replaced by two or more new line segments. We need
    // to maintain this mapping to maintain relation integrity by removing deleted line members and
    // replacing them with the created sliced segments.
    private final Map<Long, Set<Long>> deletedToCreatedLineMapping;

    public SimpleChangeSet()
    {
        this.createdPoints = new HashSet<>();
        this.updatedPointTags = new HashMap<>();
        this.createdLines = new HashSet<>();
        this.updatedLineTags = new HashMap<>();
        this.deletedLines = new HashSet<>();
        this.deletedToCreatedLineMapping = new HashMap<>();
    }

    public void createDeletedToCreatedMapping(final long deletedIdentifier,
            final Set<Long> createdIdentifiers)
    {
        this.deletedToCreatedLineMapping.put(deletedIdentifier, createdIdentifiers);
    }

    public void createLine(final TemporaryLine line)
    {
        this.createdLines.add(line);
    }

    public void createPoint(final TemporaryPoint point)
    {
        this.createdPoints.add(point);
    }

    public void deleteLine(final long identifier)
    {
        this.deletedLines.add(identifier);
    }

    public Set<TemporaryLine> getCreatedLines()
    {
        return this.createdLines;
    }

    public Set<TemporaryPoint> getCreatedPoints()
    {
        return this.createdPoints;
    }

    public Set<Long> getDeletedLines()
    {
        return this.deletedLines;
    }

    public Map<Long, Set<Long>> getDeletedToCreatedLineMapping()
    {
        return this.deletedToCreatedLineMapping;
    }

    public Map<Long, Map<String, String>> getUpdatedLineTags()
    {
        return this.updatedLineTags;
    }

    public Map<Long, Map<String, String>> getUpdatedPointTags()
    {
        return this.updatedPointTags;
    }

    public void updateLineTags(final long lineIdentifier, final Map<String, String> newTags)
    {
        this.updatedLineTags.put(lineIdentifier, newTags);
    }

    public void updatePointTags(final long pointIdentifier, final Map<String, String> newTags)
    {
        this.updatedPointTags.put(pointIdentifier, newTags);
    }
}
