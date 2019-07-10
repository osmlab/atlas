package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;

/**
 * Something that takes an {@link Atlas} and produces a set of {@link FeatureChange} that should be
 * apply-able back to the initial {@link Atlas}
 *
 * @author matthieun
 */
public interface AtlasChangeGenerator extends Converter<Atlas, Set<FeatureChange>>, Serializable
{
    static Set<FeatureChange> expandNodeBounds(final Atlas atlas, // NOSONAR
            final Set<FeatureChange> featureChanges)
    {
        final Set<FeatureChange> result = new HashSet<>();
        final Set<FeatureChange> nodes = new HashSet<>();
        final MultiMapWithSet<Long, Rectangle> nodeIdentifierToConnectedEdgeBounds = new MultiMapWithSet<>();
        for (final FeatureChange featureChange : featureChanges)
        {
            final ItemType itemType = featureChange.getItemType();
            if (itemType == ItemType.NODE)
            {
                nodes.add(featureChange);
            }
            else
            {
                result.add(featureChange);
                if (itemType == ItemType.EDGE)
                {
                    final Edge changedEdge = (Edge) featureChange.getAfterView();
                    final Node start = changedEdge.start();
                    final Node end = changedEdge.end();
                    final Rectangle bounds = changedEdge.bounds();
                    if (start != null)
                    {
                        nodeIdentifierToConnectedEdgeBounds.add(start.getIdentifier(), bounds);
                    }
                    if (end != null)
                    {
                        nodeIdentifierToConnectedEdgeBounds.add(end.getIdentifier(), bounds);
                    }
                }
            }
        }
        for (final FeatureChange featureChange : nodes)
        {
            Rectangle newBounds = featureChange.bounds();
            final Long originalNodeIdentifier = featureChange.getIdentifier();
            final CompleteNode originalCompleteNode = (CompleteNode) featureChange.getAfterView();
            final Node originalNode = atlas.node(originalNodeIdentifier);
            if (originalNode != null)
            {
                for (final Edge originalEdge : originalNode.connectedEdges())
                {
                    newBounds = newBounds.combine(originalEdge.bounds());
                }
            }
            if (nodeIdentifierToConnectedEdgeBounds.containsKey(originalNodeIdentifier))
            {
                for (final Rectangle bounds : nodeIdentifierToConnectedEdgeBounds
                        .get(originalNodeIdentifier))
                {
                    newBounds = newBounds.combine(bounds);
                }
            }
            CompleteNode newCompleteNode = originalCompleteNode.withBoundsExtendedBy(newBounds);
            if (originalCompleteNode.getLocation() == null && originalNode != null)
            {
                newCompleteNode = newCompleteNode.withLocation(originalNode.getLocation());
            }
            final FeatureChange newFeatureChange = new FeatureChange(featureChange.getChangeType(),
                    newCompleteNode);
            result.add(newFeatureChange);
        }
        return result;
    }

    @Override
    default Set<FeatureChange> convert(final Atlas atlas)
    {
        return generate(atlas);
    }

    /**
     * Generate a set of changes that make sense out of the gate.
     *
     * @param atlas
     *            The Atlas to generate the changes from.
     * @return The validated set of {@link FeatureChange}s
     */
    default Set<FeatureChange> generate(final Atlas atlas)
    {
        final Set<FeatureChange> result = expandNodeBounds(atlas, generateWithoutValidation(atlas));
        result.stream().forEach(featureChange -> featureChange.withAtlasContext(atlas));

        if (result.isEmpty())
        {
            return result;
        }

        final ChangeBuilder builder = new ChangeBuilder();
        result.forEach(builder::add);
        final Change change = builder.get();

        // Validate
        validate(atlas, change);
        // Return the already merged changes
        return change.changes().collect(Collectors.toSet());
    }

    /**
     * Generate a set of changes.
     *
     * @param atlas
     *            The Atlas to generate the changes from.
     * @return The un-validated set of {@link FeatureChange}s
     */
    Set<FeatureChange> generateWithoutValidation(Atlas atlas);

    default String getName()
    {
        return this.getClass().getSimpleName();
    }

    default void validate(final Atlas source, final Change change)
    {
        new ChangeAtlas(source, change).validate();
    }
}
