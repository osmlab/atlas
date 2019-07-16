package org.openstreetmap.atlas.exception.change;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;

/**
 * A special exception for {@link FeatureChange} merge errors.
 * 
 * @author lcram
 */
public class FeatureChangeMergeException extends CoreException
{
    private static final long serialVersionUID = -3583945839922744755L;

    private final transient MergeFailureType failureType;

    public FeatureChangeMergeException(final MergeFailureType failureType, final String message)
    {
        super(message);
        this.failureType = failureType;
    }

    public FeatureChangeMergeException(final MergeFailureType failureType, final String message,
            final Object... arguments)
    {
        super(message, arguments);
        this.failureType = failureType;
    }

    public MergeFailureType getFailureType()
    {
        return this.failureType;
    }
}
