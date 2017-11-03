package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.EnumSet;
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
 * Base OSM turn tag indicating a turn direction
 *
 * @author brian_l_davis
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/turn#values", osm = "https://wiki.openstreetmap.org/wiki/Key:turn")
public interface TurnTag
{
    @TagKey
    String KEY = "turn";

    String TURN_LANE_DELIMITER = "\\|";
    String TURN_TYPE_DELIMITER = ";";

    /**
     * The list of turn types specified in the turn tag
     *
     * @param taggable
     *            The taggable object being test
     * @return The list of turn types when tagged
     */
    static Optional<List<Set<TurnType>>> getTurns(final Taggable taggable)
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
     * {@code turn[:lanes[:forward|:backward]]} tags.
     * 
     * @param taggable
     *            The taggable object being test
     * @param turnType
     *            A turn type to test for
     * @return {@code true} if tagged with the turn type, otherwise {@code false}
     */
    @SuppressWarnings("unchecked")
    static boolean hasTurn(final Taggable taggable, final TurnType turnType)
    {
        final OptionalIterable<List<Set<TurnType>>> turnLanes = new OptionalIterable<>(
                Iterables.iterable(getTurns(taggable), TurnLanesTag.getTurnLanes(taggable),
                        TurnLanesForwardTag.getForwardTurnLanes(taggable),
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
    static boolean hasTurn(final Taggable taggable)
    {
        return taggable.getTag(TurnTag.KEY).isPresent()
                || taggable.getTag(TurnLanesTag.KEY).isPresent()
                || taggable.getTag(TurnLanesForwardTag.KEY).isPresent()
                || taggable.getTag(TurnLanesBackwardTag.KEY).isPresent();
    }

    /**
     * @author brian_l_davis
     */
    enum TurnType
    {
        LEFT,
        SHARP_LEFT,
        SLIGHT_LEFT,
        THROUGH,
        RIGHT,
        SHARP_RIGHT,
        SLIGHT_RIGHT,
        REVERSE,
        MERGE_TO_LEFT,
        MERGE_TO_RIGHT,
        NONE;

        static final EnumSet<TurnType> leftTurn = EnumSet.of(LEFT, SHARP_LEFT, SLIGHT_LEFT,
                MERGE_TO_LEFT);
        static final EnumSet<TurnType> rightTurn = EnumSet.of(RIGHT, SHARP_RIGHT, SLIGHT_RIGHT,
                MERGE_TO_RIGHT);

        static TurnType safeValueOf(final String value)
        {
            try
            {
                return valueOf(value.toUpperCase());
            }
            catch (final IllegalArgumentException ignored)
            {
                return null;
            }
        }
    }
}
