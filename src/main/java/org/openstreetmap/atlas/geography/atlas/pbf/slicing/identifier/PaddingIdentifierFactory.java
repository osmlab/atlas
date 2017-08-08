package org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier;

/**
 * IdentifierFactory for padding only
 *
 * @author tony
 */
public final class PaddingIdentifierFactory
{
    private static final long IDENTIFIER_PADDING = 1_000_000;

    public static long pad(final long identifier)
    {
        return identifier * IDENTIFIER_PADDING;
    }

    private PaddingIdentifierFactory()
    {
    }
}
