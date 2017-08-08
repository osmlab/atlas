package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Identifier management like create new way, node and relation identifier
 *
 * @author tony
 */
public abstract class AbstractIdentifierFactory
{
    public static final long IDENTIFIER_SCALE = 1000;

    private final long[] referenceIdentifiers;
    private int index;
    private long delta;

    public AbstractIdentifierFactory(final long referenceIdentifier)
    {
        this(new long[] { referenceIdentifier });
    }

    public AbstractIdentifierFactory(final long[] referenceIdentifierArray)
    {
        this.referenceIdentifiers = referenceIdentifierArray;
        this.delta = 0;
        this.index = 0;
    }

    public long getDelta()
    {
        return this.delta;
    }

    public long getReferenceIdentifier()
    {
        return this.referenceIdentifiers[this.index];
    }

    public boolean hasMore()
    {
        return this.delta < IDENTIFIER_SCALE - 1
                || this.index < this.referenceIdentifiers.length - 1;
    }

    public abstract long nextIdentifier();

    protected void incrementDelta()
    {
        this.delta++;

        if (this.delta >= IDENTIFIER_SCALE)
        {
            if (this.index < this.referenceIdentifiers.length - 1)
            {
                this.index++;
                this.delta = 1;
            }
            else
            {
                throw new CoreException("Entity {} has been split into more than 999 pieces",
                        this.referenceIdentifiers);
            }
        }
    }
}
