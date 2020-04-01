package org.openstreetmap.atlas.geography.boundary;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that transforms a {@link CountryBoundaryMap}, {@link Sharding} and returns all the
 * {@link Shard}s that are covered by each country in a provided list.
 *
 * @author mkalender
 * @author matthieun
 */
public final class CountryShardListing
{
    private static final Logger logger = LoggerFactory.getLogger(CountryShardListing.class);

    public static MultiMapWithSet<String, Shard> countryToShardList(
            final Iterable<String> countries, final CountryBoundaryMap boundaries,
            final Sharding sharding)
    {
        // Extract country boundaries and queue them
        final BlockingQueue<CountryBoundary> queue = new LinkedBlockingQueue<>();
        final MultiMapWithSet<String, Shard> countryToShardMap = new MultiMapWithSet<>();
        countries.forEach(country ->
        {
            // Initialize country-shard map
            countryToShardMap.put(country, new HashSet<>());

            // Fetch boundaries
            final List<CountryBoundary> countryBoundaries = boundaries.countryBoundary(country);
            if (countryBoundaries == null)
            {
                logger.error("No boundaries found for {}!", country);
                return;
            }

            // Queue boundary for processing
            countryBoundaries.forEach(queue::add);
        });

        // Use all available processors except one (used by main thread)
        final int threadCount = Runtime.getRuntime().availableProcessors() - 1;
        logger.info("Generating tasks with {} processors (threads).", threadCount);

        // Start the execution pool to generate tasks
        try (Pool processPool = new Pool(threadCount, "CountryShardListing"))
        {
            // Generate processors
            IntStream.range(0, threadCount)
                    .forEach(index -> processPool.queue(new CountryShardListingProcessor(queue,
                            sharding, boundaries, countryToShardMap)));
        }
        return countryToShardMap;
    }

    private CountryShardListing()
    {
    }
}
