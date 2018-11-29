package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Set;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Something that takes an {@link Atlas} and produces a set of {@link FeatureChange} that should be
 * apply-able back to the initial {@link Atlas}
 *
 * @author matthieun
 */
public interface AtlasChangeGenerator extends Function<Atlas, Set<FeatureChange>>
{
    @Override
    default Set<FeatureChange> apply(final Atlas atlas)
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
        final Set<FeatureChange> result = generateWithoutValidation(atlas);
        // Validate
        final ChangeBuilder builder = new ChangeBuilder();
        result.forEach(builder::add);
        new ChangeAtlas(atlas, builder.get());
        return result;
    }

    /**
     * Generate a set of changes.
     *
     * @param atlas
     *            The Atlas to generate the changes from.
     * @return The un-validated set of {@link FeatureChange}s
     */
    Set<FeatureChange> generateWithoutValidation(Atlas atlas);
}
