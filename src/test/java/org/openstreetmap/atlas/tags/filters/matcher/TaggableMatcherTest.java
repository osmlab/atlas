package org.openstreetmap.atlas.tags.filters.matcher;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class TaggableMatcherTest
{
    @Test
    public void test()
    {
        final Taggable taggable = new Taggable()
        {
            final Map<String, String> tags = Maps.hashMap("foo", "bar2", "baz", "bat", "hello",
                    "world", "name", "lucas");

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
        };

        Assert.assertTrue(
                TaggableMatcher.from("foo=(bar | bar2) & hello=world & name").test(taggable));
    }
}
