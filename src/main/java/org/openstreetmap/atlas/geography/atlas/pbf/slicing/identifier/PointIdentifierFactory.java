package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * Identifier factory for points (mainly used for synthetic boundary nodes)
 *
 * @author samg
 */
public class PointIdentifierFactory extends AbstractIdentifierFactory
{
    private static final long IDENFITIER_SCALE = 1000000;

    public PointIdentifierFactory(final long referenceIdentifier)
    {
        super(referenceIdentifier, IDENFITIER_SCALE);
    }

    @Override
    public long nextIdentifier()
    {
        incrementDelta();
        return getReferenceIdentifier() + getDelta();
    }
}
