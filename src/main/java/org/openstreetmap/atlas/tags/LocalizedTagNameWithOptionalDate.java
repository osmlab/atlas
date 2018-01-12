package org.openstreetmap.atlas.tags;

import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.utilities.collections.StringList;

import com.google.common.base.Joiner;

/**
 * Some tags in OSM have optional languages and even date ranges.
 * <p>
 * This class is immutable, so it's threadsafe
 *
 * @author cstaylor
 */
public class LocalizedTagNameWithOptionalDate
{
    private final String name;

    private Optional<IsoLanguage> language = Optional.empty();

    // For now, we just leave this be. In the future we may parse and use this portion of the name
    private Optional<String> untouchedDateRange = Optional.empty();

    public LocalizedTagNameWithOptionalDate(final String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("key can't be null");
        }

        final StringList list = StringList.split(key, ":");
        // This is the count of items in the string list that are part of the name and not optional
        // language or date range fields
        int nameBlocks = list.size();
        if (nameBlocks > 1)
        {
            // Check if we have a '-' character in the last item
            // This would make it a date range
            if (list.get(nameBlocks - 1).contains("-"))
            {
                // We don't need the date range in the name
                nameBlocks--;
                this.untouchedDateRange = Optional.of(list.get(nameBlocks));
            }

            // Check if we have the language portion at the end: it's possible nameBlocks is now 1,
            // so we need to check it again
            if (nameBlocks > 1)
            {
                this.language = IsoLanguage.forLanguageCode(list.get(nameBlocks - 1));
                if (this.language.isPresent())
                {
                    nameBlocks--;
                }
            }
        }
        // Thank you streams!
        this.name = Joiner.on(":")
                .join(list.stream().limit(nameBlocks).collect(Collectors.toList()));
    }

    public Optional<String> getDateRange()
    {
        return this.untouchedDateRange;
    }

    public Optional<IsoLanguage> getLanguage()
    {
        return this.language;
    }

    public String getName()
    {
        return this.name;
    }
}
