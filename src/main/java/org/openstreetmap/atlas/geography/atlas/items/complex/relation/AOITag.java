package org.openstreetmap.atlas.geography.atlas.items.complex.relation;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * @author sayas01
 */
public enum AOITag
{
    AMENITY("FESTIVAL_GROUNDS"),
    LANDUSE("CEMETRY"),
    BOUNDARY("NATIONAL_PARK"),
    LEISURE("PARK");

    private String value;
    AOITag(final String value)
    {
        this.value = value;
    }
    @TagKey
    public static final String KEY = "aoi";

    public static boolean isAoiTag(final Taggable taggable)
    {
        return Validators.isOfType(taggable, AOITag.class,AMENITY) ||
                Validators.isOfType(taggable, AOITag.class,LANDUSE) ||
                Validators.isOfType(taggable, AOITag.class, BOUNDARY) ||
                Validators.isOfType(taggable,AOITag.class, LEISURE);
    }
}
