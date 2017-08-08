package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM Boundary Tag. Also works with the {@link AdministrativeLevelTag}.
 *
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/boundary#values", osm = "http://wiki.openstreetmap.org/wiki/Key:boundary")
public enum BoundaryTag
{
    ADMINISTRATIVE,
    HISTORIC,
    MARITIME,
    NATIONAL_PARK,
    POLITICAL,
    POSTAL_CODE,
    RELIGIOUS_ADMINISTRATION,
    PROTECTED_AREA;

    @TagKey
    public static final String KEY = "boundary";

    public static boolean isAdministrative(final Taggable taggable)
    {
        return Validators.isOfType(taggable, BoundaryTag.class, ADMINISTRATIVE);
    }
}
