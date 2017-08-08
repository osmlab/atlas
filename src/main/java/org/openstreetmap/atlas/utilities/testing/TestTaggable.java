package org.openstreetmap.atlas.utilities.testing;

import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * Simple taggable interface used for test cases
 *
 * @author cstaylor
 */
public class TestTaggable implements Taggable
{
    private final Map<String, String> tags;

    public TestTaggable(final Enum<?>... enums)
    {
        this(Validators.toMap(enums));
    }

    public TestTaggable(final Map<String, String> tags)
    {
        this.tags = tags;
    }

    public TestTaggable(final String key, final String value)
    {
        this(Maps.hashMap(key, value));
    }

    @Override
    public Optional<String> getTag(final String key)
    {
        return Optional.ofNullable(this.tags.get(key));
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }
}
