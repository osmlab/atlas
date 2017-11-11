package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Records any additions, updates and deletions that occurred during country slicing. We try to keep
 * this class as light-weight as possible. For additions, we rely on the {@link TemporaryEntity}
 * objects to keep track of the bare minimum needed to create Atlas entities. For tag updates, we
 * store a mapping between the identifier of the object and the added tags. For deletions, we store
 * the set of identifiers to delete. We also keep a mapping between deleted and created lines to
 * properly update any relations.
 *
 * @author mgostintsev
 */
public class RawAtlasChangeSet
{
    // Points
    private final List<TemporaryPoint> createdPoints;
    private final Map<Long, Map<String, String>> updatedPointTags;

    // Lines
    private final List<TemporaryLine> createdLines;
    private final Map<Long, Map<String, String>> updatedLineTags;
    private final Set<Long> deletedLines;
    private final Map<Long, List<Long>> deletedToCreatedLineMapping;

    // Relations -- created, modified, deleted
    // TODO Use TagMap instead of Map<String, String>

    public RawAtlasChangeSet()
    {
        this.createdPoints = new ArrayList<>();
        this.updatedPointTags = new HashMap<>();
        this.createdLines = new ArrayList<>();
        this.updatedLineTags = new HashMap<>();
        this.deletedLines = new HashSet<>();
        this.deletedToCreatedLineMapping = new HashMap<>();
    }

    public void createDeletedToCreatedMapping(final long deletedIdentifier,
            final List<Long> createdIdentifiers)
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

    public List<TemporaryLine> getCreatedLines()
    {
        return this.createdLines;
    }

    public List<TemporaryPoint> getCreatedPoints()
    {
        return this.createdPoints;
    }

    public Set<Long> getDeletedLines()
    {
        return this.deletedLines;
    }

    public Map<Long, List<Long>> getDeletedToCreatedLineMapping()
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
