package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base class for a temporary object, that keeps track of the bare minimum information required to
 * create some Atlas entity.
 *
 * @author mgostintsev
 */
public abstract class TemporaryEntity implements Serializable
{
    private static final long serialVersionUID = -7784252999177116142L;

    private final Map<String, String> tags;
    private final long identifier;

    protected TemporaryEntity(final long identifier, final Map<String, String> tags)
    {
        this.identifier = identifier;
        this.tags = tags;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other != null && this.getClass() == other.getClass())
        {
            final TemporaryEntity that = (TemporaryEntity) other;
            return this.getIdentifier() == that.getIdentifier();
        }
        return false;
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getIdentifier()).append(getClass()).hashCode();
    }

    @Override
    public abstract String toString();

    protected String tagString()
    {
        final StringBuilder builder = new StringBuilder();
        final Map<String, String> tags = getTags();
        int index = 0;
        builder.append("[Tags: ");
        for (final String key : tags.keySet())
        {
            final String value = tags.get(key);
            builder.append("[");
            builder.append(key);
            builder.append(" => ");
            builder.append(value);
            builder.append("]");
            if (index < tags.size() - 1)
            {
                builder.append(", ");
            }
            index++;
        }
        builder.append("]");
        return builder.toString();
    }
}
