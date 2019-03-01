package org.openstreetmap.atlas.geography.atlas.items;

import java.io.Serializable;
import java.util.Map;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.AtlasTag;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * Very loose interface for something that lives off an {@link Atlas}
 *
 * @author matthieun
 */
public interface AtlasObject extends Located, Taggable, Serializable
{
    /**
     * @return The {@link Atlas} this object is attached to
     */
    Atlas getAtlas();

    /**
     * @return This object's identifier
     */
    long getIdentifier();

    /**
     * Get this item's OSM identifier
     *
     * @return This item's OSM identifier
     */
    long getOsmIdentifier();

    /**
     * Atlas objects contain OSM tags plus tags inserted during Atlas generation. This function will
     * remove all but the OSM tags.
     *
     * @return All the OSM tags for the object
     */
    default Map<String, String> getOsmTags()
    {
        return this.getTags(tag -> !AtlasTag.TAGS_FROM_OSM.contains(tag)
                && !AtlasTag.TAGS_FROM_ATLAS.contains(tag));
    }
}
