package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Extracts a {@link Altitude} from a value. Can be used on tag values such as
 * {@link org.openstreetmap.atlas.tags.HeightTag}.
 *
 * @author bbreithaupt
 */
public class AltitudeExtractor implements TagExtractor
{
    /**
     * Validates and converts a value to a {@link Distance}.
     *
     * @param value
     *            {@link String} value.
     * @return {@link Optional} of a {@link Distance}
     */
    public static Optional<Altitude> validateAndExtract(final String value)
    {
        if (value.startsWith("-"))
        {
            final Optional<Distance> distance = LengthExtractor
                    .validateAndExtract(value.substring(1));
            return distance.map(distance1 -> Altitude.meters(distance1.asMeters() * -1));
        }
        final Optional<Distance> distance = LengthExtractor.validateAndExtract(value);
        return distance.map(distance1 -> Altitude.meters(distance1.asMeters()));
    }

    @Override
    public Optional<Altitude> validateAndExtract(final String value, final Tag tag)
    {
        return AltitudeExtractor.validateAndExtract(value);
    }
}
