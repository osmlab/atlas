package org.openstreetmap.atlas.geography;

/**
 * Compressed {@link Polygon}
 *
 * @author matthieun
 */
public class StringCompressedPolygon extends StringCompressedPolyLine
{
    private static final long serialVersionUID = 7681617657249431319L;

    public StringCompressedPolygon(final byte[] encoding)
    {
        super(encoding);
    }

    public StringCompressedPolygon(final Polygon polygon)
    {
        super(polygon);
    }

    public Polygon asPolygon()
    {
        return new Polygon(asPolyLine());
    }
}
