package org.openstreetmap.atlas.tags.cache;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Cache for Tags of certain type. For applications that check tags on big numbers of objects, it
 * would save time to cache associations between tag names and their representative Enum values.
 *
 * @author gpogulsky
 * @author sbhalekar
 * @param <T>
 *            - type of tag Enum class
 */
public class Tagger<T extends Enum<T>> implements Serializable
{
    private static final long serialVersionUID = -9170158494924659179L;

    private final Class<T> type;
    private final String tagName;
    private final Cache<String, Optional<T>> cache;

    public Tagger(final Class<T> type)
    {
        // This would not work properly with localized Tags.
        // So far we don't have any Enum-based tags that are localized.
        // But if they appear, this code should prevent those (throw).

        this.type = type;
        this.tagName = Validators.findTagNameIn(type);
        this.cache = CacheBuilder.newBuilder().build();
    }

    public Optional<T> getTag(final Taggable taggable)
    {
        final Optional<String> possibleTagValue = taggable.getTag(this.tagName);
        if (possibleTagValue.isPresent())
        {
            final String tagValue = possibleTagValue.get();
            try
            {
                // Referenced from
                // https://github.com/google/guava/wiki/CachesExplained#from-a-callable
                // If tagValue is present in the cache then return the value; otherwise execute
                // function add the value to cache and return it
                return this.cache.get(tagValue,
                        () -> Validators.fromAnnotation(Tagger.this.type, taggable));
            }
            // this exception is thrown by the Callable in the get method of the cache. Ideally
            // we should never hit this exception
            catch (final ExecutionException e)
            {
                throw new CoreException("Error getting tag value from the cache", e);
            }
        }
        return Optional.empty();
    }
}
