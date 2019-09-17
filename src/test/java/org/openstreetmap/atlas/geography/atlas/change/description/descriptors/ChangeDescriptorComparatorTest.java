package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class ChangeDescriptorComparatorTest
{
    @Test
    public void testGlobalSorting()
    {
        final TagChangeDescriptor removeTag1 = new TagChangeDescriptor(ChangeDescriptorType.REMOVE,
                "keyRemove1", null, "valueRemove1");
        final TagChangeDescriptor removeTag2 = new TagChangeDescriptor(ChangeDescriptorType.REMOVE,
                "keyRemove2", null, "valueRemove2");
        final TagChangeDescriptor addTag1 = new TagChangeDescriptor(ChangeDescriptorType.ADD,
                "keyAdd1", "valueAdd1", null);
        final TagChangeDescriptor addTag2 = new TagChangeDescriptor(ChangeDescriptorType.ADD,
                "keyAdd2", "valueAdd2", null);
        final TagChangeDescriptor updateTag1 = new TagChangeDescriptor(ChangeDescriptorType.UPDATE,
                "keyUpdate1", "valueUpdate1Updated", "valueUpdate1");
        final TagChangeDescriptor updateTag2 = new TagChangeDescriptor(ChangeDescriptorType.UPDATE,
                "keyUpdate2", "valueUpdate2Updated", "valueUpdate2");

        final List<ChangeDescriptor> goldenList = Arrays.asList(addTag1, addTag2, updateTag1,
                updateTag2, removeTag1, removeTag2);
        final List<ChangeDescriptor> actualList = new ArrayList<>(goldenList);
        Collections.shuffle(actualList);
        actualList.sort(new ChangeDescriptorComparator());

        // TODO remove print
        System.out.println(actualList);
        System.out.println(goldenList);

        Assert.assertEquals(goldenList, actualList);
    }
}
