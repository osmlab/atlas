package org.openstreetmap.atlas.geography.boundary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that stores country and intersecting shards in a {@link MultiMap}, and can be constructed
 * from scratch or from file. Allows saving this mapping to file, which is useful when sharding is
 * dense or borders are complex.
 *
 * @author james-gage
 */
public final class CountryToShardListCache extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(CountryToShardListCache.class);
    private static final Switch<CountryBoundaryMap> BOUNDARIES = new Switch<>("boundaries",
            "The country boundaries.", value -> initializeCountryBoundaryMap(value),
            Optionality.REQUIRED);
    private static final Switch<StringList> COUNTRIES = new Switch<>("countries",
            "CSV list of the country iso3 codes", value -> StringList.split(value, ","),
            Optionality.OPTIONAL);
    private static final Switch<Sharding> SHARDING = new Switch<>("sharding",
            "File containing the sharding definition (Works with files only)", Sharding::forString,
            Optionality.REQUIRED);
    private static final Switch<File> OUTPUT = new Switch<>("output", "The output file", File::new,
            Optionality.REQUIRED);
    private static final String DELIMITER = "||";
    private static final StringToShardConverter CONVERTER = new StringToShardConverter();
    private final MultiMap<String, Shard> countryToShards = new MultiMap<>();

    public static void main(final String[] args)
    {
        new CountryToShardListCache().run(args);
    }

    private static CountryBoundaryMap initializeCountryBoundaryMap(final String value)
    {
        final Time start = Time.now();
        logger.info("Loading boundaries");
        final CountryBoundaryMap result = CountryBoundaryMap.fromPlainText(new File(value));
        logger.info("Loaded boundaries in {}", start.elapsedSince());
        return result;
    }

    public CountryToShardListCache(final CountryBoundaryMap boundaries, final Sharding sharding)
    {
        this(boundaries, new StringList(boundaries.allCountryNames()), sharding);
    }

    public CountryToShardListCache(final CountryBoundaryMap boundaries, final StringList countries,
            final Sharding sharding)
    {
        CountryShardListing.countryToShardList(countries, boundaries, sharding)
                .forEach((country, shardSet) ->
                {
                    shardSet.forEach(shard -> this.countryToShards.add(country,
                            CONVERTER.convert(shard.getName())));
                });
    }

    public CountryToShardListCache(final Resource resource)
    {
        try
        {
            resource.lines().forEach(line ->
            {
                final String[] countryAndShardList = line.split(Pattern.quote(DELIMITER));
                final String country = countryAndShardList[0];
                final String shardList = countryAndShardList[1];
                Arrays.asList(shardList.split("\\s*,\\s*")).stream().map(CONVERTER::convert)
                        .forEach(shard -> this.countryToShards.add(country, shard));
            });
        }
        catch (final Exception e)
        {
            throw new CoreException("Error while reading CountryToShardListCache resource", e);
        }
    }

    private CountryToShardListCache()
    {

    }

    /**
     * Takes a country code, and returns a List of all {@link SlippyTile}s that cover the country
     * boundary. If an invalid country code is passed, an empty list is returned.
     *
     * @param country
     *            The three digit country code
     * @return A List of {@link SlippyTile}s
     */
    public List<Shard> getShardsForCountry(final String country)
    {
        return this.countryToShards.getOrDefault(country, Collections.emptyList());
    }

    /**
     * Writes to a {@link WritableResource} the CountryShardListCache. The resulting resource can be
     * used to initialize another CountryShardListCache.
     *
     * @param output
     *            The {@link WritableResource} where the CountryToShardListCache will be written.
     */
    public void save(final WritableResource output)
    {
        try (SafeBufferedWriter writer = output.writer())
        {
            this.countryToShards.forEach((country, shardList) ->
            {
                final List<String> shardNames = shardList.stream()
                        .map(slippyTile -> slippyTile.getName()).collect(Collectors.toList());
                writer.writeLine(String.format("%s%s%s", country, DELIMITER, shardNames)
                        .replace("[", "").replace("]", ""));
            });
        }
        catch (final Exception e)
        {
            throw new CoreException("Error while writing CountryToShardListCache to file!", e);
        }
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final StringList countries = (StringList) command.get(COUNTRIES);
        final CountryBoundaryMap boundaries = (CountryBoundaryMap) command.get(BOUNDARIES);
        final File output = (File) command.get(OUTPUT);
        final Sharding sharding = (Sharding) command.get(SHARDING);
        CountryToShardListCache cache = null;
        if (countries != null)
        {
            cache = new CountryToShardListCache(boundaries, countries, sharding);
        }
        else
        {
            cache = new CountryToShardListCache(boundaries, sharding);
        }
        logger.info("Saving file to {}", output);
        cache.save(output);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(COUNTRIES, BOUNDARIES, OUTPUT, SHARDING);
    }
}
