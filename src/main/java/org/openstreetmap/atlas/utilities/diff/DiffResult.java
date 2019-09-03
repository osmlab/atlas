package org.openstreetmap.atlas.utilities.diff;

import java.util.List;

import org.openstreetmap.atlas.geography.atlas.change.ChangeType;

/**
 * A representation of a diff result. A {@link DiffResult} is either an ADD, REMOVE, or a null (no
 * change). It contains the actual element being added, removed, or unchanged. It also contains line
 * numbers for the before/after views. A line-based diff algorithm will return an ordered sequence
 * (a {@link List}) of {@link DiffResult}s.
 * 
 * @author lcram
 * @param <T>
 *            the type of the element
 */
public class DiffResult<T>
{
    private final ChangeType changeType;
    private final int lineNumberLeft;
    private final int lineNumberRight;
    private final T element;

    public DiffResult(final ChangeType changeType, final int lineNumberLeft,
            final int lineNumberRight, final T element)
    {
        this.changeType = changeType;
        this.lineNumberLeft = lineNumberLeft;
        this.lineNumberRight = lineNumberRight;
        this.element = element;
    }

    public ChangeType getChangeType()
    {
        return this.changeType;
    }

    public T getElement()
    {
        return this.element;
    }

    public int getLineNumberLeft()
    {
        return this.lineNumberLeft;
    }

    public int getLineNumberRight()
    {
        return this.lineNumberRight;
    }
}
