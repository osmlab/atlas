package org.openstreetmap.atlas.geography.boundary;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This builder builds a spatial index using {@link STRtree} based on JTS. Instead of equally
 * divided grid index, dynamic grid index use a 2d kd-tree like structure to generate cells
 * dynamically. This reduces the number of cells generated and allow us to generate much smaller
 * cells at boundary to improve search performance by reducing false positive hits.
 *
 * @author Yiqing Jin
 * @author mkalender
 */
public class DynamicGridIndexBuilder extends AbstractGridIndexBuilder
{
    /**
     * A class to hold units of work while building grid index.
     *
     * @author mkalender
     */
    private class GridWorkItem
    {
        private final double minX;
        private final double minY;
        private final double maxX;
        private final double maxY;
        private final Polygon polygon;

        /**
         * Default constructor
         *
         * @param minX
         *            Lower bound on X-axis
         * @param minY
         *            Lower bound on Y-axis
         * @param maxX
         *            Upper bound on X-axis
         * @param maxY
         *            Upper bound on Y-axis
         * @param polygon
         *            Boundary in {@link Polygon} format
         */
        GridWorkItem(final double minX, final double minY, final double maxX, final double maxY,
                final Polygon polygon)
        {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.polygon = polygon;
        }
    }

    /**
     * Worker that acts like both producer and consumer. Takes an {@link GridWorkItem} from work
     * queue and processes it.
     *
     * @author mkalender
     */
    private class Processor implements Runnable
    {
        private final BlockingQueue<GridWorkItem> queue;
        private final STRtree index;
        private final STRtree rawIndex;

        /**
         * Default constructor.
         *
         * @param queue
         *            {@link BlockingQueue} holding {@link GridWorkItem}s to process
         * @param index
         *            {@link STRtree} to be built
         * @param rawIndex
         *            {@link STRtree} holding raw boundaries
         */
        Processor(final BlockingQueue<GridWorkItem> queue, final STRtree index,
                final STRtree rawIndex)
        {
            this.queue = queue;
            this.index = index;
            this.rawIndex = rawIndex;
        }

        @Override
        public void run()
        {
            try
            {
                while (!this.queue.isEmpty())
                {
                    final GridWorkItem itemToProcess = this.queue.poll();

                    // Queue might have been emptied by another thread. That gives an null item.
                    // Check for null item to avoid an exception.
                    if (itemToProcess != null)
                    {
                        this.process(itemToProcess);
                    }
                }
            }
            catch (final Exception e)
            {
                logger.error("Processor failed to process.", e);
            }
        }

        @SuppressWarnings("unchecked")
        private void process(final GridWorkItem item)
        {
            final double minX = item.minX;
            final double minY = item.minY;
            final double maxX = item.maxX;
            final double maxY = item.maxY;
            final Polygon polygon = item.polygon;
            Envelope box = null;

            try
            {
                box = new Envelope(minX, maxX, minY, maxY);
                final double width = maxX - minX;
                final double height = maxY - minY;
                final Polygon geoBox = buildGeoBox(minX, maxX, minY, maxY);

                if (!geoBox.intersects(polygon))
                {
                    // We throw away non-intersected cells
                    return;
                }
                else if (Math.max(width / 2, height / 2) < GRANULARITY)
                {
                    synchronized (this.index)
                    {
                        this.index.insert(box, polygon);
                    }
                    return;
                }
                else if (CountryBoundaryMap.isSameCountry(this.rawIndex.query(box))
                        || polygon.covers(geoBox))
                {
                    // The box must be within the boundary envelope so rawIndex query should return
                    // at least the boundary itself. If query results isSameCountry returns true,
                    // the box must ONLY intersect with the country the boundary belongs, in this
                    // case we treat it same as inside cell. This is an end point, we add the box to
                    // the spatial index.
                    synchronized (this.index)
                    {
                        this.index.insert(box, polygon);
                    }
                    return;
                }

                // None of above conditions are met, we need to split the box into sub-boxes
                if (width > height)
                {
                    this.queue.add(new GridWorkItem(minX, minY, minX + width / 2, maxY, polygon));
                    this.queue.add(new GridWorkItem(minX + width / 2, minY, maxX, maxY, polygon));
                }
                else
                {
                    this.queue.add(new GridWorkItem(minX, minY, maxX, minY + height / 2, polygon));
                    this.queue.add(new GridWorkItem(minX, minY + height / 2, maxX, maxY, polygon));
                }
            }
            catch (final TopologyException e)
            {
                // Catch mostly JTS Topology exceptions
                final String countryCode = CountryBoundaryMap.getGeometryProperty(polygon,
                        ISOCountryTag.KEY);
                logger.error("Unable to build tree under box {} for country code {}.", box,
                        countryCode, e);
                // Save the current box and do not iterate down.
                synchronized (this.index)
                {
                    this.index.insert(box, polygon);
                }
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DynamicGridIndexBuilder.class);

    private static final double BOUND_ROUNDING_IN_MICRODEGREES = 0;
    private static final double GRANULARITY = 0.02;

    // RTree to hold grid boxes
    private STRtree index;

    // RTree to hold raw boundaries
    private STRtree rawIndex;

    // Boundaries to build grid index against
    private final List<Polygon> boundaries;

    // Envelope providing area to be indexed
    private final Envelope envelope;

    /**
     * @param boundaries
     *            The boundaries to build the index on. Usually the country boundaries.
     * @param envelope
     *            {@link Envelope} that defines the indexing area, anything outside of the envelope
     *            will be thrown away
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
            this.createRawIndex();
        }
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
            synchronized (this)
            {
                if (this.index == null)
                {
                    this.index = new STRtree();
                    final BlockingQueue<GridWorkItem> queue = new LinkedBlockingQueue<>();
                    this.boundaries.stream().parallel().forEach(boundary ->
                    {
                        final Envelope bound = boundary.getEnvelopeInternal();
                        Envelope workingBound = bound;
                        if (this.envelope != null)
                        {
                            workingBound = bound.intersection(this.envelope);
                        }
                        workingBound.expandBy(BOUND_ROUNDING_IN_MICRODEGREES);

                        // Add work item to the queue
                        queue.add(new GridWorkItem(workingBound.getMinX(), workingBound.getMinY(),
                                workingBound.getMaxX(), workingBound.getMaxY(), boundary));
                    });

                    // Use all available processors except one (used by main thread)
                    final int threadCount = Runtime.getRuntime().availableProcessors() - 1;
                    logger.info("Building index with {} processors (threads).", threadCount);

                    // Start the execution pool to generate grid index
                    try (Pool processPool = new Pool(threadCount, "Grid Index Builder"))
                    {
                        // Generate processors
                        IntStream.range(0, threadCount).forEach(index ->
                        {
                            processPool.queue(new Processor(queue, this.index, this.rawIndex));
                        });
                    }
                    catch (final Exception e)
                    {
                        logger.error("Generating grid index is failed.", e);
                    }
                }
            }
        }

        return this.index;
    }

    private void createRawIndex()
    {
        this.rawIndex = new STRtree();
        for (final Polygon boundary : this.boundaries)
        {
            this.rawIndex.insert(boundary.getEnvelopeInternal(), boundary);
        }
    }

}
