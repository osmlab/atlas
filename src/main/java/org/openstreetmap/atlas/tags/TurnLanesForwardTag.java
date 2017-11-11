package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM turn:lanes:forward tag indicating the lane types for a two-way
 *
 * @author brian_l_davis
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/turn%3Alanes%3Aforward#values", osm = "https://wiki.openstreetmap.org/wiki/Key:turn")
public interface TurnLanesForwardTag extends TurnLanesTag
{
    @TagKey
    String KEY = "turn:lanes:forward";

    /**
     * The list of turn types specified in the turn:lanes:forward tag
     *
     * @param taggable
     *            The taggable object being test
     * @return The list of turn types when tagged
     */
    static Optional<List<Set<TurnType>>> getForwardTurnLanes(final Taggable taggable)
    {
        return taggable.getTag(KEY)
                .map(tagValue -> Arrays.stream(tagValue.split(TURN_LANE_DELIMITER))
                        .map(lane -> Arrays.stream(lane.split(TURN_TYPE_DELIMITER))
                                .map(TurnType::safeValueOf).filter(Objects::nonNull)
                                .collect(Collectors.toSet()))
                        .collect(Collectors.toList()));
    }

    /**
     * Checks if the {@link Taggable} has the {@link TurnType} in the {@code turn:lanes:forward}
     * tags.
     *
     * @param taggable
     *            The taggable object being test
     * @param turnType
     *            A turn type to test for
     * @return {@code true} if tagged with the turn type, otherwise {@code false}
     */
    static boolean hasForwardTurnLane(final Taggable taggable, final TurnType turnType)
    {
        return getForwardTurnLanes(taggable)
                .map(lanes -> lanes.stream().anyMatch(turnLane -> turnLane.contains(turnType)))
                .orElse(false);
    }

    /**
     * Checks if the {@link Taggable} has a {@code turn:lanes:forward} tag value.
     *
     * @param taggable
     *            The taggable object being test
     * @return {@code true} if tagged, otherwise {@code false}
     */
    static boolean hasForwardTurnLane(final Taggable taggable)
    {
        return taggable.getTag(KEY).isPresent();
    }
}
