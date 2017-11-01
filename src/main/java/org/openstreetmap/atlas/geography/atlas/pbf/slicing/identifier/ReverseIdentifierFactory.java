package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * @author matthieun
 */
public class ReverseIdentifierFactory
{
    public long getCountryCode(final long countryCodeAndWaySectionedIdentifier)
    {
        return countryCodeAndWaySectionedIdentifier / AbstractIdentifierFactory.IDENTIFIER_SCALE
                % AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }

    public long getCountryOsmIdentifier(final long countryCodeAndWaySectionedIdentifier)
    {
        return Math.abs(
                countryCodeAndWaySectionedIdentifier / AbstractIdentifierFactory.IDENTIFIER_SCALE);
    }

    public long getOsmIdentifier(final long countryCodeAndWaySectionedIdentifier)
    {
        return Math.abs(
                countryCodeAndWaySectionedIdentifier / (AbstractIdentifierFactory.IDENTIFIER_SCALE
                        * AbstractIdentifierFactory.IDENTIFIER_SCALE));
    }

    public long getStartIdentifier(final long countryOsmIdentifier)
    {
        return countryOsmIdentifier * AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }

    public long getStartIdentifier(final long osmIdentifier, final long countryCode)
    {
        return (osmIdentifier * AbstractIdentifierFactory.IDENTIFIER_SCALE + countryCode)
                * AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }

    public long getWaySectionIndex(final long countryCodeAndWaySectionedIdentifier)
    {
        return countryCodeAndWaySectionedIdentifier % AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }
}
