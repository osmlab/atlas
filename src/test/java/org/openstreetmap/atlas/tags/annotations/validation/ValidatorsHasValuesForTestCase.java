package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test cases for hasValuesFor Validators convenience method
 *
 * @author cstaylor
 * @author mgostintsev
 */
public class ValidatorsHasValuesForTestCase
{
    @Test
    public void degenerateCase()
    {
        Assert.assertTrue(Validators.hasValuesFor(new TestTaggable(NameTag.KEY, "yes!")));
    }

    @Test
    public void keyPresentDifferentValues()
    {
        Assert.assertFalse(Validators.isOfSameType(new TestTaggable(HighwayTag.BUS_STOP),
                new TestTaggable(HighwayTag.CYCLEWAY), HighwayTag.class));
    }

    @Test
    public void keyPresentSameValue()
    {
        Assert.assertTrue(Validators.isOfSameType(new TestTaggable(WaterTag.LAKE),
                new TestTaggable(WaterTag.LAKE), WaterTag.class));
    }

    @Test
    public void oneEnumeratedTagOneNonEnumeratedTag()
    {
        Assert.assertFalse(Validators.isOfSameType(new TestTaggable(WaterwayTag.CANAL),
                new TestTaggable(LastEditTimeTag.KEY, "invalid"), WaterTag.class));
    }

    @Test
    public void oneMissingKey()
    {
        Assert.assertFalse(Validators.isOfSameType(new TestTaggable(WaterTag.CANAL),
                new TestTaggable(WaterwayTag.CANAL), WaterTag.class));
    }

    @Test
    public void oneOfOne()
    {
        Assert.assertTrue(
                Validators.hasValuesFor(new TestTaggable(NameTag.KEY, "yes!"), NameTag.class));
    }

    @Test
    public void oneOfOneEnum()
    {
        Assert.assertTrue(
                Validators.hasValuesFor(new TestTaggable(WaterTag.CANAL), WaterTag.class));
    }

    @Test
    public void testIsNotOfType()
    {
        Assert.assertTrue(Validators.isNotOfType(new TestTaggable(NaturalTag.BEACH),
                NaturalTag.class, NaturalTag.BAY));
    }

    @Test
    public void testIsOfType()
    {
        Assert.assertTrue(Validators.isOfType(new TestTaggable(NaturalTag.BEACH), NaturalTag.class,
                NaturalTag.BEACH));
    }

    @Test
    public void testManyOfManyFilter()
    {
        final Predicate<Taggable> checkMe = Validators.hasValuesFor(WaterTag.class,
                NaturalTag.class);
        Assert.assertTrue(checkMe
                .test(new TestTaggable(WaterTag.CANAL, NaturalTag.BAY, HighwayTag.BUS_STOP)));
        Assert.assertFalse(checkMe.test(new TestTaggable(HighwayTag.BUS_STOP, NaturalTag.BAY)));
    }

    @Test
    public void testOneOfOneFilter()
    {
        final Predicate<Taggable> checkMe = Validators.hasValuesFor(NameTag.class);
        Assert.assertTrue(checkMe.test(new TestTaggable(NameTag.KEY, "someName")));
        Assert.assertFalse(checkMe.test(new TestTaggable(WaterTag.CANAL)));
    }

    @Test
    public void testUnionOfFilters()
    {
        final Predicate<Taggable> unionTocheck = Validators.hasValuesFor(WaterTag.class)
                .and(Validators.hasValuesFor(NaturalTag.class));
        Assert.assertTrue(unionTocheck
                .test(new TestTaggable(WaterTag.CANAL, NaturalTag.BAY, HighwayTag.BUS_STOP)));
        Assert.assertFalse(
                unionTocheck.test(new TestTaggable(HighwayTag.BUS_STOP, NaturalTag.BAY)));
    }

    @Test
    public void threeOfThreeEnums()
    {
        Assert.assertTrue(Validators.hasValuesFor(
                new TestTaggable(WaterTag.CANAL, NaturalTag.WATER, HighwayTag.ESCAPE),
                NaturalTag.class, HighwayTag.class, WaterTag.class));
    }

    @Test
    public void threeOfTwoEnums()
    {
        Assert.assertTrue(Validators.hasValuesFor(
                new TestTaggable(WaterTag.CANAL, NaturalTag.WATER, HighwayTag.ESCAPE),
                NaturalTag.class, WaterTag.class));
    }

    @Test
    public void twoMissingKeys()
    {
        Assert.assertFalse(Validators.isOfSameType(new TestTaggable(WaterwayTag.CANAL),
                new TestTaggable(WaterwayTag.CANAL), WaterTag.class));
    }

    @Test
    public void twoNonEnumeratedTagsSameKeyDifferentValues()
    {
        Assert.assertFalse(Validators.isOfSameType(new TestTaggable(LastEditTimeTag.KEY, "01Apr19"),
                new TestTaggable(LastEditTimeTag.KEY, "06June19"), LastEditTimeTag.class));
    }

    @Test
    public void twoNonEnumeratedTagsSameKeySameValue()
    {
        Assert.assertTrue(Validators.isOfSameType(new TestTaggable(LastEditTimeTag.KEY, "06June19"),
                new TestTaggable(LastEditTimeTag.KEY, "06June19"), LastEditTimeTag.class));
    }

    @Test
    public void twoOfThreeEnums()
    {
        Assert.assertFalse(
                Validators.hasValuesFor(new TestTaggable(WaterTag.CANAL, NaturalTag.WATER),
                        NaturalTag.class, HighwayTag.class, WaterTag.class));
    }
}
