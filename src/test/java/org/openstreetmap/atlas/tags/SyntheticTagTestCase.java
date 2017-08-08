package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.annotations.validation.Validators.TagKeySearchResults;
import org.openstreetmap.atlas.tags.names.NameTag;

/**
 * Test case for checking if the synthetic attribute works
 *
 * @author cstaylor
 */
public class SyntheticTagTestCase
{
    @Test
    public void isNotSynthetic()
    {
        final TagKeySearchResults results = Validators.TagKeySearch.findTagKeyIn(NameTag.class)
                .orElseThrow(() -> new IllegalArgumentException("Not a tag"));
        Assert.assertFalse(results.getTag().synthetic());
    }

    @Test
    public void isSynthetic()
    {
        final TagKeySearchResults results = Validators.TagKeySearch
                .findTagKeyIn(TestSyntheticTag.class)
                .orElseThrow(() -> new IllegalArgumentException("Not a tag"));
        Assert.assertTrue(results.getTag().synthetic());
    }
}
