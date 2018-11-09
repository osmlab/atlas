package org.openstreetmap.atlas.utilities.command.output;

/**
 * A simple string building class that allows for optional TTY formatting.
 *
 * @author lcram
 */
public class TTYStringBuilder
{
    private final StringBuilder builder;
    private final boolean useColors;

    public TTYStringBuilder(final boolean useColors)
    {
        this.builder = new StringBuilder();
        this.useColors = useColors;
    }

    public TTYStringBuilder append(final Object object, final TTYAttribute... attributes)
    {
        if (this.useColors)
        {
            for (final TTYAttribute attribute : attributes)
            {
                this.builder.append(attribute.getANSISequence());
            }
        }

        this.builder.append(String.valueOf(object));

        // If an attribute was supplied, we need to reset the TTY
        if (this.useColors && attributes.length > 0)
        {
            this.builder.append(TTYAttribute.RESET.getANSISequence());
        }

        return this;
    }

    public TTYStringBuilder append(final String string, final TTYAttribute... attributes)
    {
        if (this.useColors)
        {
            for (final TTYAttribute attribute : attributes)
            {
                this.builder.append(attribute.getANSISequence());
            }
        }

        this.builder.append(string);

        // If an attribute was supplied, we need to reset the TTY
        if (this.useColors && attributes.length > 0)
        {
            this.builder.append(TTYAttribute.RESET.getANSISequence());
        }

        return this;
    }

    @Override
    public String toString()
    {
        return this.builder.toString();
    }
}
