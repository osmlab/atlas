package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryEdge;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryNode;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * TODO
 *
 * @author mgostintsev
 */
public class WaySectionChangeSet
{
    // All 1-1 mappings
    private final Set<Long> linesToBecomeAreas;
    private final Set<Long> pointsToStayPoints;

    // Mapping of line identifiers (that become edges) to points identifiers (that become nodes)
    // that are contained by the line. TODO
    private final Map<Long, NodeOccurenceCounter> edgeToNodeMapping;

    // Mapping of line identifiers to temporary edges. These are needed so we can update relation
    // member list after sectioning is completed.
    private final MultiMap<Long, TemporaryEdge> lineToEdgeMapping;

    public WaySectionChangeSet()
    {
        this.linesToBecomeAreas = new HashSet<>();
        this.pointsToStayPoints = new HashSet<>();
        this.edgeToNodeMapping = new HashMap<>();
        this.lineToEdgeMapping = new MultiMap<>();
    }

    public void createEdgeToNodeMapping(final long edgeIdentifier, final NodeOccurenceCounter nodes)
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

    public NodeOccurenceCounter getNodesForEdge(final Line line)
    {
        return this.edgeToNodeMapping.get(line.getIdentifier());
    }

    public Set<TemporaryNode> getPointsThatBecomeNodes()
    {
        final Set<TemporaryNode> allNodes = new HashSet<>();
        this.edgeToNodeMapping.values().forEach(mapping -> allNodes.addAll(mapping.getNodes()));
        return allNodes;
    }

    public Set<Long> getPointsThatStayPoints()
    {
        return this.pointsToStayPoints;
    }

    public void recordArea(final Line line)
    {
        this.linesToBecomeAreas.add(line.getIdentifier());
    }

    public void recordPoint(final Point point)
    {
        this.pointsToStayPoints.add(point.getIdentifier());
    }
}
