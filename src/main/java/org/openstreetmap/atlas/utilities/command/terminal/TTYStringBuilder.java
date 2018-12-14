package org.openstreetmap.atlas.utilities.command.terminal;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A simple string building class that allows for optional TTY formatting.
 *
 * @author lcram
 */
public class TTYStringBuilder
{
    public static final int DEFAULT_LEVEL_WIDTH = 4;

    private final StringBuilder builder;
    private final boolean useColors;
    private int exactIndentWidth;
    private int levelWidth;

    public TTYStringBuilder(final boolean useColors)
    {
        this.builder = new StringBuilder();
        this.useColors = useColors;
        this.exactIndentWidth = 0;
        this.levelWidth = DEFAULT_LEVEL_WIDTH;
    }

    public TTYStringBuilder append(final Object object, final TTYAttribute... attributes)
    {
        // Append whitespace for the indent setting
        for (int i = 0; i < this.exactIndentWidth; i++)
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

    public TTYStringBuilder withExactIndentWidth(final int width)
    {
        if (width < 0)
        {
            throw new CoreException("Indent width ({}) must be > 0", width);
        }
        this.exactIndentWidth = width;
        return this;
    }

    public TTYStringBuilder withIndentLevel(final int level)
    {
        if (level < 0)
        {
            throw new CoreException("Indent level ({}) must be > 0", level);
        }
        this.exactIndentWidth = level * this.levelWidth;
        return this;
    }

    public TTYStringBuilder withLevelWidth(final int newLevelWidth)
    {
        if (newLevelWidth < 0)
        {
            throw new CoreException("Level width ({}) must be > 0", newLevelWidth);
        }
        this.levelWidth = newLevelWidth;
        return this;
    }
}
