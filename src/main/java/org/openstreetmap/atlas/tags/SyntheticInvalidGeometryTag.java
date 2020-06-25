package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag indicating an entity does not conform to the OGC geometry specification.
 * <p>
 * This is not an OSM tag.
 *
 * @author samg
 */

@Tag(synthetic = true)
public enum SyntheticInvalidGeometryTag
{
    YES;

    @TagKey
    public static final String KEY = "synthetic_invalid_geometry";

    public static boolean isInvalidGeometry(final Taggable taggable)
    {
        return Validators.from(SyntheticInvalidGeometryTag.class, taggable).isPresent();
    }
}
