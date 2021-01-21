package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import org.locationtech.jts.geom.Geometry;
import org.openstreetmap.atlas.tags.ISOCountryTag;

/**
 * Simple container that tracks country code and nearest neighbor values returned from a JTS
 * {@link Geometry}.
 *
 * @author mgostintsev
 */
public class CountryCodeProperties
{
    private final String iso3CountryCode;

    public CountryCodeProperties(final String iso3CountryCode)
    {
        this.iso3CountryCode = iso3CountryCode;
    }

    /**
     * @return a string to represent country code in iso_3 format. If multiple countries, they'll be
     *         separated by comma. e.g. USA,CAN
     */
    public String getIso3CountryCode()
    {
        return this.iso3CountryCode;
    }

    /**
     * @return {@code true} if the country code field contains more than 1 country
     */
    public boolean inMultipleCountries()
    {
        return this.iso3CountryCode.contains(ISOCountryTag.COUNTRY_DELIMITER);
    }
}
