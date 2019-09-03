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
        if (left instanceof GeometryChangeDescriptor && right instanceof GeometryChangeDescriptor)
        {
            return geometryChangeCompare((GeometryChangeDescriptor) left,
                    (GeometryChangeDescriptor) right);
        }
        if (left instanceof ParentRelationChangeDescriptor
                && right instanceof ParentRelationChangeDescriptor)
        {
            return parentRelationChangeCompare((ParentRelationChangeDescriptor) left,
                    (ParentRelationChangeDescriptor) right);
        }

        throw new CoreException("Could not compare {} vs {}", left, right);
    }

    private int geometryChangeCompare(final GeometryChangeDescriptor left,
            final GeometryChangeDescriptor right)
    {
        return Integer.compare(left.getSourcePosition(), right.getSourcePosition());
    }

    private int parentRelationChangeCompare(final ParentRelationChangeDescriptor left,
            final ParentRelationChangeDescriptor right)
    {
        if (left.getChangeDescriptorType() != right.getChangeDescriptorType())
        {
            return left.getChangeDescriptorType().compareTo(right.getChangeDescriptorType());
        }
        return left.getElement().compareTo(right.getElement());
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
