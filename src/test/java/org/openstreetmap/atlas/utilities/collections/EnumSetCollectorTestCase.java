package org.openstreetmap.atlas.utilities.collections;

import java.util.EnumSet;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * JUnit test case for showing how we can use an EnumSetCollector with a stream of strings. This
 * includes how the ALL tag works, that we're case-insensitive, exceptions are thrown when illegal
 * values are added, and how to declare a subclass of EnumSetCollector
 *
 * @author cstaylor
 */
public class EnumSetCollectorTestCase
{
    /**
     * Example enum for testing purposes
     *
     * @author cstaylor
     */
    private enum Testing
    {
        ONE,
        TWO,
        THREE;
    }

    /**
     * This is how we must declare subclasses of EnumSetCollector, because we need the class for
     * introspection over the declared enum values.
     *
     * @author cstaylor
     */
    private static final class TestingEnumSetCollector extends EnumSetCollector<Testing>
    {
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testAllEnumSet()
    {
        final EnumSet<Testing> setItems = Stream.of("all").collect(new TestingEnumSetCollector());
        Assert.assertTrue(setItems.contains(Testing.ONE));
        Assert.assertTrue(setItems.contains(Testing.TWO));
        Assert.assertTrue(setItems.contains(Testing.THREE));
    }

    /**
     * Anonymous classes require less typing, but you _must_ remember to add those curly braces
     * after the instantiation. Just ot make sure I've marked EnumSetCollector as abstract so you
     * can't mistakenly create an instance of EnumSetCollector directly, since the type parameters
     * are associated with the subclass and not with the instance of the parameterized class.
     */
    @Test
    public void testAnonymousClassAllEnumSet()
    {
        final EnumSet<Testing> setItems = Stream.of("all").collect(new EnumSetCollector<Testing>()
        {

        });
        Assert.assertTrue(setItems.contains(Testing.ONE));
        Assert.assertTrue(setItems.contains(Testing.TWO));
        Assert.assertTrue(setItems.contains(Testing.THREE));
    }

    @Test
    public void testBadValueEnumSet()
    {
        this.exception.expect(CoreException.class);
        Stream.of("doesntexist").collect(new TestingEnumSetCollector());
    }

    @Test
    public void testPartialInOrderEnumSet()
    {
        final EnumSet<Testing> setItems = Stream.of("OnE", "ThReE")
                .collect(new TestingEnumSetCollector());
        Assert.assertTrue(setItems.contains(Testing.ONE));
        Assert.assertFalse(setItems.contains(Testing.TWO));
        Assert.assertTrue(setItems.contains(Testing.THREE));
    }

    @Test
    public void testPartialOutOfOrderEnumSet()
    {
        final EnumSet<Testing> setItems = Stream.of("ThReE", "OnE")
                .collect(new TestingEnumSetCollector());
        Assert.assertTrue(setItems.contains(Testing.ONE));
        Assert.assertFalse(setItems.contains(Testing.TWO));
        Assert.assertTrue(setItems.contains(Testing.THREE));
    }
}
