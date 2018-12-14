package org.openstreetmap.atlas.utilities.command.terminal;

/**
 * A simple string building class that allows for optional TTY formatting.
 *
 * @author lcram
 */
public class TTYStringBuilder
{
    private final StringBuilder builder;
    private final boolean useColors;
    private int indentWidth;

    public TTYStringBuilder(final boolean useColors)
    {
        this.builder = new StringBuilder();
        this.useColors = useColors;
        this.indentWidth = 0;
    }

    public TTYStringBuilder append(final Object object, final TTYAttribute... attributes)
    {
        // Append whitespace for the indent setting
        for (int i = 0; i < this.indentWidth; i++)
        {
            this.builder.append(" ");
        }

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

    /**
     * Append a newline to this builder.
     *
     * @return the updated builder
     */
    public TTYStringBuilder newline()
    {
        this.builder.append(System.getProperty("line.separator"));
        return this;
    }

    @Override
    public String toString()
    {
        return this.builder.toString();
    }

    public TTYStringBuilder withIndentWidth(final int width)
    {
        this.indentWidth = width;
        return this;
    }
}
