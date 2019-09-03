package org.openstreetmap.atlas.utilities.diff;

import java.util.List;

/**
 * A generic interface for line-based diff algorithms.
 * 
 * @author lcram
 * @param <T>
 *            the type of the element
 */
public interface GenericDiff<T>
{
    List<DiffResult<T>> diff(List<DiffElement<T>> left, List<DiffElement<T>> right);
}
