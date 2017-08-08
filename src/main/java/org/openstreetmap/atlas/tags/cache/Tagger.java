package org.openstreetmap.atlas.tags.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Cache for Tags of certain type. For applications that check tags on big numbers of objects, it
 * would save time to cache associations between tag names and their representative Enum values.
 *
 * @author gpogulsky
 * @param <T>
 *            - type of tag Enum class
 */
public class Tagger<T extends Enum<T>> implements Serializable
{
    private static final long serialVersionUID = -9170158494924659179L;

    private final Class<T> type;
    private final String tagName;

    private final Map<String, Optional<T>> storedTags;

    public Tagger(final Class<T> type)
    {
        // This would not work properly with localized Tags.
        // So far we don't have any Enum-based tags that are localized.
        // But if they appear, this code should prevent those (throw).

        this.type = type;
        this.tagName = Validators.findTagNameIn(type);
        this.storedTags = new HashMap<>();
    }

    public Optional<T> getTag(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(this.tagName);

        if (tagValue.isPresent())
        {
            final Optional<T> value = this.storedTags.get(tagValue.get());
            if (value == null)
            {
                synchronized (this)
                {
                    if (!this.storedTags.containsKey(tagValue))
                    {
                        final Optional<T> tag = Validators.from(this.type, taggable);
                        this.storedTags.put(tagValue.get(), tag);
                    }
                }
            }

            return this.storedTags.get(tagValue.get());
        }

        return Optional.empty();
    }

}
