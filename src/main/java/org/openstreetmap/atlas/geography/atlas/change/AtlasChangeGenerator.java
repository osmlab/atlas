package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * Something that takes an {@link Atlas} and produces a set of {@link FeatureChange} that should be
 * apply-able back to the initial {@link Atlas}
 *
 * @author matthieun
 */
public interface AtlasChangeGenerator extends Converter<Atlas, Set<FeatureChange>>, Serializable
{
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
        final Set<FeatureChange> result = new FeatureChangeBoundsExpander(
                generateWithoutValidation(atlas), atlas).apply();
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
