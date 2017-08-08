package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.validation.OrdinalValidator;

/**
 * Extracts integer {@link Number} values.
 *
 * @author mgostintsev
 */
public final class OrdinalExtractor implements TagExtractor<Integer>
{
    @Override
    public Optional<Integer> validateAndExtract(final String value, final Tag tag)
    {
        final OrdinalValidator validator = new OrdinalValidator();
        final Range range = tag.range();
        if (range != null)
        {
            validator.setRange(range.min(), range.max());

            for (final long exclusion : range.exclude())
            {
                validator.excludeValue(exclusion);
            }
        }

        if (validator.isValid(value))
        {
            return Optional.of(Integer.parseInt(value));
        }

        return Optional.empty();
    }
}
