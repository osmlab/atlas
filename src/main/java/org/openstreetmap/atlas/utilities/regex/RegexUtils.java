package org.openstreetmap.atlas.utilities.regex;

import java.time.Duration;
import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Utility class to avoid compiling patterns all the time.
 *
 * @author Taylor Smock
 */
public final class RegexUtils
{
    private static final int EXPIRE_DURATION = 120;
    private static final int MAXIMUM_CACHE = 1000;
    private static final LoadingCache<String, Pattern> COMPILED_REGEXES = CacheBuilder.newBuilder()
            .maximumSize(MAXIMUM_CACHE).expireAfterAccess(Duration.ofSeconds(EXPIRE_DURATION))
            .build(new CacheLoader<>()
            {
                @Override
                public Pattern load(final String key) throws Exception
                {
                    return Pattern.compile(key);
                }
            });

    /**
     * Get a specified pattern
     *
     * @param pattern
     *            The pattern that would otherwise be compiled
     * @return A compiled pattern
     */
    public static Pattern getCompiledPattern(final String pattern)
    {
        return COMPILED_REGEXES.getUnchecked(pattern);
    }

    private RegexUtils()
    {
        // Don't allow instantiation
    }
}
