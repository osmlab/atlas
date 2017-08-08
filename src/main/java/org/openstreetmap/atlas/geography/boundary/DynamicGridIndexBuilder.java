package org.openstreetmap.atlas.geography.boundary;

import java.util.List;

import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This builder builds a spatial index using R-tree based on JTS. Instead of equally divided grid
 * index, dynamic grid index use a 2d kd-tree like structure to generate cells dynamically. This
 * reduces the number of cells generated and allow us to generate much smaller cells at boundary to
 * improve search performance by reducing false positive hits.
 *
 * @author Yiqing Jin
 */
public class DynamicGridIndexBuilder extends AbstractGridIndexBuilder
{
    private static final double BOUND_ROUNDING_IN_MICRODEGREES = 0;
    private static final double GRANULARITY = 0.02;
    private STRtree index;
    private STRtree rawIndex;

    private final List<Polygon> boundaries;
    private final Envelope envelope;
    private final MultiMap<String, Envelope> spatialIndexCells;

    /**
     * @param boundaries
     *            The boundaries to build the index on. Usually the country boundaries.
     * @param envelope
     *            The envelope that defines the indexing area, anything outside of the envelope will
     *            be thrown away
     * @param rawIndex
     *            The raw index of the boundary list, if null is passed in, the builder will build a
     *            new one itself.
     */
    public DynamicGridIndexBuilder(final List<Polygon> boundaries, final Envelope envelope,
            final STRtree rawIndex)
    {
        this.envelope = envelope;
        this.boundaries = boundaries;
        this.index = null;
        this.rawIndex = rawIndex;
        if (this.rawIndex == null)
        {
            createRawIndex();
        }
        this.spatialIndexCells = new MultiMap<>();
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
     * Get index for given boundaries. If index is not built, build it first.
     *
     * @return the index built
     */
    @Override
    public STRtree getIndex()
    {
        if (this.index == null)
        {
            this.index = new STRtree();
            for (final Polygon boundary : this.boundaries)
            {
                final Envelope bound = boundary.getEnvelopeInternal();
                Envelope workingBound = bound;
                if (this.envelope != null)
                {
                    workingBound = bound.intersection(this.envelope);
                }
                workingBound.expandBy(BOUND_ROUNDING_IN_MICRODEGREES);
                this.process(workingBound.getMinX(), workingBound.getMinY(), workingBound.getMaxX(),
                        workingBound.getMaxY(), boundary);
            }
        }
        return this.index;
    }

    /**
     * @return the Quad-Tree cells stored within the Spatial Index (R-Tree).
     */
    @Override
    public MultiMap<String, Envelope> getSpatialIndexCells()
    {
        return this.spatialIndexCells;
    }

    private void createRawIndex()
    {
        this.rawIndex = new STRtree();
        for (final Polygon boundary : this.boundaries)
        {
            this.rawIndex.insert(boundary.getEnvelopeInternal(), boundary);
        }
    }

    @SuppressWarnings("unchecked")
    private void process(final double minX, final double minY, final double maxX, final double maxY,
            final Polygon polygon)
    {
        final double width = maxX - minX;
        final double height = maxY - minY;
        final Envelope box = new Envelope(minX, maxX, minY, maxY);
        final Polygon geoBox = buildGeoBox(minX, maxX, minY, maxY);
        try
        {
            if (!geoBox.intersects(polygon))
            {
                // We throw away non-intersected cells
                return;
            }
            else if (Math.max(width / 2, height / 2) < GRANULARITY)
            {
                if (savingGridIndexCells())
                {
                    final String countryCode = CountryBoundaryMap.getGeometryProperty(polygon,
                            ISOCountryTag.KEY);
                    this.spatialIndexCells.add(countryCode, box);
                }
                this.index.insert(box, polygon);
                return;
            }
            else if (CountryBoundaryMap.isSameCountry(this.rawIndex.query(box))
                    || polygon.covers(geoBox))
            {
                // The box must be within the boundary envelope so rawIndex query should return at
                // least the boundary itself. If query results isSameCountry returns true, the box
                // must ONLY intersect with the country the boundary belongs, in this case we treat
                // it same as inside cell. This is an end point, we add the box to the spatial
                // index.
                if (savingGridIndexCells())
                {
                    final String countryCode = CountryBoundaryMap.getGeometryProperty(polygon,
                            ISOCountryTag.KEY);
                    this.spatialIndexCells.add(countryCode, box);
                }
                this.index.insert(box, polygon);
                return;
            }

            // None of above conditions are met, we need to split the box into sub-boxes
            if (width > height)
            {
                process(minX, minY, minX + width / 2, maxY, polygon);
                process(minX + width / 2, minY, maxX, maxY, polygon);
            }
            else
            {
                process(minX, minY, maxX, minY + height / 2, polygon);
                process(minX, minY + height / 2, maxX, maxY, polygon);
            }
        }
        catch (final TopologyException e)
        {
            // Catch mostly JTS Topology exceptions
            final String countryCode = CountryBoundaryMap.getGeometryProperty(polygon,
                    ISOCountryTag.KEY);
            logger.error("Unable to build tree under box {} for country code {}.", box, countryCode,
                    e);
            if (savingGridIndexCells())
            {
                // Save the current box and do not iterate down.
                this.index.insert(box, polygon);
            }
        }
    }

}
