package org.openstreetmap.atlas.vectortiles;

/**
 * This utility class is where you set the minimum zoom for features based on their tags.
 *
 * @author hallahan
 */
final class MinZoom
{
    private MinZoom()
    {
        // Util
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

    static int landuse(final String tagValue)
    {
        switch (tagValue)
        {
            case "basin":
                return 7;
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
