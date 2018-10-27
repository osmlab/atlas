package org.openstreetmap.atlas.vectortiles;

/**
 * This utility class is where you set the minimum zoom for features based on their tags.
 *
 * The values you see here for various min zooms is inspired for what you see in the standard OpenStreetMap
 * carto style. This is very loosely based off of the minimum zooms you see for various types of features.
 *
 * Note that there is definitely more work that needs to be done to refine our min zooms.
 *
 * https://github.com/gravitystorm/openstreetmap-carto
 *
 * @author hallahan
 */
final class MinZoom
{
    private MinZoom()
    {
        // Util
    }

    static int amenity(final String tagValue)
    {
        switch (tagValue)
        {
            case "hospital":
                return 12;
        }
        return 13;
    }

    static int building(final String tagValue)
    {
        return 13;
    }

    static int highway(final String tagValue)
    {
        switch (tagValue)
        {
            case "motorway":
            case "trunk":
                return 6;
            case "primary":
                return 8;
            case "secondary":
                return 9;
            case "tertiary":
                return 10;
            case "motorway_link":
            case "trunk_link":
            case "primary_link":
            case "secondary_link":
            case "residential":
            case "unclassified":
                return 12;
            case "service":
            case "living_street":
            case "pedestrian":
            case "bridleway":
            case "footway":
            case "cycleway":
            case "track":
            case "steps":
                return 13;
            default:
                return 14;
        }
    }

    static int railway(final String tagValue)
    {
        switch (tagValue)
        {
            case "rail":
            case "light_rail":
                return 8;
            case "tram":
            case "subway":
                return 12;
            case "disused":
                return 14;
            default:
                return 13;
        }
    }

    static int route(final String tagValue)
    {
        switch (tagValue)
        {
            case "ferry":
                return 8;
            default:
                return 14;
        }
    }

    static int place(final String tagValue)
    {
        switch (tagValue)
        {
            case "country":
                return 3;
            case "state":
                return 5;
            default:
                return 12;
        }
    }

    static int natural(final String tagValue)
    {
        switch (tagValue)
        {
            case "glacier":
                return 8;
            default:
                return 12;
        }
    }

    static int leisure(final String tagValue)
    {
        switch (tagValue)
        {
            case "park":
                return 8;
            default:
                return 13;
        }
    }

    static int landuse(final String tagValue)
    {
        switch (tagValue)
        {
            case "basin":
                return 7;
            case "forest":
                return 8;
            default:
                return 12;
        }
    }

    static int waterway(final String tagValue)
    {
        switch (tagValue)
        {
            case "river":
                return 8;
            default:
                return 12;
        }
    }
}
