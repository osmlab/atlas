package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.Comparator;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;

/**
 * A {@link Comparator} for {@link ChangeDescriptor}s, which defines an ordering. This is useful
 * when printing {@link ChangeDescription}s, so the display can show a consistent element ordering
 * 
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
        if (!left.getName().equals(right.getName()))
        {
            return left.getName().compareTo(right.getName());
        }
        if (left.getChangeDescriptorType() != right.getChangeDescriptorType())
        {
            return left.getChangeDescriptorType().compareTo(right.getChangeDescriptorType());

        }
        final Comparable leftBeforeComparable = (Comparable) left.getBeforeElement();
        final Comparable rightBeforeComparable = (Comparable) right.getBeforeElement();
        if (leftBeforeComparable != null && rightBeforeComparable != null
                && !leftBeforeComparable.equals(rightBeforeComparable))
        {
            return leftBeforeComparable.compareTo(rightBeforeComparable);
        }
        final Comparable leftAfterComparable = (Comparable) left.getAfterElement();
        final Comparable rightAfterComparable = (Comparable) right.getAfterElement();
        if (leftAfterComparable != null && rightAfterComparable != null
                && !leftAfterComparable.equals(rightAfterComparable))
        {
            return leftAfterComparable.compareTo(rightAfterComparable);
        }
        else
        {
            /*
             * If we start seeing this message in production, then that means either this comparison
             * logic or ChangeDescriptor generation is dubious. But based on the way
             * ChangeDescriptors are generated, we really should never see this.
             */
            throw new CoreException("No comparable criteria for {} vs {}", left, right);
        }
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
