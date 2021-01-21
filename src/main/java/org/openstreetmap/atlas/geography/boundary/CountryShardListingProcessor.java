package org.openstreetmap.atlas.geography.boundary;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker that acts like both producer and consumer. Takes a Polygon from work queue and processes
 * it.
 *
 * @author mkalender
 */
public class CountryShardListingProcessor implements Runnable
{
    private static final Logger logger = LoggerFactory
            .getLogger(CountryShardListingProcessor.class);
    private final BlockingQueue<Polygon> queue;
    private final Sharding sharding;
    private final CountryBoundaryMap boundaryMap;
    private final MultiMapWithSet<String, Shard> countryToShardMap;

    /**
     * Default constructor.
     *
     * @param queue
     *            {@link BlockingQueue} holding {@link CountryBoundary}s to process
     * @param sharding
     *            {@link Sharding} strategy to extract {@link Shard}s
     * @param boundaryMap
     *            {@link CountryBoundaryMap} to eliminate false positives
     * @param countryToShardMap
     *            {@link Map} of country names to {@link Set} of {@link Shard}s
     */
    CountryShardListingProcessor(final BlockingQueue<Polygon> queue, final Sharding sharding,
            final CountryBoundaryMap boundaryMap,
            final MultiMapWithSet<String, Shard> countryToShardMap)
    {
        this.queue = queue;
        this.sharding = sharding;
        this.boundaryMap = boundaryMap;
        this.countryToShardMap = countryToShardMap;
    }

    @Override
    public void run()
    {
        try
        {
            while (!this.queue.isEmpty())
            {
                final Polygon itemToProcess = this.queue.poll();

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

    private void process(final Polygon item)
    {
        final JtsPolygonConverter converter = new JtsPolygonConverter();
        final String country = CountryBoundaryMap.getGeometryProperty(item, ISOCountryTag.KEY);
        this.sharding.shards(converter.backwardConvert(item).bounds()).forEach(shard ->
        {

            final Rectangle shardBounds = shard.bounds();
            if (this.boundaryMap.boundaries(shardBounds).containsKey(country))
            {
                final Set<Shard> shards = this.countryToShardMap.get(country);
                synchronized (shards)
                {
                    shards.add(shard);
                }
            }
        });
    }
}
