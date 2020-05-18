package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag indicating an entity had its geometry sliced during country slicing
 * <p>
 * This is not an OSM tag.
 *
 * @author samg
 */

@Tag(synthetic = true)
public enum SyntheticGeometrySlicedTag
{
    YES;

    @TagKey
    public static final String KEY = "synthetic_geometry_sliced";

    public static boolean isGeometrySliced(final Taggable taggable)
    {
        return Validators.from(SyntheticGeometrySlicedTag.class, taggable).isPresent();
    }
}
