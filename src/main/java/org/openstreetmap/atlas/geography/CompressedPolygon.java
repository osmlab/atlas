package org.openstreetmap.atlas.geography;

/**
 * Compressed {@link Polygon}. This simply extends {@link CompressedPolyLine}
 *
 * @author matthieun
 */
public class CompressedPolygon extends CompressedPolyLine
{
    private static final long serialVersionUID = 2762361356248033855L;

    public CompressedPolygon(final byte[][] positions, final boolean[] signs)
    {
        super(positions, signs);
    }

    public CompressedPolygon(final Polygon polygon)
    {
        super(polygon);
    }

    public Polygon asPolygon()
    {
        return new Polygon(asPolyLine());
    }
}
