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
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.OptionalIterable;

/**
 * OSM turn:lanes tag indicating the lane types for a one-way road
 *
 * @author brian_l_davis
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/turn%3Alanes#values", osm = "https://wiki.openstreetmap.org/wiki/Key:turn")
public interface TurnLanesTag extends TurnTag
{
    @TagKey
    String KEY = "turn:lanes";

    /**
     * The list of turn types specified in the turn:lanes tag
     *
     * @param taggable
     *            The taggable object being test
     * @return The list of turn types when tagged
     */
    static Optional<List<Set<TurnType>>> getTurnLanes(final Taggable taggable)
    {
        return taggable.getTag(KEY)
                .map(tagValue -> Arrays.stream(tagValue.split(TURN_LANE_DELIMITER))
                        .map(lane -> Arrays.stream(lane.split(TURN_TYPE_DELIMITER))
                                .map(TurnType::safeValueOf).filter(Objects::nonNull)
                                .collect(Collectors.toSet()))
                        .collect(Collectors.toList()));
    }

    /**
     * Checks if the {@link Taggable} has the {@link TurnType} in any of the
     * {@code turn:lanes[:forward|:backward]} tags.
     * 
     * @param taggable
     *            The taggable object being test
     * @param turnType
     *            A turn type to test for
     * @return {@code true} if tagged with the turn type, otherwise {@code false}
     */
    @SuppressWarnings("unchecked")
    static boolean hasTurnLane(final Taggable taggable, final TurnType turnType)
    {
        final OptionalIterable<List<Set<TurnType>>> turnLanes = new OptionalIterable<>(Iterables
                .iterable(getTurnLanes(taggable), TurnLanesForwardTag.getForwardTurnLanes(taggable),
                        TurnLanesBackwardTag.getBackwardTurnLanes(taggable)));
        return Iterables.asList(turnLanes).stream().anyMatch(
                lanes -> lanes.stream().anyMatch(turnLane -> turnLane.contains(turnType)));
    }

    /**
     * Checks if the {@link Taggable} has a {@code turn[:lanes[:forward|:backward]]} tag value.
     *
     * @param taggable
     *            The taggable object being test
     * @return {@code true} if tagged, otherwise {@code false}
     */
    static boolean hasTurnLane(final Taggable taggable)
    {
        return taggable.getTag(TurnLanesTag.KEY).isPresent()
                || taggable.getTag(TurnLanesForwardTag.KEY).isPresent()
                || taggable.getTag(TurnLanesBackwardTag.KEY).isPresent();
    }
}
