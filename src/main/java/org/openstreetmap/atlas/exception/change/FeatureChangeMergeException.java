package org.openstreetmap.atlas.exception.change;

import java.util.ArrayList;
import java.util.List;

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
    static final int MAXIMUM_MESSAGE_SIZE = 2000;

    private final List<MergeFailureType> failureTypeTrace;

    static String truncate(final String input)
    {
        return input.substring(0, Math.min(input.length(), MAXIMUM_MESSAGE_SIZE));
    }

    public FeatureChangeMergeException(final List<MergeFailureType> failureTypeTrace,
            final String message)
    {
        super(truncate(message));
        this.failureTypeTrace = failureTypeTrace;
        if (this.failureTypeTrace == null || this.failureTypeTrace.isEmpty())
        {
            throw new CoreException("failureTypeTrace cannot be null or empty");
        }
    }

    public FeatureChangeMergeException(final List<MergeFailureType> failureTypeTrace,
            final String message, final Object... arguments)
    {
        super(truncate(message), arguments);
        this.failureTypeTrace = failureTypeTrace;
        if (this.failureTypeTrace == null || this.failureTypeTrace.isEmpty())
        {
            throw new CoreException("failureTypeTrace cannot be null or empty");
        }
    }

    public FeatureChangeMergeException(final MergeFailureType rootLevelFailure,
            final String message, final Object... arguments)
    {
        super(truncate(message), arguments);
        if (rootLevelFailure == null)
        {
            throw new CoreException("rootLevelFailure cannot be null");
        }
        this.failureTypeTrace = new ArrayList<>();
        this.failureTypeTrace.add(rootLevelFailure);
    }

    public FeatureChangeMergeException(final MergeFailureType rootLevelFailure,
            final String message)
    {
        super(truncate(message));
        if (rootLevelFailure == null)
        {
            throw new CoreException("rootLevelFailure cannot be null");
        }
        this.failureTypeTrace = new ArrayList<>();
        this.failureTypeTrace.add(rootLevelFailure);
    }

    /**
     * Return the {@link MergeFailureType} at the provided stack index. If the index is out of
     * bounds, this will return the top level failure.
     * 
     * @param index
     *            the index to check
     * @return the {@link MergeFailureType} at the provided index, or the most top level failure if
     *         the index is out of bounds
     */
    public MergeFailureType failureAtFrameIndex(final int index)
    {
        if (index >= this.failureTypeTrace.size())
        {
            return topLevelFailure();
        }
        return this.failureTypeTrace.get(index);
    }

    /**
     * Get the number of {@link MergeFailureType}s in the failure trace.
     *
     * @return the number of {@link MergeFailureType}s in the failure trace
     */
    public int failureTraceSize()
    {
        return this.failureTypeTrace.size();
    }

    public List<MergeFailureType> getMergeFailureTrace()
    {
        return new ArrayList<>(this.failureTypeTrace);
    }

    /**
     * Return the root level {@link MergeFailureType}. This is the bottom-most failure reason, and
     * will generally be the most specific failure reason available for a given
     * {@link FeatureChangeMergeException}.
     *
     * @return the root level {@link MergeFailureType}
     */
    public MergeFailureType rootLevelFailure()
    {
        return this.failureTypeTrace.get(0);
    }

    /**
     * Return the top level {@link MergeFailureType}. This is the top-most failure reason, and will
     * generally be the most general failure reason available for a given
     * {@link FeatureChangeMergeException}.
     *
     * @return the top level {@link MergeFailureType}
     */
    public MergeFailureType topLevelFailure()
    {
        return this.failureTypeTrace.get(this.failureTypeTrace.size() - 1);
    }

    /**
     * Check if the failure trace contains an exact failure sequence of {@link MergeFailureType}s,
     * in level order from root to top. E.g. suppose the failure trace list is [root: A, B, C, D,
     * top: E], and our provided subsequence is [A, B]. In this case, the method would return true.
     * Now suppose the provided subsequence is [A, C]. Now, the method will return false. This
     * method may be useful if callers want to check for and recover from a specific failure
     * sequence.
     *
     * @param subSequence
     *            the subsequence of {@link MergeFailureType}s to check
     * @return if the subsequence is present
     */
    public boolean traceContainsExactFailureSubSequence(final List<MergeFailureType> subSequence)
    {
        if (subSequence.isEmpty())
        {
            return true;
        }
        if (subSequence.size() > this.failureTypeTrace.size())
        {
            return false;
        }

        for (int i = 0; i < this.failureTypeTrace.size(); i++)
        {
            boolean foundSubSequenceThisIteration = true;
            for (int j = 0, tmpI = i; j < subSequence.size(); j++, tmpI++)
            {
                // We hit the end of the failure trace early or we found a sequence mismatch
                if (tmpI >= this.failureTypeTrace.size()
                        || subSequence.get(j) != this.failureTypeTrace.get(tmpI))
                {
                    foundSubSequenceThisIteration = false;
                    break;
                }
            }
            if (foundSubSequenceThisIteration)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if this exception trace contains a {@link MergeFailureType} that matches the given
     * type.
     *
     * @param type
     *            the {@link MergeFailureType} for which to check
     * @return true if the trace contains the provided type
     */
    public boolean traceContainsFailureType(final MergeFailureType type)
    {
        for (final MergeFailureType currentType : this.failureTypeTrace)
        {
            if (type == currentType)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the failure trace matches exact failure sequence of {@link MergeFailureType}s, in
     * level order from root to top.
     *
     * @param sequence
     *            the sequence of {@link MergeFailureType}s to check
     * @return if the sequence is matches exactly
     */
    public boolean traceMatchesExactFailureSequence(final List<MergeFailureType> sequence)
    {
        if (sequence == null)
        {
            return false;
        }
        if (sequence.size() != this.failureTypeTrace.size())
        {
            return false;
        }
        return this.failureTypeTrace.equals(sequence);
    }

    /**
     * Add a new top-level {@link MergeFailureType} to the failure trace.
     * 
     * @param type
     *            the {@link MergeFailureType} to add
     * @return the modified trace
     */
    public List<MergeFailureType> withNewTopLevelFailure(final MergeFailureType type)
    {
        final List<MergeFailureType> newList = new ArrayList<>(this.failureTypeTrace);
        newList.add(type);
        return newList;
    }
}
