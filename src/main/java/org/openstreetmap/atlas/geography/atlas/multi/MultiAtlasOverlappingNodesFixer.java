package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.exception.AtlasIntegrityException;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge nodes that exactly overlap.
 *
 * @author matthieun
 */
public class MultiAtlasOverlappingNodesFixer implements Serializable
{

    private static final long serialVersionUID = -8926124646524609913L;

    private static final Logger logger = LoggerFactory
            .getLogger(MultiAtlasOverlappingNodesFixer.class);

    private final MultiAtlas parent;

    // The overlapping nodes... Those maps should be tiny
    private final Map<Long, Long> overlappingNodeIdentifierToMasterNodeIdentifier = new HashMap<>();
    private final MultiMapWithSet<Long, Long> masterNodeIdentifierToOverlappingNodeIdentifier = new MultiMapWithSet<>();

    protected MultiAtlasOverlappingNodesFixer(final MultiAtlas parent)
    {
        this.parent = parent;
    }

    /**
     * This is to build maps of nodes that are at the same location, and to build their master to
     * slave relationship. The master to slave relationship is purely arbitrary, the first node at a
     * location to be found becoming the master.
     */
    protected void aggregateSameLocationNodes()
    {
        this.parent.getNodeIdentifierToAtlasIndices().forEach(identifier ->
        {
            final Node master = this.parent.node(identifier);
            final Set<Node> slaves = nodesOverlapping(master);
            slaves.remove(master);
            if (!slaves.isEmpty())
            {
                if (!this.overlappingNodeIdentifierToMasterNodeIdentifier.containsKey(identifier))
                {
                    // if our master is not already a slave
                    slaves.forEach(node ->
                    {
                        final long slaveIdentifier = node.getIdentifier();
                        this.overlappingNodeIdentifierToMasterNodeIdentifier.put(slaveIdentifier,
                                identifier);
                        this.masterNodeIdentifierToOverlappingNodeIdentifier.add(identifier,
                                slaveIdentifier);
                    });
                    warnIfNodesHaveDifferentTags(master, slaves);
                }
            }
        });
    }

    /**
     * In case there is a master node overlapping this node, get the master node.
     *
     * @param identifier
     *            The node identifier to query
     * @return The identifier of the master node that has the exact same location
     */
    protected Optional<Long> masterNode(final Long identifier)
    {
        return Optional
                .ofNullable(this.overlappingNodeIdentifierToMasterNodeIdentifier.get(identifier));
    }

    /**
     * In case this node is a master, get all the overlapping nodes.
     *
     * @param identifier
     *            The node identifier to query
     * @return The identifiers of the overlapping nodes that has the exact same location
     */
    protected Set<Long> overlappingNodes(final Long identifier)
    {
        if (this.masterNodeIdentifierToOverlappingNodeIdentifier.containsKey(identifier))
        {
            return this.masterNodeIdentifierToOverlappingNodeIdentifier.get(identifier);
        }
        else
        {
            return new HashSet<>();
        }
    }

    /**
     * @param master
     *            The node to check for
     * @return all the nodes that have the same location, including itself
     */
    private Set<Node> nodesOverlapping(final Node master)
    {
        final List<Rectangle> bounds = new ArrayList<>();
        // Make sure that the AntiMeridian case is taken care of
        final Location masterLocation = master.getLocation();
        // This will be true for both the minimum antimeridian and the maximum
        if (Longitude.ANTIMERIDIAN_WEST.equals(masterLocation.getLongitude()))
        {
            final Location antimeridianMinimum = new Location(masterLocation.getLatitude(),
                    Longitude.ANTIMERIDIAN_WEST);
            final Location antimeridianMaximum = new Location(masterLocation.getLatitude(),
                    Longitude.ANTIMERIDIAN_EAST);
            bounds.add(Rectangle.forCorners(antimeridianMinimum, antimeridianMinimum));
            bounds.add(Rectangle.forCorners(antimeridianMaximum, antimeridianMaximum));
        }
        else
        {
            bounds.add(master.bounds());
        }
        final Set<Node> others = new HashSet<>();
        for (final Rectangle bound : bounds)
        {
            others.addAll(Iterables.asSet(this.parent.nodesWithin(bound)));
        }
        if (others.isEmpty())
        {
            throw new AtlasIntegrityException("A node has to overlap itself at least! {}", master);
        }
        return others;
    }

    /**
     * Print a warning for nodes that have the same location but different tags.
     *
     * @param master
     *            The master node
     * @param slaves
     *            The slave nodes
     */
    private void warnIfNodesHaveDifferentTags(final Node master, final Set<Node> slaves)
    {
        final Node origin = master;
        final Map<String, String> tags = master.getTags();
        for (final Node node : slaves)
        {
            if (!tags.equals(node.getTags()))
            {
                logger.warn("Nodes overlap but have different tags: {} and {}",
                        origin.getIdentifier(), node.getIdentifier());
            }
        }
    }
}
