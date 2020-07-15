package org.openstreetmap.atlas.utilities.conversion;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.time.Time;

/**
 * @author lcram
 */
public class StringToPredicateConverterTest
{
    @Rule
    public StringToPredicateConverterTestRule rule = new StringToPredicateConverterTestRule();

    @Test
    public void testConversion()
    {
        final Predicate<String> predicate1 = new StringToPredicateConverter<String>()
                .convert("e.equals(\"foo\")");
        final Predicate<Map<String, String>> predicate2 = new StringToPredicateConverter<Map<String, String>>()
                .convert("e.containsKey(\"foo\")");
        final Predicate<Integer> predicate3 = new StringToPredicateConverter<Integer>()
                .convert("e == 3");
        final Predicate<AtlasEntity> predicate4 = new StringToPredicateConverter<AtlasEntity>()
                .convert("e.getTags().containsKey(\"mat\")");
        final Predicate<Integer> predicate5 = new StringToPredicateConverter<Integer>()
                .withAddedStarImportPackages("org.openstreetmap.atlas.utilities.random")
                .convert("e.intValue() == RandomTagsSupplier.randomTags(5).size()");
        final Predicate<Integer> predicate6 = new StringToPredicateConverter<Integer>()
                .withAddedStarImportPackages(
                        Arrays.asList("org.openstreetmap.atlas.utilities.random"))
                .convert("e.intValue() == RandomTagsSupplier.randomTags(5).size()");

        Assert.assertTrue(predicate1.test("foo"));
        Assert.assertFalse(predicate1.test("bar"));

        Assert.assertTrue(predicate2.test(Maps.hashMap("foo", "bar", "baz", "bat")));
        Assert.assertFalse(predicate2.test(Maps.hashMap("baz", "bat", "bar", "mat")));

        Assert.assertTrue(predicate3.test(3));
        Assert.assertFalse(predicate3.test(10));

        Assert.assertTrue(predicate4.test(this.rule.getAtlas().point(3)));
        Assert.assertFalse(predicate4.test(this.rule.getAtlas().point(1)));

        Assert.assertTrue(predicate5.test(5));
        Assert.assertTrue(predicate6.test(5));
    }

    @Test
    public void testEfficiency()
    {
        final Predicate<String> predicate1 = new StringToPredicateConverter<String>()
                .convert("e.equals(\"foo\")");
        final Time start = Time.now();
        final int iterations = 100000;
        for (int i = 0; i < iterations; i++)
        {
            Assert.assertTrue(predicate1.test("foo"));
        }
        System.out.println("(1) " + iterations + " iterations took: "
                + start.elapsedSince().asMilliseconds() + " ms");

        // TODO implement a convert2 that uses old-school way
        final Predicate<String> predicate2 = new StringToPredicateConverter<String>()
                .convertUnsafe("e.equals(\"foo\")");
        final Time start2 = Time.now();
        for (int i = 0; i < iterations; i++)
        {
            Assert.assertTrue(predicate2.test("foo"));
        }
        System.out.println("(2) " + iterations + " iterations took: "
                + start2.elapsedSince().asMilliseconds() + " ms");
    }
}
