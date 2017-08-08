package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM bridge tag
 *
 * @author cstaylor
 * @author pmi
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/bridge#values", osm = "http://wiki.openstreetmap.org/wiki/Key:bridge")
public enum BridgeTag
{
    YES,
    VIADUCT,
    NO,
    AQUEDUCT,
    BOARDWALK,
    MOVABLE,
    SUSPENSION,
    CULVERT,
    ABANDONED,
    LOW_WATER_CROSSING,
    SIMPLE_BRUNNEL,
    COVERED;

    // Left out bridge=no and bridge=simple_brunnel
    private static final EnumSet<BridgeTag> BRIDGE_WAYS = EnumSet.of(YES, VIADUCT, AQUEDUCT,
            BOARDWALK, MOVABLE, SUSPENSION, CULVERT, ABANDONED, LOW_WATER_CROSSING, SIMPLE_BRUNNEL,
            COVERED);

    @TagKey
    public static final String KEY = "bridge";

    public static Optional<BridgeTag> get(final Taggable taggable)
    {
        return Validators.from(BridgeTag.class, taggable);
    }

    public static boolean isBridge(final Taggable taggable)
    {
        final Optional<BridgeTag> bridge = get(taggable);
        return bridge.isPresent() && BRIDGE_WAYS.contains(bridge.get());
    }
}
