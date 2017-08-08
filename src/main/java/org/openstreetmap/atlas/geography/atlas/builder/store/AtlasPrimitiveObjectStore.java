package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.JoinedCollection;
import org.openstreetmap.atlas.utilities.collections.ParallelIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store all primitive entities which can then be used to create @{link Atlas}
 *
 * @author tony
 */
public class AtlasPrimitiveObjectStore
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasPrimitiveObjectStore.class);

    private final Map<Long, AtlasPrimitiveLocationItem> nodes = new HashMap<>();
    private final Map<Long, AtlasPrimitiveLocationItem> points = new HashMap<>();
    private final Map<Long, AtlasPrimitiveLineItem> edges = new HashMap<>();
    private final Map<Long, AtlasPrimitiveLineItem> lines = new HashMap<>();
    private final Map<Long, AtlasPrimitiveArea> areas = new HashMap<>();
    private final Map<Long, AtlasPrimitiveRelation> relations = new HashMap<>();

    public void addArea(final AtlasPrimitiveArea area)
    {
        this.areas.put(area.getIdentifier(), area);
    }

    public void addEdge(final AtlasPrimitiveLineItem edge)
    {
        this.edges.put(edge.getIdentifier(), edge);
    }

    public void addLine(final AtlasPrimitiveLineItem line)
    {
        this.lines.put(line.getIdentifier(), line);
    }

    public void addNode(final AtlasPrimitiveLocationItem node)
    {
        this.nodes.put(node.getIdentifier(), node);
    }

    public void addPoint(final AtlasPrimitiveLocationItem point)
    {
        this.points.put(point.getIdentifier(), point);
    }

    public void addRelation(final AtlasPrimitiveRelation relation)
    {
        this.relations.put(relation.getIdentifier(), relation);
    }

    /**
     * @return an {@link Atlas} object based on this object store
     */
    public Atlas build()
    {
        // There should be no missing identifiers if the data is complete
        final Optional<TemporaryObjectStore> missingIdentifiers = checkDataIntegrity();
        missingIdentifiers.ifPresent(missingObjects ->
        {
            throw new CoreException("Data is not complete, still missing {} ",
                    missingObjects.toDebugString());
        });

        logger.debug("Data is complete, starts to build atlas");

        // Data is complete, run atlas builder
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(new AtlasSize(
                this.getEdges().size(), this.getNodes().size(), this.getAreas().size(),
                this.getLines().size(), this.getPoints().size(), this.getRelations().size()));

        logger.debug("Building atlas nodes...");
        getNodes().values().forEach(node ->
        {
            builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags());
        });

        logger.debug("Building atlas points...");
        getPoints().values().forEach(point ->
        {
            builder.addPoint(point.getIdentifier(), point.getLocation(), point.getTags());
        });

        logger.debug("Building atlas edges...");
        getEdges().values().forEach(edge ->
        {
            builder.addEdge(edge.getIdentifier(), edge.getPolyLine(), edge.getTags());
        });

        logger.debug("Building atlas lines...");
        getLines().values().forEach(line ->
        {
            builder.addLine(line.getIdentifier(), line.getPolyLine(), line.getTags());
        });

        logger.debug("Building atlas areas...");
        getAreas().values().forEach(area ->
        {
            builder.addArea(area.getIdentifier(), area.getPolygon(), area.getTags());
        });

        logger.debug("Building atlas relations...");
        getRelations().values().forEach(relation ->
        {
            builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                    relation.getRelationBean(), relation.getTags());
        });

        return builder.get();
    }

    /**
     * This method checks the data integrity and will be run at the beginning of build() method.
     * Also different readers (PBF/other) can call this method to check if the current data set is
     * complete. If not the reader needs to make data set complete
     *
     * @return A {@link TemporaryObjectStore} if the data in current store is not complete
     */
    public Optional<TemporaryObjectStore> checkDataIntegrity()
    {
        logger.debug("Checking object store data integrity");
        final TemporaryObjectStore missingObjects = new TemporaryObjectStore();

        // Check relations
        getRelations().values().forEach(relation ->
        {
            final RelationBean bean = relation.getRelationBean();
            final ParallelIterable parallel = new ParallelIterable(bean.getMemberIdentifiers(),
                    bean.getMemberTypes());
            final Iterator<JoinedCollection> iterator = parallel.iterator();
            while (iterator.hasNext())
            {
                final JoinedCollection relationMember = iterator.next();
                final long identifier = relationMember.get(0);
                final ItemType type = (ItemType) relationMember.get(1);
                switch (type)
                {
                    case NODE:
                        if (!this.nodes.containsKey(identifier))
                        {
                            missingObjects.addNode(identifier);
                        }
                        break;
                    case EDGE:
                        if (!this.edges.containsKey(identifier))
                        {
                            missingObjects.addEdge(identifier);
                        }
                        break;
                    case AREA:
                        if (!this.areas.containsKey(identifier))
                        {
                            missingObjects.addArea(identifier);
                        }
                        break;
                    case LINE:
                        if (!this.lines.containsKey(identifier))
                        {
                            missingObjects.addLine(identifier);
                        }
                        break;
                    case POINT:
                        if (!this.points.containsKey(identifier))
                        {
                            missingObjects.addPoint(identifier);
                        }
                        break;
                    case RELATION:
                        if (!this.relations.containsKey(identifier))
                        {
                            missingObjects.addRelation(identifier);
                        }
                        break;
                    default:
                        throw new CoreException("Unknown type {}", type);
                }
            }
        });

        // Put location of all nodes into a set
        final Set<Location> locationOfNodes = new HashSet<>(this.nodes.size(), 1);
        getNodes().values().forEach(node -> locationOfNodes.add(node.getLocation()));

        // Check Edge
        getEdges().values().forEach(edge ->
        {
            final PolyLine shape = edge.getPolyLine();
            final Location first = shape.first();
            final Location last = shape.last();
            if (!locationOfNodes.contains(first))
            {
                missingObjects.addLocation(first);
            }
            if (!locationOfNodes.contains(last))
            {
                missingObjects.addLocation(last);
            }
        });

        return missingObjects.isEmpty() ? Optional.empty() : Optional.of(missingObjects);
    }

    public Map<Long, AtlasPrimitiveArea> getAreas()
    {
        return this.areas;
    }

    public Map<Long, AtlasPrimitiveLineItem> getEdges()
    {
        return this.edges;
    }

    public Map<Long, AtlasPrimitiveLineItem> getLines()
    {
        return this.lines;
    }

    public Map<Long, AtlasPrimitiveLocationItem> getNodes()
    {
        return this.nodes;
    }

    public Map<Long, AtlasPrimitiveLocationItem> getPoints()
    {
        return this.points;
    }

    public Map<Long, AtlasPrimitiveRelation> getRelations()
    {
        return this.relations;
    }

    public String summary()
    {
        return "The store has " + this.nodes.size() + " nodes, " + this.points.size() + " points, "
                + this.edges.size() + " edges, " + this.lines.size() + " lines, "
                + this.areas.size() + " areas, " + this.relations.size() + " relations";
    }
}
