package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.Comparator;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class ChangeDescriptorComparator implements Comparator<ChangeDescriptor>
{
    @Override
    public int compare(final ChangeDescriptor left, final ChangeDescriptor right)
    {
        if (left.getClass().isAssignableFrom(right.getClass()))
        {
            return complexCompare(left, right);
        }
        return left.getClass().getSimpleName().compareTo(right.getClass().getSimpleName());
    }

    private int complexCompare(final ChangeDescriptor left, final ChangeDescriptor right)
    {
        if (left instanceof TagChangeDescriptor && right instanceof TagChangeDescriptor)
        {
            return tagChangeCompare((TagChangeDescriptor) left, (TagChangeDescriptor) right);
        }

        throw new CoreException("Could not compare {} vs {}", left, right);
    }

    private int tagChangeCompare(final TagChangeDescriptor left, final TagChangeDescriptor right)
    {
        if (left.getChangeDescriptorType() != right.getChangeDescriptorType())
        {
            return left.getChangeDescriptorType().compareTo(right.getChangeDescriptorType());
        }
        return left.getKey().compareTo(right.getKey());
    }
}
