package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

import com.google.common.collect.EnumBiMap;

/**
 * OSM highway tag
 *
 * @author cstaylor
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/highway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:highway")
public enum HighwayTag
{
    MOTORWAY,
    MOTORWAY_LINK,
    TRUNK,
    TRUNK_LINK,
    PRIMARY,
    PRIMARY_LINK,
    SECONDARY,
    SECONDARY_LINK,
    TERTIARY,
    TERTIARY_LINK,
    UNCLASSIFIED,
    RESIDENTIAL,
    SERVICE,
    TRACK,
    LIVING_STREET,
    PEDESTRIAN,
    BUS_GUIDEWAY,
    RACEWAY,
    ROAD,
    CYCLEWAY,
    FOOTWAY,
    BRIDLEWAY,
    STEPS,
    PATH,
    PROPOSED,
    CONSTRUCTION,
    ESCAPE,
    BUS_STOP,
    CROSSING,
    ELEVATOR,
    EMERGENCY_ACCESS_POINT,
    GIVE_WAY,
    MINI_ROUNDABOUT,
    MOTORWAY_JUNCTION,
    PASSING_PLACE,
    REST_AREA,
    SPEED_CAMERA,
    STREET_LAMP,
    SERVICES,
    STOP,
    TRAFFIC_SIGNALS,
    TURNING_CIRCLE,
    PLATFORM,
    MILESTONE,
    TURNING_LOOP,
    CORRIDOR,
    NO;

    @TagKey
    public static final String KEY = "highway";

    private static final EnumSet<HighwayTag> CORE_WAYS = EnumSet.of(MOTORWAY, TRUNK, PRIMARY,
            SECONDARY, TERTIARY, UNCLASSIFIED, RESIDENTIAL, SERVICE, MOTORWAY_LINK, TRUNK_LINK,
            PRIMARY_LINK, SECONDARY_LINK, TERTIARY_LINK, LIVING_STREET, PEDESTRIAN, TRACK,
            BUS_GUIDEWAY, RACEWAY, ROAD, FOOTWAY, BRIDLEWAY, STEPS, PATH, CYCLEWAY, ESCAPE);

    private static final EnumSet<HighwayTag> METRICS_HIGHWAYS = EnumSet.of(MOTORWAY, TRUNK, PRIMARY,
            SECONDARY, TERTIARY, UNCLASSIFIED, RESIDENTIAL, SERVICE, MOTORWAY_LINK, TRUNK_LINK,
            PRIMARY_LINK, SECONDARY_LINK, TERTIARY_LINK, LIVING_STREET, PEDESTRIAN, TRACK,
            BUS_GUIDEWAY, FOOTWAY, BRIDLEWAY, STEPS, PATH, CYCLEWAY, ESCAPE);

    private static final EnumSet<HighwayTag> CAR_NAVIGABLE_HIGHWAYS = EnumSet.of(MOTORWAY, TRUNK,
            PRIMARY, SECONDARY, TERTIARY, UNCLASSIFIED, RESIDENTIAL, SERVICE, MOTORWAY_LINK,
            TRUNK_LINK, PRIMARY_LINK, SECONDARY_LINK, TERTIARY_LINK, LIVING_STREET, TRACK, ROAD);

    private static final EnumSet<HighwayTag> PEDESTRIAN_NAVIGABLE_HIGHWAYS = EnumSet.of(PEDESTRIAN,
            FOOTWAY, STEPS, PATH, CROSSING, PLATFORM, ELEVATOR, CORRIDOR);

    private static final EnumBiMap<HighwayTag, HighwayTag> HIGHWAY_LINKS = EnumBiMap
            .create(HighwayTag.class, HighwayTag.class);

    static
    {
        HIGHWAY_LINKS.put(MOTORWAY, MOTORWAY_LINK);
        HIGHWAY_LINKS.put(TRUNK, TRUNK_LINK);
        HIGHWAY_LINKS.put(PRIMARY, PRIMARY_LINK);
        HIGHWAY_LINKS.put(SECONDARY, SECONDARY_LINK);
        HIGHWAY_LINKS.put(TERTIARY, TERTIARY_LINK);
    }

    public static Optional<HighwayTag> highwayTag(final Taggable taggable)
    {
        return Validators.from(HighwayTag.class, taggable);
    }

    public static boolean isCarNavigableHighway(final HighwayTag tag)
    {
        return CAR_NAVIGABLE_HIGHWAYS.contains(tag);
    }

    public static boolean isCarNavigableHighway(final Taggable taggable)
    {
        final Optional<HighwayTag> highway = highwayTag(taggable);
        return highway.isPresent() && CAR_NAVIGABLE_HIGHWAYS.contains(highway.get());
    }

    public static boolean isCoreWay(final Taggable taggable)
    {
        final Optional<HighwayTag> highway = highwayTag(taggable);
        return highway.isPresent() && CORE_WAYS.contains(highway.get());
    }

    /**
     * Looking for (highway=pedestrian or highway=footway) and (building = * or area=yes) tag
     * combination. These are pedestrian plazas and can contain valid road intersections.
     *
     * @param taggable
     *            The taggable object being test
     * @return Whether the taggable object is a highway area or not
     */
    public static boolean isHighwayArea(final Taggable taggable)
    {
        return Validators.isOfType(taggable, HighwayTag.class, HighwayTag.PEDESTRIAN,
                HighwayTag.FOOTWAY)
                && (Validators.isOfType(taggable, AreaTag.class, AreaTag.YES)
                        || Validators.hasValuesFor(taggable, BuildingTag.class));
    }

    public static boolean isHighwayWithLink(final Taggable taggable)
    {
        final Optional<HighwayTag> highway = highwayTag(taggable);
        return highway.isPresent() && HIGHWAY_LINKS.containsKey(highway.get());
    }

    public static boolean isLinkHighway(final HighwayTag tag)
    {
        return HIGHWAY_LINKS.containsValue(tag);
    }

    public static boolean isLinkHighway(final Taggable taggable)
    {
        final Optional<HighwayTag> highway = highwayTag(taggable);
        return highway.isPresent() && HIGHWAY_LINKS.containsValue(highway.get());
    }

    public static boolean isMetricHighway(final Taggable taggable)
    {
        final Optional<HighwayTag> highway = highwayTag(taggable);
        return highway.isPresent() && METRICS_HIGHWAYS.contains(highway.get());
    }

    public static boolean isPedestrianCrossing(final Taggable taggable)
    {
        return Validators.isOfType(taggable, HighwayTag.class, CROSSING);
    }

    public static boolean isPedestrianNavigableHighway(final Taggable taggable)
    {
        final Optional<HighwayTag> highway = highwayTag(taggable);
        return highway.isPresent() && PEDESTRIAN_NAVIGABLE_HIGHWAYS.contains(highway.get());
    }

    public static Optional<HighwayTag> tag(final Taggable taggable)
    {
        return Validators.from(HighwayTag.class, taggable);
    }

    /**
     * Checks if the current highway type has a complementary link type
     *
     * @return true if has a link type for highway
     */
    public boolean canHaveLink()
    {
        return HIGHWAY_LINKS.containsKey(this);
    }

    /**
     * Gets the highway type from a link type. So PRIMARY_LINK will return PRIMARY
     *
     * @return an Optional {@link HighwayTag}, if highway type does not have a link will return
     *         Optional.empty
     */
    public Optional<HighwayTag> getHighwayFromLink()
    {
        return Optional.ofNullable(HIGHWAY_LINKS.inverse().get(this));
    }

    /**
     * Gets the highway_link type from the highway type. So PRIMARY will return PRIMARY_LINK
     *
     * @return an Optional {@link HighwayTag}, if no link for highway available will return
     *         Optional.empty
     */
    public Optional<HighwayTag> getLinkFromHighway()
    {
        return Optional.ofNullable(HIGHWAY_LINKS.get(this));
    }

    public String getTagValue()
    {
        return name().toLowerCase().intern();
    }

    /**
     * Checks to see if one highway type has the identical classification as another.
     *
     * @param tag
     *            The {@link HighwayTag} that you are comparing this to
     * @return {@code true} if class is the same
     */
    public boolean isIdenticalClassification(final HighwayTag tag)
    {
        return this == tag;
    }

    public boolean isLessImportantThan(final HighwayTag other)
    {
        return this.compareTo(other) > 0;
    }

    public boolean isLessImportantThanOrEqualTo(final HighwayTag other)
    {
        return this.compareTo(other) >= 0;
    }

    /**
     * Checks to see if the highway type is a link type
     *
     * @return true if it is link type
     */
    public boolean isLink()
    {
        return HIGHWAY_LINKS.containsValue(this);
    }

    public boolean isMoreImportantThan(final HighwayTag other)
    {
        return this.compareTo(other) < 0;
    }

    public boolean isMoreImportantThanOrEqualTo(final HighwayTag other)
    {
        return this.compareTo(other) <= 0;
    }

    /**
     * Checks to see if one highway type has an equal classification as another. This either
     * indicates that it is the exact same type, or that one is a link and the other the same
     * non-link type. e.g. TRUNK and TRUNK_LINK.
     *
     * @param tag
     *            The {@link HighwayTag} that you are comparing this to
     * @return {@code true} if class the same
     */
    public boolean isOfEqualClassification(final HighwayTag tag)
    {
        return this == tag || HIGHWAY_LINKS.get(this) == tag
                || HIGHWAY_LINKS.inverse().get(this) == tag;
    }

}
