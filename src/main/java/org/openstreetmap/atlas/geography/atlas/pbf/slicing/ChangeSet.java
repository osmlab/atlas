package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class stores all changes made during data processing. Until now all changes falls into the
 * case which is splitting one feature into smaller pieces.
 *
 * @author Yiqing Jin
 */
public class ChangeSet
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeSet.class);

    private final List<Node> createdNodes;
    private final List<Node> deletedNodes;
    private final List<Way> createdWays;
    private final List<Way> deletedWays;
    private final List<Relation> createdRelations;
    private final List<Relation> modifiedRelation;
    private final List<Relation> deletedRelations;

    // Mapping between the original way to sliced ways
    private final MultiMap<Long, Way> wayMap;

    public ChangeSet()
    {
        this.createdNodes = new ArrayList<>();
        this.deletedNodes = new ArrayList<>();
        this.createdWays = new ArrayList<>();
        this.deletedWays = new ArrayList<>();
        this.createdRelations = new ArrayList<>();
        this.deletedRelations = new ArrayList<>();
        this.modifiedRelation = new ArrayList<>();
        this.wayMap = new MultiMap<>();
    }

    public void addCreatedNode(final Node createdNode)
    {
        this.createdNodes.add(createdNode);
    }

    public void addCreatedRelation(final Relation relation)
    {
        this.createdRelations.add(relation);
    }

    public void addCreatedWay(final Way createdWay)
    {
        // In the case we cut open a relation formed polygon, new ways will be created to fill the
        // gap. Do we need to add flag for these ways?
        this.createdWays.add(createdWay);
    }

    /**
     * @param createdWay
     *            The newly created {@link Way}
     * @param originalWayIdentifier
     *            The original {@link Way} identifier from which the new {@link Way} is created.
     */
    public void addCreatedWay(final Way createdWay, final long originalWayIdentifier)
    {
        this.createdWays.add(createdWay);
        this.wayMap.add(originalWayIdentifier, createdWay);
    }

    public void addDeletedNode(final Node node)
    {
        this.deletedNodes.add(node);
    }

    public void addDeletedRelation(final Relation relation)
    {
        this.deletedRelations.add(relation);
    }

    public void addDeletedWay(final Way way)
    {
        this.deletedWays.add(way);
    }

    public void addModifiedRelation(final Relation relation)
    {
        this.modifiedRelation.add(relation);
    }

    public List<Node> getCreatedNodes()
    {
        return this.createdNodes;
    }

    public List<Relation> getCreatedRelations()
    {
        return this.createdRelations;
    }

    public List<Way> getCreatedWays()
    {
        return this.createdWays;
    }

    /**
     * @param originalWayIdentifier
     *            The original way identifier
     * @return a {@link List} of {@link Way}s created from the original way specified by the
     *         originalWayIdentifier
     */
    public List<Way> getCreatedWays(final long originalWayIdentifier)
    {
        return this.wayMap.get(originalWayIdentifier);
    }

    public List<Node> getDeletedNodes()
    {
        return this.deletedNodes;
    }

    public List<Relation> getDeletedRelations()
    {
        return this.deletedRelations;
    }

    public List<Way> getDeletedWays()
    {
        return this.deletedWays;
    }

    public List<Relation> getModifiedRelation()
    {
        return this.modifiedRelation;
    }

    public MultiMap<Long, Way> getWayMap()
    {
        return this.wayMap;
    }

    public boolean hasWay(final long wayIdentifier)
    {
        return this.wayMap.containsKey(wayIdentifier);
    }

    public void save(final OutputStream output)
    {
        try
        {
            final BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(output, StandardCharsets.UTF_8));

            for (final Way way : this.createdWays)
            {
                writer.write("create,way,");
                writer.write(String.valueOf(way.getId()));
                writer.newLine();
            }
            writer.write("======new relations=====\n");
            for (final Relation toBeAddedRelation : this.createdRelations)
            {
                writer.write("nr: ");
                writer.write(toBeAddedRelation.toString());
                writer.newLine();
            }
            writer.write("======old ways======\n");
            for (final Way toBeRemovedWay : this.deletedWays)
            {
                writer.write("ow: ");
                writer.write(toBeRemovedWay.toString());
                writer.newLine();
            }
            writer.write("======old relations=====\n");
            for (final Relation toBeRemovedRelation : this.deletedRelations)
            {
                writer.write("or: ");
                writer.write(toBeRemovedRelation.toString());
                writer.newLine();
            }
            writer.write("======modified relations=====\n");
            for (final Relation modified : this.modifiedRelation)
            {
                writer.write("or: ");
                writer.write(modified.toString());
                writer.newLine();
            }
            writer.close();
        }
        catch (final IOException e)
        {
            logger.error("Could not save ChangeSet", e);
        }
    }

}
