package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM access tag
 *
 * @author robert_stack
 * @author matthieun
 * @author pmi
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/access#values", osm = "http://wiki.openstreetmap.org/wiki/Key:access")
public enum AccessTag
{
    YES,
    PRIVATE,
    NO,
    PERMISSIVE,
    AGRICULTURAL,
    USE_SIDEPATH,
    DELIVERY,
    DESIGNATED,
    DISMOUNT,
    DISCOURAGED,
    FORESTRY,
    DESTINATION,
    CUSTOMERS,
    PROHIBITED,
    RESTRICTED,
    UNKNOWN;

    private static final EnumSet<AccessTag> PRIVATE_ACCESS = EnumSet.of(CUSTOMERS, NO, PRIVATE,
            RESTRICTED, PROHIBITED);

    @TagKey
    public static final String KEY = "access";

    public static boolean isNo(final Taggable taggable)
    {
        return Validators.isOfType(taggable, AccessTag.class, AccessTag.NO);
    }

    public static boolean isPrivate(final Taggable taggable)
    {
        final Optional<AccessTag> access = Validators.from(AccessTag.class, taggable);
        return access.isPresent() && PRIVATE_ACCESS.contains(access.get());
    }
}
