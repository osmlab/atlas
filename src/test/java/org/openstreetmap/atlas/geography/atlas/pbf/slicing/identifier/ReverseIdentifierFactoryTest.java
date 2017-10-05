package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ReverseIdentifierFactory} test.
 *
 * @author mgostintsev
 */
public class ReverseIdentifierFactoryTest
{
    private static final ReverseIdentifierFactory IDENTIFIER_FACTORY = new ReverseIdentifierFactory();

    @Test
    public void testMinimumCountryOsmIdentifier()
    {
        final long atlasIdentifier = Long.MIN_VALUE;
        final long countryOsmIdentifier = IDENTIFIER_FACTORY
                .getCountryOsmIdentifier(atlasIdentifier);
        Assert.assertTrue(countryOsmIdentifier > 0);
    }

    @Test
    public void testMinimumOsmIdentifier()
    {
        final long atlasIdentifier = Long.MIN_VALUE;
        final long osmIdentifier = IDENTIFIER_FACTORY.getOsmIdentifier(atlasIdentifier);
        Assert.assertTrue(osmIdentifier > 0);
    }
}
