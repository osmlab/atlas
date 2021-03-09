package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorComparator;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A basic description of the internal contents of a {@link FeatureChange}. A
 * {@link ChangeDescription} consists of a {@link List} of {@link ChangeDescriptor}s as well as some
 * other details (like an identifier, an {@link ItemType}, and a {@link ChangeDescriptorType}).
 *
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
            final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeType sourceFeatureChangeType)
    {
        this.identifier = identifier;
        this.itemType = itemType;
        this.descriptors = new ArrayList<>();

        if (sourceFeatureChangeType == ChangeType.ADD)
        {
            if (beforeView != null)
            {
                this.changeDescriptorType = ChangeDescriptorType.UPDATE;
            }
            else
            {
                this.changeDescriptorType = ChangeDescriptorType.ADD;
            }
        }
        else
        {
            this.changeDescriptorType = ChangeDescriptorType.REMOVE;
        }

        this.descriptors.addAll(
                new ChangeDescriptorGenerator(beforeView, afterView, this.changeDescriptorType)
                        .generate());
    }

    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeDescriptorType;
    }

    /**
     * Get a sorted copy of the underlying {@link ChangeDescriptor} list.
     *
     * @return the sorted list
     */
    public List<ChangeDescriptor> getChangeDescriptors()
    {
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

    public JsonElement toJsonElement()
    {
        final JsonObject description = new JsonObject();
        description.addProperty("type", this.changeDescriptorType.toString());
        final JsonArray descriptorArray = new JsonArray();
        for (final ChangeDescriptor descriptor : this.descriptors)
        {
            descriptorArray.add(descriptor.toJsonElement());
        }
        description.add("descriptors", descriptorArray);
        return description;
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
