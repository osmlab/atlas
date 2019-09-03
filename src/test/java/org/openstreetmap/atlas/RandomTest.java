package org.openstreetmap.atlas;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

/**
 * TODO remove
 */
public class RandomTest
{
    @Test
    public void test()
    {
        final List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5, 6);
        final List<Integer> list2 = Arrays.asList(2, 3, 100, 200, 4, 7, 8);

        Patch<Integer> patch = null;
        try
        {
            patch = DiffUtils.diff(list1, list2);
        }
        catch (final DiffException e)
        {
            e.printStackTrace();
        }

        for (final AbstractDelta<Integer> delta : patch.getDeltas())
        {
            System.out.println(delta);
        }
    }
}
