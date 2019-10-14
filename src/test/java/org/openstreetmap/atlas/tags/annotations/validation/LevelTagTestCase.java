package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * Test case for the {@link LevelTag} class
 *
 * @author sayas01
 */
public class LevelTagTestCase extends BaseTagTestCase
{
    @Test
    public void taggablesOnDifferentLevel()
    {
        final Taggable taggableOne = Taggable.with("level", "1");
        final Taggable taggableTwo = Taggable.with("level", "2");
        final Taggable taggableThree = Taggable.with("level", "0");
        final Taggable taggableFour = Taggable.with("highway", "primary");
        Assert.assertFalse(LevelTag.areOnSameLevel(taggableOne, taggableTwo));
        Assert.assertFalse(LevelTag.areOnSameLevel(taggableThree, taggableFour));
    }

    @Test
    public void taggablesOnSameLevel()
    {
        final Taggable taggableOne = Taggable.with("level", "1");
        final Taggable taggableTwo = Taggable.with("level", "1");
        Assert.assertTrue(LevelTag.areOnSameLevel(taggableOne, taggableTwo));
    }
}
