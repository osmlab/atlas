package org.openstreetmap.atlas.utilities.testing;

import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Non-threadsafe class for tracking and assigning feature ids
 *
 * @author cstaylor
 */
class FeatureIDGenerator
{
    private final NavigableSet<Long> usedIds;

    FeatureIDGenerator()
    {
        this.usedIds = new TreeSet<>();
    }

    public long nextId(final String value)
    {
        if (null == value)
        {
            throw new IllegalArgumentException("value can't be null");
        }
        Long currentValue = null;
        if (value.equalsIgnoreCase(TestAtlas.AUTO_GENERATED))
        {
            currentValue = this.usedIds.floor(Long.MAX_VALUE);
            currentValue = currentValue == null ? 1L : currentValue + 1;
        }
        else
        {
            currentValue = Long.parseLong(value);
        }
        if (this.usedIds.contains(currentValue))
        {
            throw new IllegalStateException(
                    String.format("%d has already been assigned", currentValue));
        }
        if (currentValue == Long.MAX_VALUE)
        {
            throw new IllegalStateException("No more IDs available");
        }

        this.usedIds.add(currentValue);
        return currentValue;
    }
}
