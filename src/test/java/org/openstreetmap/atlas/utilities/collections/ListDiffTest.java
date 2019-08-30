package org.openstreetmap.atlas.utilities.collections;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class ListDiffTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testListDiffApply()
    {
        final List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list2 = Arrays.asList(1, 2, 100, 4);
        final ListDiff.Diff<Integer> diff1 = ListDiff.diff(list1, list2);
        final List<Integer> result1 = ListDiff.apply(diff1, list1);
        Assert.assertEquals(list2, result1);

        final List<Integer> list3 = Arrays.asList(1, 2, 3, 4, 5, 6);
        final List<Integer> list4 = Arrays.asList(1, 2, 3, 4);
        final ListDiff.Diff<Integer> diff2 = ListDiff.diff(list3, list4);
        final List<Integer> result2 = ListDiff.apply(diff2, list3);
        Assert.assertEquals(list4, result2);

        final List<Integer> list5 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list6 = Arrays.asList(1, 2, 3, 4, 5);
        final ListDiff.Diff<Integer> diff3 = ListDiff.diff(list5, list6);
        final List<Integer> result3 = ListDiff.apply(diff3, list5);
        Assert.assertEquals(list6, result3);

        final List<Integer> list7 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list8 = Arrays.asList(1, 100, 3, 4, 5, 6);
        final ListDiff.Diff<Integer> diff4 = ListDiff.diff(list7, list8);
        final List<Integer> result4 = ListDiff.apply(diff4, list7);
        Assert.assertEquals(list8, result4);
    }

    @Test
    public void testListDiffApplyFailure()
    {
        final List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list2 = Arrays.asList(1, 2, 100, 4);
        final ListDiff.Diff<Integer> diff1 = ListDiff.diff(list1, list2);

        final List<Integer> list3 = Arrays.asList(10, 11, 12);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "Diffs can only be applied to the list from which they are generated.");
        ListDiff.apply(diff1, list3);
    }

    @Test
    public void testListDiffCompute()
    {
        final List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list2 = Arrays.asList(1, 2, 100, 4);
        final ListDiff.Diff diff1 = ListDiff.diff(list1, list2);
        final ListDiff.Diff<Integer> goldenDiff1 = new ListDiff.Diff<>(list1);
        goldenDiff1.addAction(new ListDiff.Action<>(ChangeDescriptorType.UPDATE, 2, 3, 100));
        Assert.assertEquals(goldenDiff1, diff1);

        final List<Integer> list3 = Arrays.asList(1, 2, 3, 4, 5, 6);
        final List<Integer> list4 = Arrays.asList(1, 2, 3, 4);
        final ListDiff.Diff diff2 = ListDiff.diff(list3, list4);
        final ListDiff.Diff<Integer> goldenDiff2 = new ListDiff.Diff<>(list3);
        goldenDiff2.addAction(new ListDiff.Action<>(ChangeDescriptorType.REMOVE,
                ListDiff.Action.END_INDEX, 5, null));
        goldenDiff2.addAction(new ListDiff.Action<>(ChangeDescriptorType.REMOVE,
                ListDiff.Action.END_INDEX, 6, null));
        Assert.assertEquals(goldenDiff2, diff2);

        final List<Integer> list5 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list6 = Arrays.asList(1, 2, 3, 4, 5);
        final ListDiff.Diff diff3 = ListDiff.diff(list5, list6);
        final ListDiff.Diff<Integer> goldenDiff3 = new ListDiff.Diff<>(list5);
        goldenDiff3.addAction(new ListDiff.Action<>(ChangeDescriptorType.ADD,
                ListDiff.Action.END_INDEX, null, 5));
        Assert.assertEquals(goldenDiff3, diff3);

        final List<Integer> list7 = Arrays.asList(1, 2, 3, 4, 5);
        final List<Integer> list8 = Arrays.asList(1, 2, 100, 4);
        final ListDiff.Diff diff4 = ListDiff.diff(list7, list8);
        final ListDiff.Diff<Integer> goldenDiff4 = new ListDiff.Diff<>(list7);
        goldenDiff4.addAction(new ListDiff.Action<>(ChangeDescriptorType.UPDATE, 2, 3, 100));
        goldenDiff4.addAction(new ListDiff.Action<>(ChangeDescriptorType.REMOVE,
                ListDiff.Action.END_INDEX, 5, null));
        Assert.assertEquals(goldenDiff4, diff4);

        final List<Integer> list9 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list10 = Arrays.asList(1, 2, 3, 4);
        final ListDiff.Diff diff5 = ListDiff.diff(list9, list10);
        Assert.assertTrue(diff5.isEmpty());
    }
}
