package org.openstreetmap.atlas.tags.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.tags.Taggable;

/**
 * Implementation of cache for Validators.
 * <P>
 * Used by {@link org.openstreetmap.atlas.tags.annotations.validation.Validators Validators}
 * implicitly. Can be used on its own.
 * <p>
 * CachingValidator uses {@link Tagger} for caching actual values of given Tag.
 *
 * @author gpogulsky
 */
public class CachingValidator
{
    private static CachingValidator INSTANCE = new CachingValidator();

    @SuppressWarnings("rawtypes")
    private final Map<Class, Tagger> map;

    public static CachingValidator getInstance()
    {
        return INSTANCE;
    }

    public CachingValidator()
    {
        this.map = new HashMap<>();
    }

    /**
     * Provides Enum value associated with the given Tag type for an object, if it exists.
     * <p>
     * {@link org.openstreetmap.atlas.tags.annotations.validation.Validators#from(Class, Taggable)
     * Validators.from} is using this method implicitly. This method could be used on its own in
     * place of Validators.from.
     *
     * @param <T>
     *            the type of enum tag we're parsing
     * @param tagType
     *            the enum style tag that we want a possible value from
     * @param taggable
     *            the source of tags and their values
     * @return an empty optional if the enum isn't a tag, doesn't have a key, the value isn't found
     *         in taggable, or no enum value matches (ignoring case) the tag's value
     */
    public <T extends Enum<T>> Optional<T> from(final Class<T> tagType, final Taggable taggable)
    {
        final Tagger<T> tagger = this.getTagger(tagType);
        return tagger.getTag(taggable);
    }

    private synchronized <T extends Enum<T>> Tagger<T> addTagger(final Class<T> tagType)
    {
        @SuppressWarnings("unchecked")
        Tagger<T> tagger = this.map.get(tagType);

        if (tagger == null)
        {
            tagger = new Tagger<T>(tagType);
            this.map.put(tagType, tagger);
        }

        return tagger;
    }

    private <T extends Enum<T>> Tagger<T> getTagger(final Class<T> tagType)
    {
        @SuppressWarnings("unchecked")
        Tagger<T> tagger = this.map.get(tagType);

        if (tagger == null)
        {
            tagger = this.addTagger(tagType);
        }

        return tagger;
    }
}
