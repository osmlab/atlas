package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * Identifier factory for country slicing
 *
 * @author tony
 */
public class CountrySlicingIdentifierFactory extends AbstractIdentifierFactory
{
    public CountrySlicingIdentifierFactory(final long referenceIdentifier)
    {
        super(referenceIdentifier);
    }

    public CountrySlicingIdentifierFactory(final long[] referenceIdentifiers)
    {
        super(referenceIdentifiers);
    }

    @Override
    public long nextIdentifier()
    {
        incrementDelta();
        return (getReferenceIdentifier() / IDENTIFIER_SCALE + getDelta()) * IDENTIFIER_SCALE;
    }

}
