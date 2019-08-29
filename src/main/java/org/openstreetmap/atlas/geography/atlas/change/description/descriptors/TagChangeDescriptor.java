package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class TagChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final String key;
    private final String value;
    private final String originalValue;

    public TagChangeDescriptor(final ChangeDescriptorType changeType, final String key,
            final String value, final String originalValue)
    {
        this.changeType = changeType;
        this.key = key;
        this.value = value;
        this.originalValue = originalValue;
    }

    public TagChangeDescriptor(final ChangeDescriptorType changeType, final String key,
            final String value)
    {
        this(changeType, key, value, null);
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    public String getKey()
    {
        return this.key;
    }

    public Optional<String> getOriginalValue()
    {
        return Optional.ofNullable(this.originalValue);
    }

    public String getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        String string = "TAG(" + this.changeType + ", " + this.key + ", ";
        if (this.originalValue == null)
        {
            string += this.value + ")";
        }
        else
        {
            string += this.originalValue + " => " + this.value + ")";
        }

        return string;
    }
}
