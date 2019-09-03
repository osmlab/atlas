package org.openstreetmap.atlas.utilities.diff;

/**
 * A diff element representation for a line-based diff algorithm. A diff element (i.e. a line), can
 * be anything. It could be an actual line of text (a string) or some other discrete object. For
 * example, a diff between two integer lists could be represented using {@link DiffElement}s
 * parametrized using {@link Integer}, where each integer in the list corresponds to a "line".
 * 
 * @author lcram
 * @param <T>
 *            the type of the element
 */
public class DiffElement<T>
{
    private final int lineNumber;
    private final T line;

    public DiffElement(final int lineNumber, final T line)
    {
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public T getLine()
    {
        return this.line;
    }

    public int getLineNumber()
    {
        return this.lineNumber;
    }
}
