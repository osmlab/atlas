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
    private final List<ChangeDescriptor> descriptors;

    public ChangeDescription(final long identifier, final ItemType itemType)
    {
        this.identifier = identifier;
        this.itemType = itemType;
        this.descriptors = new ArrayList<>();
    }

    public void addChangeDescriptor(final ChangeDescriptor descriptor)
    {
        this.descriptors.add(descriptor);
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

    public String toMultiLineString()
    {
        return toString(true);
    }

    @Override
    public String toString()
    {
        return toString(false);
    }

    private String toString(final boolean multiLine)
    {
        this.descriptors.sort(COMPARATOR);
        final StringBuilder builder = new StringBuilder();
        builder.append("ChangeDescription [");
        if (multiLine)
        {
            builder.append("\n");
        }

        if (this.descriptors.isEmpty())
        {
            builder.append("]");
            return builder.toString();
        }

        for (int i = 0; i < this.descriptors.size() - 1; i++)
        {
            builder.append(this.descriptors.get(i).toString());
            if (multiLine)
            {
                builder.append("\n");
            }
            else
            {
                builder.append(", ");
            }
        }
        builder.append(this.descriptors.get(this.descriptors.size() - 1).toString());
        if (multiLine)
        {
            builder.append("\n");
        }
        builder.append("]");

        return builder.toString();
    }
}
