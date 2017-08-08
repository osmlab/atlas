package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * Identifier factory for way section
 *
 * @author tony
 */
public class WaySectionIdentifierFactory extends AbstractIdentifierFactory
{
    public WaySectionIdentifierFactory(final long referenceIdentifier)
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
