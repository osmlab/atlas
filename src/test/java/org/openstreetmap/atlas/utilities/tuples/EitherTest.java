package org.openstreetmap.atlas.utilities.tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the functionality of the {@link Either} class
 *
 * @author cuthbertm
 */
public class EitherTest
{
    @Test
    public void leftTest()
    {
        final Either<String, Integer> either = Either.left("Test");
        either.apply(left -> Assert.assertTrue(true), right -> Assert.assertTrue(false));
    }

    @Test
    public void mappingTest()
    {
        final List<Either<String, Integer>> eitherList = new ArrayList<>();
        new Random().ints(25, 0, 2).forEach(value ->
        {
            if (value == 0)
            {
                eitherList.add(Either.left("Test"));
            }
            else
            {
                eitherList.add(Either.right(54));
            }
        });

        eitherList.stream().filter(Either::isLeft).forEach(either -> either
                .apply(left -> Assert.assertTrue(true), right -> Assert.assertTrue(false)));
        eitherList.stream().filter(Either::isRight).forEach(either -> either
                .apply(left -> Assert.assertTrue(false), right -> Assert.assertTrue(true)));
    }

    @Test
    public void rightTest()
    {
        final Either<String, Integer> either = Either.right(54);
        either.apply(left -> Assert.assertTrue(false), right -> Assert.assertTrue(true));
    }
}
