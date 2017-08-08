package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

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

    /**
     * Simple taggable implementation for testing
     *
     * @author cstaylor
     */
    private static final class TestingTaggable implements Taggable
    {
        private final String key;
        private final String value;

        TestingTaggable(final String key, final String value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public Optional<String> getTag(final String key)
        {
            if (key.equals(this.key))
            {
                return Optional.of(this.value);
            }
            return Optional.empty();
        }
    }

    @Test
    public void testExists()
    {
        final TestingTaggable testing = new TestingTaggable(EightBall.KEY, "maYbE");
        final Optional<EightBall> found = Validators.from(EightBall.class, testing);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(EightBall.MAYBE, found.get());
    }

    @Test
    public void testIllegalValue()
    {
        final TestingTaggable testing = new TestingTaggable(EightBall.KEY, "Nope");
        final Optional<EightBall> found = Validators.from(EightBall.class, testing);
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testMissingValue()
    {
        final TestingTaggable testing = new TestingTaggable(BuildingTag.KEY, "Nope");
        final Optional<EightBall> found = Validators.from(EightBall.class, testing);
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testWith()
    {
        final TestingTaggable testing = new TestingTaggable(DisusedEightBall.KEY, "maYbE");
        final Optional<EightBall> found = Validators.from(DisusedEightBall.class, EightBall.class,
                testing);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(EightBall.MAYBE, found.get());
    }
}
