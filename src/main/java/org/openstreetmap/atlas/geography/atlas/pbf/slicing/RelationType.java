package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Commonly used relation types based on <a href= "https://wiki.openstreetmap.org/wiki/Relation">OSM
 * Wiki</a> and <a href= "https://taginfo.openstreetmap.org/keys/type#values">Tag Info</a>
 *
 * @author tony
 */
public enum RelationType
{
    MULTIPOLYGON,
    RESTRICTION,
    ROUTE,
    BOUNDARY,
    OTHER;

    private static final Map<String, RelationType> relationType;

    static
    {
        final Map<String, RelationType> map = new HashMap<>();
        for (final RelationType type : RelationType.values())
        {
            if (type != OTHER)
            {
                map.put(type.asTagValue(), type);
            }
        }
        relationType = Collections.unmodifiableMap(map);
    }

    public static RelationType forValue(final String value)
    {
        final RelationType type = relationType.get(value);
        return type == null ? RelationType.OTHER : type;
    }

    public String asTagValue()
    {
        return this.name().toLowerCase().intern();
    }
}
