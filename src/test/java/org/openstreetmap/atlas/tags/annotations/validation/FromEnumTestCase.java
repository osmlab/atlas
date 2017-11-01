package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test case for verifying reflections-based enum value parsing
 *
 * @author cstaylor
 */
public class FromEnumTestCase
{
    /**
     * Private tag used for with=enum testing
     *
     * @author cstaylor
     */
    @Tag(with = { EightBall.class })
    public interface DisusedEightBall
    {
        @TagKey
        String KEY = "disused:eightball";
    }

    /**
     * Private tag used for test case
     *
     * @author cstaylor
     */
    @Tag
    public enum EightBall
    {
        YES,
        NO,
        MAYBE;

        @TagKey
        public static final String KEY = "magic-eight-ball";
    }

    @Test
    public void testExists()
    {
        final TestTaggable testing = new TestTaggable(EightBall.KEY, "maYbE");
        final Optional<EightBall> found = Validators.from(EightBall.class, testing);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(EightBall.MAYBE, found.get());
    }

    @Test
    public void testIllegalValue()
    {
        final TestTaggable testing = new TestTaggable(EightBall.KEY, "Nope");
        final Optional<EightBall> found = Validators.from(EightBall.class, testing);
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testMissingValue()
    {
        final TestTaggable testing = new TestTaggable(BuildingTag.KEY, "Nope");
        final Optional<EightBall> found = Validators.from(EightBall.class, testing);
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testWith()
    {
        final TestTaggable testing = new TestTaggable(DisusedEightBall.KEY, "maYbE");
        final Optional<EightBall> found = Validators.from(DisusedEightBall.class, EightBall.class,
                testing);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(EightBall.MAYBE, found.get());
    }
}
