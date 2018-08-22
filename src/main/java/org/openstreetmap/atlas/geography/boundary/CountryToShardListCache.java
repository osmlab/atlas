package org.openstreetmap.atlas.geography.boundary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Country to shard list that is read from a .txt file. Useful for when using dense shard trees to
 * reduce overhead of recalculating country/shard intersection.
 *
 * @author jamesgage
 */
public final class CountryToShardListCache
{
    private static final Logger logger = LoggerFactory.getLogger(CountryToShardListCache.class);
    private final HashMap<String, List<SlippyTile>> countryToShards = new HashMap<>();

    public static CountryToShardListCache create(final Resource file)
    {
        return new CountryToShardListCache(file);
    }

    private CountryToShardListCache(final Resource file)
    {
        try
        {
            file.lines().forEach(line ->
            {
                final String[] countryAndShardList = line.split(Pattern.quote("||"));
                final String country = countryAndShardList[0];
                final String shardList = countryAndShardList[1];
                final List<SlippyTile> shards = new ArrayList<>(
                        Arrays.asList(shardList.split("\\s*,\\s*"))).stream()
                                .map(string -> SlippyTile.forName(string))
                                .collect(Collectors.toList());
                this.countryToShards.put(country, shards);
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
}
