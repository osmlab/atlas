package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * Identifier factory for points (mainly used for synthetic boundary nodes)
 *
 * @author samg
 */
public class PointIdentifierFactory extends AbstractIdentifierFactory
{
    public PointIdentifierFactory(final long referenceIdentifier)
    {
        super(referenceIdentifier);
    }

    @Override
    public long nextIdentifier()
    {
        incrementDelta();
        return getReferenceIdentifier() + getDelta();
    }
}
