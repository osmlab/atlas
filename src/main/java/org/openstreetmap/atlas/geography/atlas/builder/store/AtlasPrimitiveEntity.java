package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * A primitive object for {@link AtlasEntity}
 *
 * @author tony
 * @author Sid
 */
public abstract class AtlasPrimitiveEntity implements Serializable, Taggable, Located
{
    private static final long serialVersionUID = -4372740269485938585L;
    private final long identifier;
    private final Map<String, String> tags;

    public AtlasPrimitiveEntity(final long identifier, final Map<String, String> tags)
    {
        this.identifier = identifier;
        this.tags = tags;
    }

    @Override
    public boolean equals(final Object other)
    {
        return other != null && other instanceof AtlasPrimitiveEntity
                && this.getIdentifier() == ((AtlasPrimitiveEntity) other).getIdentifier();
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Optional<String> getTag(final String key)
    {
        return Optional.ofNullable(getTags().get(key));
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.identifier);
    }
}
