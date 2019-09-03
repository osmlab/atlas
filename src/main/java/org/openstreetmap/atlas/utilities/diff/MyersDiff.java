package org.openstreetmap.atlas.utilities.diff;

import java.util.List;

/**
 * An implementation of {@link GenericDiff} based on the Myers diff algorithm. This implementation
 * is based on the C# Myers implementation found here: http://www.mathertel.de/Diff/ViewSrc.aspx.
 * 
 * @author lcram
 * @param <T>
 *            the type of the element
 */
public class MyersDiff<T> implements GenericDiff<T>
{
    @Override
    public List<DiffResult<T>> diff(final List<DiffElement<T>> left,
            final List<DiffElement<T>> right)
    {
        return null;
    }
}
