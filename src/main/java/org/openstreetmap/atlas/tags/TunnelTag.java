package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM tunnel tag
 *
 * @author robert_stack
 * @author pmi
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/tunnel#values", osm = "http://wiki.openstreetmap.org/wiki/Key:tunnel")
public enum TunnelTag
{
    YES,
    CULVERT,
    BUILDING_PASSAGE,
    FLOODED,
    NO;

    private static final EnumSet<TunnelTag> TUNNEL_WAYS = EnumSet.of(YES, CULVERT, BUILDING_PASSAGE,
            FLOODED);

    @TagKey
    public static final String KEY = "tunnel";

    public static boolean isTunnel(final Taggable taggable)
    {
        final Optional<TunnelTag> tunnel = Validators.from(TunnelTag.class, taggable);
        return tunnel.isPresent() && TUNNEL_WAYS.contains(tunnel.get());
    }
}
