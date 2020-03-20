package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Type of supported water bodies
 *
 * @author sbhalekar
 */
public enum WaterType
{
    LAKE,
    RIVER,
    CANAL,
    CREEK,
    DITCH,
    LAGOON,
    POND,
    POOL,
    RESERVOIR,
    SEA,
    WETLAND,
    HARBOUR,
    UNKNOWN;

    /**
     * Convert a string to {@link WaterType}
     *
     * @param type
     *            String representation of WaterType
     * @return {@link WaterType}
     */
    public static WaterType from(final String type)
    {
        for (final WaterType waterBodyType : WaterType.values())
        {
            if (waterBodyType.name().equalsIgnoreCase(type))
            {
                return waterBodyType;
            }
        }

        throw new CoreException("Unknown water type {}", type);
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }
}
