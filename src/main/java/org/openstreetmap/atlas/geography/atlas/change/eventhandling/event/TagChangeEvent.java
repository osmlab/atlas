package org.openstreetmap.atlas.geography.atlas.change.eventhandling.event;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.consts.FieldChangeOperation;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;

/**
 * Represents a tag change event in a
 * {@link org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity}.
 *
 * @author Yazad Khambata
 */
public class TagChangeEvent extends EntityChangeEvent
{
    private FieldChangeOperation fieldOperation;

    protected TagChangeEvent(final CompleteItemType completeItemType, final long identifier,
            final FieldChangeOperation fieldOperation)
    {
        super(completeItemType, identifier);
        this.fieldOperation = fieldOperation;
    }

    protected TagChangeEvent(final CompleteItemType completeItemType, final long identifier,
            final Optional<Object> newValue, final FieldChangeOperation fieldOperation)
    {
        super(completeItemType, identifier, newValue);
        this.fieldOperation = fieldOperation;
    }

    public static TagChangeEvent added(final CompleteItemType completeItemType,
            final long identifier, final Pair<String, String> addedTagPair)
    {
        return new TagChangeEvent(completeItemType, identifier, Optional.of(addedTagPair),
                FieldChangeOperation.ADD);
    }

    public static TagChangeEvent remove(final CompleteItemType completeItemType,
            final long identifier, final String key)
    {
        return new TagChangeEvent(completeItemType, identifier, Optional.of(key),
                FieldChangeOperation.REMOVE);
    }

    public static TagChangeEvent replaced(final CompleteItemType completeItemType,
            final long identifier, final Triple<String, String, String> tagReplacementInfo)
    {
        return new TagChangeEvent(completeItemType, identifier, Optional.of(tagReplacementInfo),
                FieldChangeOperation.REPLACE);
    }

    public static TagChangeEvent overwrite(final CompleteItemType completeItemType,
            final long identifier, final Map<String, String> newTags)
    {
        return new TagChangeEvent(completeItemType, identifier, Optional.ofNullable(newTags),
                FieldChangeOperation.OVERWRITE);
    }

    public FieldChangeOperation getFieldOperation()
    {
        return fieldOperation;
    }
}
