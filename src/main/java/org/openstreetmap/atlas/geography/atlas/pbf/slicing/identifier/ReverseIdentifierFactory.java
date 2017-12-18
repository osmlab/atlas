package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * Common methods to extract information from an Atlas identifier. Atlas identifier is defined as
 * {OSM identifier}{3 digits for country slicing id}{3 digits for way-section id}.
 *
 * @author matthieun
 */
public class ReverseIdentifierFactory
{
    /**
     * Returns the country code for the given identifier. Example: 222222001003 returns 1.
     *
     * @param countryCodeAndWaySectionedIdentifier
     *            The full atlas identifier
     * @return the country code for given identifier
     */
    public long getCountryCode(final long countryCodeAndWaySectionedIdentifier)
    {
        return countryCodeAndWaySectionedIdentifier / AbstractIdentifierFactory.IDENTIFIER_SCALE
                % AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }

    /**
     * Returns the OSM identifier and country portion for the given identifier. This truncates the
     * identifier to get rid of the way-sectioning piece. Example: 222222001003 returns 222222001.
     *
     * @param countryCodeAndWaySectionedIdentifier
     *            The full atlas identifier
     * @return the identifier without the way-section portion for given identifier
     */
    public long getCountryOsmIdentifier(final long countryCodeAndWaySectionedIdentifier)
    {
        return Math.abs(
                countryCodeAndWaySectionedIdentifier / AbstractIdentifierFactory.IDENTIFIER_SCALE);
    }

    /**
     * Returns the OSM identifier padded by the 6 digits allocated for country-slicing and
     * way-sectioning. Example: 222222001003 returns 222222000000.
     *
     * @param countryCodeAndWaySectionedIdentifier
     *            The full atlas identifier
     * @return the OSM identifier padded by the 6 digits allocated for country-slicing and
     *         way-sectioning
     */
    public long getFirstAtlasIdentifier(final long countryCodeAndWaySectionedIdentifier)
    {
        return Math.abs(getOsmIdentifier(countryCodeAndWaySectionedIdentifier)
                * AbstractIdentifierFactory.IDENTIFIER_SCALE
                * AbstractIdentifierFactory.IDENTIFIER_SCALE);
    }

    /**
     * Returns the OSM identifier for the given identifier. This truncates the identifier to get rid
     * of the country slicing and way-sectioning pieces. Example: 222222001003 returns 222222.
     *
     * @param countryCodeAndWaySectionedIdentifier
     *            The full atlas identifier
     * @return the OSM identifier for the given identifier
     */
    public long getOsmIdentifier(final long countryCodeAndWaySectionedIdentifier)
    {
        return Math.abs(
                countryCodeAndWaySectionedIdentifier / (AbstractIdentifierFactory.IDENTIFIER_SCALE
                        * AbstractIdentifierFactory.IDENTIFIER_SCALE));
    }

    /**
     * Given the identifier that has the OSM identifier and country slicing portion, this will
     * return the first complete Atlas identifier, including the way-section index. Example:
     * 222222001 returns 222222001000.
     *
     * @param countryOsmIdentifier
     *            The identifier with OSM identifier and country-slicing pieces
     * @return the first complete Atlas identifier, including the way-section index
     */
    public long getStartIdentifier(final long countryOsmIdentifier)
    {
        return countryOsmIdentifier * AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }

    /**
     * Similar to {@link #getStartIdentifier(long)}, this takes the OSM identifier and the country
     * code as two separate inputs and returns the first complete Atlas identifier, including the
     * way-section index. Example: 222222 and 001 returns 222222001000.
     *
     * @param osmIdentifier
     *            The OSM identifier
     * @param countryCode
     *            The country-code identifier
     * @return the first complete Atlas identifier, including the way-section index
     */
    public long getStartIdentifier(final long osmIdentifier, final long countryCode)
    {
        return (osmIdentifier * AbstractIdentifierFactory.IDENTIFIER_SCALE + countryCode)
                * AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }

    /**
     * Returns the way-section index for the given identifier. Example: 222222001003 returns 3.
     *
     * @param countryCodeAndWaySectionedIdentifier
     *            The full atlas identifier
     * @return the way-section index for given identifier
     */
    public long getWaySectionIndex(final long countryCodeAndWaySectionedIdentifier)
    {
        return countryCodeAndWaySectionedIdentifier % AbstractIdentifierFactory.IDENTIFIER_SCALE;
    }
}
