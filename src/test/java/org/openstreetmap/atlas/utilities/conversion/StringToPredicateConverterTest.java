package org.openstreetmap.atlas.utilities.conversion;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class StringToPredicateConverterTest
{
    private static final Logger logger = LoggerFactory.getLogger(StringToPredicateConverterTest.class);

    @Rule
    public StringToPredicateConverterTestRule rule = new StringToPredicateConverterTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void checkSecurityForConvert()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Unable to parse");
        new StringToPredicateConverter<Integer>().convert(
                "e.intValue() == org.openstreetmap.atlas.utilities.random.RandomTagsSupplier.randomTags(5).size()");
    }

    @Test
    public void checkSecurityForConvertUnsafe()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Unable to parse");
        new StringToPredicateConverter<Integer>().convertUnsafe(
                "e.intValue() == org.openstreetmap.atlas.utilities.random.RandomTagsSupplier.randomTags(5).size()");
    }

    @Test
    public void testComplexExpression()
    {
        /*
         * This complex expression will always return true.
         */
        final String complexExpression = "Boolean val = false; if (!val) { val = true; }; val";
        final Predicate<String> predicate = new StringToPredicateConverter<String>()
                .convert(complexExpression);
        Assert.assertTrue(predicate.test("ignoredValue"));
    }

    @Test
    public void testComplexExpressionFail()
    {
        /*
         * This complex expression will always return true. However, it cannot be converted using
         * the unsafe version of the method. Complex expressions are only supported by the regular
         * converter.
         */
        final String complexExpression = "Boolean val = false; if (!val) { val = true; }; val";
        this.expectedException.expect(MultipleCompilationErrorsException.class);
        new StringToPredicateConverter<String>().convertUnsafe(complexExpression);
    }

    @Test
    public void testComplexExpressionFail2()
    {
        /*
         * Another complex expression which should fail.
         */
        final String complexExpression = "e.equals(\"foo\"); e.equals(\"foo\")";
        this.expectedException.expect(MultipleCompilationErrorsException.class);
        new StringToPredicateConverter<String>().convertUnsafe(complexExpression);
    }

    @Test
    public void testConvert()
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
                        Collections.singletonList("org.openstreetmap.atlas.utilities.random"))
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
    public void testConvertUnsafe()
    {
        final Predicate<String> predicate1 = new StringToPredicateConverter<String>()
                .convertUnsafe("e.equals(\"foo\")");
        final Predicate<Map<String, String>> predicate2 = new StringToPredicateConverter<Map<String, String>>()
                .convertUnsafe("e.containsKey(\"foo\")");
        final Predicate<Integer> predicate3 = new StringToPredicateConverter<Integer>()
                .convertUnsafe("e == 3");
        final Predicate<AtlasEntity> predicate4 = new StringToPredicateConverter<AtlasEntity>()
                .convertUnsafe("e.getTags().containsKey(\"mat\")");
        final Predicate<Integer> predicate5 = new StringToPredicateConverter<Integer>()
                .withAddedStarImportPackages("org.openstreetmap.atlas.utilities.random")
                .convertUnsafe("e.intValue() == RandomTagsSupplier.randomTags(5).size()");
        final Predicate<Integer> predicate6 = new StringToPredicateConverter<Integer>()
                .withAddedStarImportPackages(
                        Collections.singletonList("org.openstreetmap.atlas.utilities.random"))
                .convertUnsafe("e.intValue() == RandomTagsSupplier.randomTags(5).size()");

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
    public void testImportInjectionProtection1()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Invalid import");
        new StringToPredicateConverter<Integer>()
                .withAddedStarImportPackages(Collections.singletonList(
                        "org.openstreetmap.atlas.utilities.random.*;System.out.println(\"INJECTED\");import org.openstreetmap.atlas"))
                .convertUnsafe("e.intValue() == RandomTagsSupplier.randomTags(5).size()");
    }

    @Test
    public void testImportInjectionProtection2()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Invalid import");
        new StringToPredicateConverter<Integer>()
                .withAddedStarImportPackages(Collections.singletonList("System.out.println()"))
                .convertUnsafe("e.intValue() == RandomTagsSupplier.randomTags(5).size()");
    }

    private void speedTest()
    {
        final Predicate<String> predicate1 = new StringToPredicateConverter<String>()
                .convert("e.equals(\"foo\")");
        final Predicate<String> predicate1Unsafe = new StringToPredicateConverter<String>()
                .convertUnsafe("e.equals(\"foo\")");

        final int num = 1000000;

        Time start = Time.now();
        for (int i = 0; i < num; i++)
        {
            predicate1.test("foo");
        }
        logger.trace("safe runtime: " + start.elapsedSince().asMilliseconds());

        start = Time.now();
        for (int i = 0; i < num; i++)
        {
            predicate1Unsafe.test("foo");
        }
        logger.trace("unsafe runtime: " + start.elapsedSince().asMilliseconds());
    }
}
