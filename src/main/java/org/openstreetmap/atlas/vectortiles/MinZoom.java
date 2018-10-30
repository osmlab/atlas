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
    private static final int THREE = 3;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int NINE = 9;
    private static final int TEN = 10;
    private static final int TWELVE = 12;
    private static final int THIRTEEN = 13;
    private static final int FOURTEEN = 14;

    private MinZoom()
    {
        // Util
    }

    static int amenity(final String tagValue)
    {
        switch (tagValue)
        {
            case "hospital":
                return TWELVE;
        }
        return THIRTEEN;
    }

    static int building(final String tagValue)
    {
        return THIRTEEN;
    }

    static int highway(final String tagValue)
    {
        switch (tagValue)
        {
            case "motorway":
            case "trunk":
                return SIX;
            case "primary":
                return EIGHT;
            case "secondary":
                return NINE;
            case "tertiary":
                return TEN;
            case "motorway_link":
            case "trunk_link":
            case "primary_link":
            case "secondary_link":
            case "residential":
            case "unclassified":
                return TWELVE;
            case "service":
            case "living_street":
            case "pedestrian":
            case "bridleway":
            case "footway":
            case "cycleway":
            case "track":
            case "steps":
                return THIRTEEN;
            default:
                return FOURTEEN;
        }
    }

    static int railway(final String tagValue)
    {
        switch (tagValue)
        {
            case "rail":
            case "light_rail":
                return EIGHT;
            case "tram":
            case "subway":
                return TWELVE;
            case "disused":
                return FOURTEEN;
            default:
                return THIRTEEN;
        }
    }

    static int route(final String tagValue)
    {
        switch (tagValue)
        {
            case "ferry":
                return EIGHT;
            default:
                return FOURTEEN;
        }
    }

    static int place(final String tagValue)
    {
        switch (tagValue)
        {
            case "country":
                return THREE;
            case "state":
                return FIVE;
            default:
                return TWELVE;
        }
    }

    static int natural(final String tagValue)
    {
        switch (tagValue)
        {
            case "glacier":
                return EIGHT;
            default:
                return TWELVE;
        }
    }

    static int leisure(final String tagValue)
    {
        switch (tagValue)
        {
            case "park":
                return EIGHT;
            default:
                return THIRTEEN;
        }
    }

    static int landuse(final String tagValue)
    {
        switch (tagValue)
        {
            case "basin":
                return SEVEN;
            case "forest":
                return EIGHT;
            default:
                return TWELVE;
        }
    }

    static int waterway(final String tagValue)
    {
        switch (tagValue)
        {
            case "river":
                return EIGHT;
            default:
                return TWELVE;
        }
    }
}
