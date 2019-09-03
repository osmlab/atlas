package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorComparator;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author lcram
 */
public class ChangeDescription
{
    private static final ChangeDescriptorComparator COMPARATOR = new ChangeDescriptorComparator();

    private final long identifier;
    private final ItemType itemType;
    private final ChangeDescriptorType changeDescriptorType;
    private final List<ChangeDescriptor> descriptors;

    public ChangeDescription(final long identifier, final ItemType itemType,
            final ChangeDescriptorType changeDescriptorType)
    {
        this.identifier = identifier;
        this.itemType = itemType;
        this.changeDescriptorType = changeDescriptorType;
        this.descriptors = new ArrayList<>();
    }

    public void addChangeDescriptor(final ChangeDescriptor descriptor)
    {
        this.descriptors.add(descriptor);
    }

    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeDescriptorType;
    }

    public List<ChangeDescriptor> getChangeDescriptors()
    {
        this.descriptors.sort(COMPARATOR);
        return new ArrayList<>(this.descriptors);
    }

    /**
     * Get the identifier of the feature described by this {@link ChangeDescription}.
     *
     * @return the identifier
     */
    public long getIdentifier()
    {
        return this.identifier;
    }

    /**
     * Get the {@link ItemType} of the feature described by this {@link ChangeDescription}.
     *
     * @return the type
     */
    public ItemType getItemType()
    {
        return this.itemType;
    }

    @Override
    public String toString()
    {
        this.descriptors.sort(COMPARATOR);
        final StringBuilder builder = new StringBuilder();
        builder.append("ChangeDescription [");
        builder.append("\n");
        builder.append(this.changeDescriptorType);
        builder.append(" ");
        builder.append(this.itemType);
        builder.append(" ");
        builder.append(this.getIdentifier());
        builder.append("\n");

        if (this.descriptors.isEmpty())
        {
            builder.append("]");
            return builder.toString();
        }

        for (int i = 0; i < this.descriptors.size() - 1; i++)
        {
            builder.append(this.descriptors.get(i).toString());
            builder.append("\n");
        }
        builder.append(this.descriptors.get(this.descriptors.size() - 1).toString());
        builder.append("\n");
        builder.append("]");

        return builder.toString();
    }
}
