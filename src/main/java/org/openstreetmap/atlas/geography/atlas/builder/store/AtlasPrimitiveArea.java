package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.Map;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;

/**
 * A primitive object for {@link Area}
 *
 * @author tony
 */
public class AtlasPrimitiveArea extends AtlasPrimitiveEntity
{
    private static final long serialVersionUID = -8890808695358609272L;
    private final Polygon polygon;

    public AtlasPrimitiveArea(final long identifier, final Polygon polygon,
            final Map<String, String> tags)
    {
        super(identifier, tags);
        this.polygon = polygon;
    }

    @Override
    public Rectangle bounds()
    {
        return this.polygon.bounds();
    }

    public Polygon getPolygon()
    {
        return this.polygon;
    }

    @Override
    public String toString()
    {
        return "AtlasPrimitiveArea [polygon=" + this.polygon + ", getIdentifier()="
                + getIdentifier() + ", getTags()=" + getTags() + "]";
    }
}
