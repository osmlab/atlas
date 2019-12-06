package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM smoothness tag
 *
 * @author bbreithaupt
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/smoothness", osm = "https://wiki.openstreetmap.org/wiki/Key:smoothness")
public enum SmoothnessTag
{
    EXCELLENT,
    GOOD,
    INTERMEDIATE,
    BAD,
    VERY_BAD,
    HORRIBLE,
    VERY_HORRIBLE,
    IMPASSABLE;

    @TagKey
    public static final String KEY = "smoothness";

    public boolean isLessImportantThan(final SmoothnessTag other)
    {
        return this.compareTo(other) > 0;
    }

    public boolean isLessImportantThanOrEqualTo(final SmoothnessTag other)
    {
        return this.compareTo(other) >= 0;
    }

    public boolean isMoreImportantThan(final SmoothnessTag other)
    {
        return this.compareTo(other) < 0;
    }

    public boolean isMoreImportantThanOrEqualTo(final SmoothnessTag other)
    {
        return this.compareTo(other) <= 0;
    }
}
