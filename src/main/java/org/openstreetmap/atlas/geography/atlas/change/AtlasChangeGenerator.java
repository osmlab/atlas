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

/**
 * Something that takes an {@link Atlas} and produces a set of {@link FeatureChange} that should be
 * apply-able back to the initial {@link Atlas}
 *
 * @author matthieun
 */
public interface AtlasChangeGenerator extends Converter<Atlas, Set<FeatureChange>>, Serializable
{
    static Set<FeatureChange> expandNodeBounds(final Atlas atlas,
            final Set<FeatureChange> featureChanges)
    {
        final Set<FeatureChange> result = new HashSet<>();
        for (final FeatureChange featureChange : featureChanges)
        {
            if (featureChange.getItemType() == ItemType.NODE)
            {
                Rectangle newBounds = featureChange.bounds();
                final long originalNodeIdentifier = featureChange.getIdentifier();
                final CompleteNode originalCompleteNode = (CompleteNode) featureChange
                        .getAfterView();
                final Node originalNode = atlas.node(originalNodeIdentifier);
                if (originalNode != null)
                {
                    for (final Edge originalEdge : originalNode.connectedEdges())
                    {
                        newBounds = newBounds.combine(originalEdge.bounds());
                    }
                }
                for (final FeatureChange featureChangeInner : featureChanges)
                {
                    if (featureChangeInner.getItemType() == ItemType.EDGE)
                    {
                        final Edge changedEdge = (Edge) featureChangeInner.getAfterView();
                        if (changedEdge.start() != null
                                && changedEdge.start().getIdentifier() == originalNodeIdentifier
                                || changedEdge.end() != null && changedEdge.end()
                                        .getIdentifier() == originalNodeIdentifier)
                        {
                            newBounds = newBounds.combine(changedEdge.bounds());
                        }
                    }
                }
                final FeatureChange newFeatureChange = new FeatureChange(
                        featureChange.getChangeType(),
                        originalCompleteNode.withBoundsExtendedBy(newBounds));
                result.add(newFeatureChange);
            }
            else
            {
                result.add(featureChange);
            }
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

        // Validate
        final ChangeBuilder builder = new ChangeBuilder();
        result.forEach(builder::add);
        final Change change = builder.get();
        new ChangeAtlas(atlas, change);
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
}
