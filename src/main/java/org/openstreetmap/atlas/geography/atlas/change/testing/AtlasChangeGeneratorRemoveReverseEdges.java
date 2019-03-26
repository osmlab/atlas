package org.openstreetmap.atlas.geography.atlas.change.testing;

import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.AtlasChangeGenerator;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasChangeGeneratorRemoveReverseEdges implements AtlasChangeGenerator
{
    private static final long serialVersionUID = 2378086577050982603L;

    @Override
    public Set<FeatureChange> generateWithoutValidation(final Atlas atlas)
    {
        return Iterables.stream(atlas.edges()).filter(Edge::isMasterEdge)
                .filter(Edge::hasReverseEdge).map(Edge::reversed).filter(Optional::isPresent)
                .map(Optional::get)
                .map(edge -> FeatureChange.remove(CompleteEntity.shallowFrom(edge))).collectToSet();
    }
}
