package org.openstreetmap.atlas.geography.boundary;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that stores country and intersecting shards in a {@link MultiMap}, and can be constructed
 * from scratch or from file. Allows saving this mapping to file, which is useful when sharding is
 * dense or borders are complex.
 *
 * @author james-gage
 */
public final class CountryToShardListCache
{
    private static final Logger logger = LoggerFactory.getLogger(CountryToShardListCache.class);
    private static final String DELIMITER = "||";
    private final MultiMap<String, SlippyTile> countryToShards = new MultiMap<>();

    public CountryToShardListCache(final CountryBoundaryMap boundaries, final StringList countries,
            final Sharding sharding)
    {
        CountryShardListing.countryToShardList(countries, boundaries, sharding)
                .forEach((country, shardSet) ->
                {
                    shardSet.forEach(shard -> this.countryToShards.add(country,
                            SlippyTile.forName(shard.getName())));
                });
    }

    public CountryToShardListCache(final Resource file)
    {
        try
        {
            file.lines().forEach(line ->
            {
                final String[] countryAndShardList = line.split(Pattern.quote(DELIMITER));
                final String country = countryAndShardList[0];
                final String shardList = countryAndShardList[1];
                Arrays.asList(shardList.split("\\s*,\\s*")).stream().map(SlippyTile::forName)
                        .forEach(slippyTile -> this.countryToShards.add(country, slippyTile));
            });
        }
        catch (final Exception e)
        {
            logger.error("Error while reading CountryToShardListCache file!", e);
        }
    }

    public Optional<List<SlippyTile>> getShardsForCountry(final String country)
    {
        if (this.countryToShards.containsKey(country))
        {
            return Optional.of(this.countryToShards.get(country));
        }
        else
        {
            return Optional.empty();
        }
    }

    public void save(final WritableResource output)
    {
        this.countryToShards.forEach((country, shardList) ->
        {
            try (SafeBufferedWriter writer = output.writer())
            {
                final List<String> shardNames = shardList.stream()
                        .map(slippyTile -> slippyTile.getName()).collect(Collectors.toList());
                writer.writeLine(String.format("%s%s%s", country, DELIMITER, shardNames)
                        .replace("[", "").replace("]", ""));
            }
            catch (final Exception e)
            {
                logger.error("Error while writing CountryToShardListCache to file!", e);
            }
        });
    }
}
