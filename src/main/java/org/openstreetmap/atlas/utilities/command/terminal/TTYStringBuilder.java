package org.openstreetmap.atlas.utilities.command.terminal;

import java.util.ArrayDeque;
import java.util.Deque;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A simple string building class that allows for optional TTY formatting and output
 * pretty-fication.
 *
 * @author lcram
 */
public class TTYStringBuilder
{
    public static final int DEFAULT_LEVEL_WIDTH = 4;

    private final StringBuilder builder;
    private final boolean useColors;
    private final Deque<Integer> exactIndentWidthStack;
    private int levelWidth;

    public TTYStringBuilder(final boolean useColors)
    {
        this.builder = new StringBuilder();
        this.useColors = useColors;
        this.exactIndentWidthStack = new ArrayDeque<>();
        this.exactIndentWidthStack.push(0);
        this.levelWidth = DEFAULT_LEVEL_WIDTH;
    }

    public TTYStringBuilder append(final Object object, final TTYAttribute... attributes)
    {
        // Append whitespace for the indent setting
        for (int i = 0; i < this.exactIndentWidthStack.peek(); i++)
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

    public TTYStringBuilder clearIndentationStack()
    {
        this.exactIndentWidthStack.clear();
        this.exactIndentWidthStack.push(0);
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

    public TTYStringBuilder popIndentation()
    {
        if (this.exactIndentWidthStack.size() == 1)
        {
            throw new CoreException("Cannot pop default indention off the stack");
        }
        this.exactIndentWidthStack.pop();
        return this;
    }

    public TTYStringBuilder pushExactIndentWidth(final int width)
    {
        if (width < 0)
        {
            throw new CoreException("Indent width ({}) must be >= 0", width);
        }
        this.exactIndentWidthStack.push(width);
        return this;
    }

    public TTYStringBuilder pushIndentLevel(final int level)
    {
        if (level < 0)
        {
            throw new CoreException("Indent level ({}) must be >= 0", level);
        }
        this.exactIndentWidthStack.push(level * this.levelWidth);
        return this;
    }

    @Override
    public String toString()
    {
        return this.builder.toString();
    }

    public TTYStringBuilder withLevelWidth(final int newLevelWidth)
    {
        if (newLevelWidth < 0)
        {
            throw new CoreException("Level width ({}) must be >= 0", newLevelWidth);
        }
        this.levelWidth = newLevelWidth;
        return this;
    }
}
