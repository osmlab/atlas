package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Atlas created tags
 *
 * @author tony
 */
public final class AtlasTag
{
    public static final Set<String> TAGS_FROM_OSM = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(LastEditTimeTag.KEY, LastEditUserIdentifierTag.KEY,
                    LastEditUserNameTag.KEY, LastEditVersionTag.KEY, LastEditChangesetTag.KEY)));

    public static final Set<String> TAGS_FROM_ATLAS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(ISOCountryTag.KEY,
                    SyntheticBoundaryNodeTag.KEY, SyntheticNearestNeighborCountryCodeTag.KEY)));

    private AtlasTag()
    {
    }
}
