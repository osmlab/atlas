package org.openstreetmap.atlas.geography.boundary;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This builder builds a spatial index using R-tree based on JTS. Unlike
 * {@link DynamicGridIndexBuilder}, the index cells have fixed size.
 *
 * @author Yiqing Jin
 */
public class FixedGridIndexBuilder extends AbstractGridIndexBuilder
{
    private static final double BOUND_ROUNDING_IN_MICRODEGREES = 0.5;
    private static final double GRANULARITY = 0.1;
    private STRtree index;

    private final List<Polygon> boundaries;
    private final Envelope envelope;

    /**
     * @param boundaries
     *            The boundaries to build index on
     * @param envelope
     *            The {@link Envelope} that defines the indexing area, anything outside of the
     *            {@link Envelope} will be thrown away
     */
    public FixedGridIndexBuilder(final List<Polygon> boundaries, final Envelope envelope)
    {
        this.envelope = envelope;
        this.boundaries = boundaries;
        this.index = null;
    }

    /**
     * Get the boundaries used by the spatial index.
     */
    @Override
    public List<Polygon> getBoundaries()
    {
        return this.boundaries;
    }

    /**
     * Get the {@link Envelope} used by the spatial index.
     */
    @Override
    public Envelope getEnvelope()
    {
        return this.envelope;
    }

    /**
     * Get index for given boundaries. If index is not built, build it first
     *
     * @return the index built
     */
    @Override
    public STRtree getIndex()
    {
        if (this.index == null)
        {
            this.index = new STRtree();
            for (final Polygon polygon : this.boundaries)
            {
                final Envelope bound = polygon.getEnvelopeInternal();
                final Envelope workingBound = bound.intersection(this.envelope);
                workingBound.expandBy(BOUND_ROUNDING_IN_MICRODEGREES);
                final double incrementValue = GRANULARITY;
                final double minX = Math.round(workingBound.getMinX());
                final double minY = Math.round(workingBound.getMinY());
                final double maxX = Math.round(workingBound.getMaxX());
                final double maxY = Math.round(workingBound.getMaxY());
                for (double currentY = minY; currentY < maxY; currentY += incrementValue)
                {
                    for (double currentX = minX; currentX < maxX; currentX += incrementValue)
                    {
                        final Envelope box = new Envelope(currentX, currentX + incrementValue,
                                currentY, currentY + incrementValue);
                        final Polygon geoBox = buildGeoBox(currentX, currentX + incrementValue,
                                currentY, currentY + incrementValue);
                        if (geoBox.intersects(polygon))
                        {
                            this.index.insert(box, polygon);
                        }
                    }
                }
            }
        }

        return this.index;
    }
}
