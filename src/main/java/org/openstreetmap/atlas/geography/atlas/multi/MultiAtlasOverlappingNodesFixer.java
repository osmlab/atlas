package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
 * @author samg
 */
public class MultiAtlasOverlappingNodesFixer implements Serializable
{

    private static final long serialVersionUID = -8926124646524609913L;

    private static final Logger logger = LoggerFactory
            .getLogger(MultiAtlasOverlappingNodesFixer.class);

    private final MultiAtlas parent;
    private final boolean fixNodesOnOppositeAntiMeridians;

    // The overlapping nodes... Those maps should be tiny
    private final Map<Long, Long> overlappingNodeIdentifierToMainNodeIdentifier = new HashMap<>();
    private final MultiMapWithSet<Long, Long> mainNodeIdentifierToOverlappingNodeIdentifier = new MultiMapWithSet<>();

    protected MultiAtlasOverlappingNodesFixer(final MultiAtlas parent,
            final boolean fixNodesOnOppositeAntiMeridians)
    {
        this.parent = parent;
        this.fixNodesOnOppositeAntiMeridians = fixNodesOnOppositeAntiMeridians;
    }

    /**
     * This is to build maps of nodes that are at the same location, and to pick the main node based
     * on point identifier
     */
    protected void aggregateSameLocationNodes()
    {
        this.parent.getNodeIdentifierToAtlasIndices().forEach(identifier ->
        {
            // if this identifier is in the map, then skip. otherwise, poll all
            // overlapping nodes here, pick a main node based on identifier,
            // and update the map
            if (!this.overlappingNodeIdentifierToMainNodeIdentifier.containsKey(identifier))
            {
                final Node current = this.parent.node(identifier);
                final SortedSet<Node> overlapping = new TreeSet<>((final Node a, final Node b) ->
                {
                    if (a.getIdentifier() < b.getIdentifier())
                    {
                        return -1;
                    }
                    else if (a.getIdentifier() > b.getIdentifier())
                    {
                        return 1;
                    }
                    return 0;
                });
                overlapping.addAll(nodesOverlapping(current));
                if (overlapping.size() > 1)
                {
                    final Node main = overlapping.first();
                    final long mainIdentifier = main.getIdentifier();
                    overlapping.remove(main);
                    overlapping.forEach(node ->
                    {
                        final long overlappingIdentifier = node.getIdentifier();
                        this.overlappingNodeIdentifierToMainNodeIdentifier
                                .put(overlappingIdentifier, mainIdentifier);
                        this.mainNodeIdentifierToOverlappingNodeIdentifier.add(mainIdentifier,
                                overlappingIdentifier);
                    });
                    warnIfNodesHaveDifferentTags(main, overlapping);
                }
            }
        });
    }

    /**
     * In case there is a main node overlapping this node, get the main node.
     *
     * @param identifier
     *            The node identifier to query
     * @return The identifier of the main node that has the exact same location
     */
    protected Optional<Long> mainNode(final Long identifier)
    {
        return Optional
                .ofNullable(this.overlappingNodeIdentifierToMainNodeIdentifier.get(identifier));
    }

    @Deprecated
    protected Optional<Long> masterNode(final Long identifier)
    {
        return mainNode(identifier);
    }

    /**
     * In case this node is a main, get all the overlapping nodes.
     *
     * @param identifier
     *            The node identifier to query
     * @return The identifiers of the overlapping nodes that has the exact same location
     */
    protected Set<Long> overlappingNodes(final Long identifier)
    {
        if (this.mainNodeIdentifierToOverlappingNodeIdentifier.containsKey(identifier))
        {
            return this.mainNodeIdentifierToOverlappingNodeIdentifier.get(identifier);
        }
        else
        {
            return new HashSet<>();
        }
    }

    /**
     * @param main
     *            The node to check for
     * @return all the nodes that have the same location, including itself
     */
    private Set<Node> nodesOverlapping(final Node main)
    {
        final List<Rectangle> bounds = new ArrayList<>();
        // Make sure that the AntiMeridian case is taken care of
        final Location mainLocation = main.getLocation();
        // This will be true for both the minimum antimeridian and the maximum.
        if (this.fixNodesOnOppositeAntiMeridians
                && (Longitude.ANTIMERIDIAN_WEST.equals(masterLocation.getLongitude())
                        || Longitude.ANTIMERIDIAN_EAST.equals(masterLocation.getLongitude())))
        {
            final Location antimeridianMinimum = new Location(mainLocation.getLatitude(),
                    Longitude.ANTIMERIDIAN_WEST);
            final Location antimeridianMaximum = new Location(mainLocation.getLatitude(),
                    Longitude.ANTIMERIDIAN_EAST);
            bounds.add(Rectangle.forCorners(antimeridianMinimum, antimeridianMinimum));
            bounds.add(Rectangle.forCorners(antimeridianMaximum, antimeridianMaximum));
        }
        else
        {
            bounds.add(main.bounds());
        }
        final Set<Node> others = new HashSet<>();
        for (final Rectangle bound : bounds)
        {
            others.addAll(Iterables.asSet(this.parent.nodesWithin(bound)));
        }
        if (others.isEmpty())
        {
            throw new AtlasIntegrityException("A node has to overlap itself at least! {}", main);
        }
        return others;
    }

    /**
     * Print a warning for nodes that have the same location but different tags.
     *
     * @param main
     *            The main node
     * @param overlapping
     *            The overlapping nodes
     */
    private void warnIfNodesHaveDifferentTags(final Node main, final Set<Node> overlapping)
    {
        final Map<String, String> tags = main.getTags();
        for (final Node node : overlapping)
        {
            if (!tags.equals(node.getTags()))
            {
                logger.warn("Nodes overlap but have different tags: {} and {}",
                        main.getIdentifier(), node.getIdentifier());
            }
        }
    }
}
