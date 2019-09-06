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
        if (left instanceof GenericElementChangeDescriptor
                && right instanceof GenericElementChangeDescriptor)
        {
            return genericSetChangeCompare((GenericElementChangeDescriptor) left,
                    (GenericElementChangeDescriptor) right);
        }
        if (left instanceof RelationMemberChangeDescriptor
                && right instanceof RelationMemberChangeDescriptor)
        {
            return relationMemberChangeCompare((RelationMemberChangeDescriptor) left,
                    (RelationMemberChangeDescriptor) right);
        }

        throw new CoreException("Could not compare {} vs {}", left, right);
    }

    private int genericSetChangeCompare(final GenericElementChangeDescriptor left,
            final GenericElementChangeDescriptor right)
    {
        if (!left.getDescription().equals(right.getDescription()))
        {
            return left.getDescription().compareTo(right.getDescription());
        }
        if (left.getChangeDescriptorType() != right.getChangeDescriptorType())
        {
            return left.getChangeDescriptorType().compareTo(right.getChangeDescriptorType());
        }
        final Comparable leftComparable = (Comparable) left.getAfterElement();
        final Comparable rightComparable = (Comparable) right.getAfterElement();
        return leftComparable.compareTo(rightComparable);
    }

    private int geometryChangeCompare(final GeometryChangeDescriptor left,
            final GeometryChangeDescriptor right)
    {
        return Integer.compare(left.getSourcePosition(), right.getSourcePosition());
    }

    private int relationMemberChangeCompare(final RelationMemberChangeDescriptor left,
            final RelationMemberChangeDescriptor right)
    {
        if (left.getChangeDescriptorType() != right.getChangeDescriptorType())
        {
            return left.getChangeDescriptorType().compareTo(right.getChangeDescriptorType());
        }
        if (left.getItemType() != right.getItemType())
        {
            return left.getItemType().compareTo(right.getItemType());
        }
        if (left.getIdentifier() != right.getIdentifier())
        {
            return Long.compare(left.getIdentifier(), right.getIdentifier());
        }
        return left.getRole().compareTo(right.getRole());
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
