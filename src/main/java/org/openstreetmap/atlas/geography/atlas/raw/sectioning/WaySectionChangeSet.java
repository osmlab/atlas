package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryEdge;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryNode;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * This class tracks all way-section updates that result in changes to raw atlas entities. The
 * possible changes are:
 * <ul>
 * <li>Raw atlas point becoming a {@link Node}.
 * <li>Raw atlas point staying a {@link Point} (vs. becoming a simple shape point).
 * <li>Raw atlas line becoming an {@link Edge}.
 * <li>Raw atlas line becoming an {@link Area}.
 * <li>Raw atlas line staying an {@link Area}, implying some raw atlas lines get filtered from the
 * final atlas.
 * <li>{@link Relation} member updates. If a raw atlas line becomes a series of {@link Edge}s, we
 * need to update the relation member bean to remove the line and add all the edges that replaced
 * it.
 * </ul>
 *
 * @author mgostintsev
 */
public class WaySectionChangeSet
{
    // All 1-1 mappings
    private final Set<Long> linesToBecomeAreas;
    private final Set<Long> linesExcludedFromAtlas;
    private final Set<Long> pointsToStayPoints;

    // Mapping of line identifiers (that become edges) to TemporaryNodes
    private final Map<Long, NodeOccurrenceCounter> edgeToNodeMapping;

    // Mapping of line identifiers to temporary edges. These are needed so we can update the
    // relation member list after sectioning is completed.
    private final MultiMap<Long, TemporaryEdge> lineToEdgeMapping;

    public WaySectionChangeSet()
    {
        this.linesToBecomeAreas = new HashSet<>();
        this.linesExcludedFromAtlas = new HashSet<>();
        this.pointsToStayPoints = new HashSet<>();
        this.edgeToNodeMapping = new HashMap<>();
        this.lineToEdgeMapping = new MultiMap<>();
    }

    public void createEdgeToNodeMapping(final long edgeIdentifier,
            final NodeOccurrenceCounter nodes)
    {
        this.edgeToNodeMapping.put(edgeIdentifier, nodes);
    }

    public void createLineToEdgeMapping(final Line line, final List<TemporaryEdge> edges)
    {
        this.lineToEdgeMapping.put(line.getIdentifier(), edges);
    }

    public List<TemporaryEdge> getCreatedEdges()
    {
        return this.lineToEdgeMapping.allValues();
    }

    public Set<TemporaryNode> getCreatedNodes()
    {
        return this.edgeToNodeMapping.values().stream()
                .flatMap(mapping -> mapping.getNodes().stream()).collect(Collectors.toSet());
    }

    public Set<Long> getExcludedLines()
    {
        return this.linesExcludedFromAtlas;
    }

    public Set<Long> getLinesThatBecomeAreas()
    {
        return this.linesToBecomeAreas;
    }

    public Set<Long> getLinesThatBecomeEdges()
    {
        return this.edgeToNodeMapping.keySet();
    }

    public MultiMap<Long, TemporaryEdge> getLineToCreatedEdgesMapping()
    {
        return this.lineToEdgeMapping;
    }

    public NodeOccurrenceCounter getNodesForEdge(final Line line)
    {
        return this.edgeToNodeMapping.get(line.getIdentifier());
    }

    public Set<Long> getPointsThatBecomeNodes()
    {
        return this.getCreatedNodes().stream().map(TemporaryNode::getIdentifier)
                .collect(Collectors.toSet());
    }

    public Set<Long> getPointsThatStayPoints()
    {
        return this.pointsToStayPoints;
    }

    public void recordArea(final Line line)
    {
        this.linesToBecomeAreas.add(line.getIdentifier());
    }

    public void recordExcludedLine(final Line line)
    {
        this.linesExcludedFromAtlas.add(line.getIdentifier());
    }

    public void recordPoint(final Point point)
    {
        this.pointsToStayPoints.add(point.getIdentifier());
    }
}
