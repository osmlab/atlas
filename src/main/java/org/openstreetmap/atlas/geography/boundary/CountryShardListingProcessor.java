package org.openstreetmap.atlas.geography.boundary;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker that acts like both producer and consumer. Takes a {@link CountryBoundary} from work queue
 * and processes it.
 *
 * @author mkalender
 */
public class CountryShardListingProcessor implements Runnable
{
    private static final Logger logger = LoggerFactory
            .getLogger(CountryShardListingProcessor.class);
    private final BlockingQueue<CountryBoundary> queue;
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
    CountryShardListingProcessor(final BlockingQueue<CountryBoundary> queue,
            final Sharding sharding, final CountryBoundaryMap boundaryMap,
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
                final CountryBoundary itemToProcess = this.queue.poll();

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

    private void process(final CountryBoundary item)
    {
        this.sharding.shards(item.getBoundary().bounds()).forEach(shard ->
        {
            final String country = item.getCountryName();

            // Skip a shard if
            // - there is no corresponding grid for given country
            // - or given country boundary doesn't overlap with shard bounds
            final Rectangle shardBounds = shard.bounds();
            if (this.boundaryMap.countryCodesOverlappingWith(shardBounds).contains(country)
                    && item.getBoundary().overlaps(shardBounds))
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
