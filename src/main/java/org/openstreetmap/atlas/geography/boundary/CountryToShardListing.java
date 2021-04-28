package org.openstreetmap.atlas.geography.boundary;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.sharding.CountryShard;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List all the {@link Shard}s for each country, wrapped in a command to print the result to a
 * Resource.
 *
 * @author matthieun
 */
public class CountryToShardListing extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(CountryToShardListing.class);

    private static final Switch<CountryBoundaryMap> BOUNDARIES = new Switch<>("boundaries",
            "The country boundaries.", value ->
            {
                final Time start = Time.now();
                logger.info("Loading boundaries");
                final CountryBoundaryMap result = CountryBoundaryMap.fromPlainText(new File(value));
                logger.info("Loaded boundaries in {}", start.elapsedSince());
                return result;
            }, Optionality.REQUIRED);
    private static final Switch<StringList> COUNTRIES = new Switch<>("countries",
            "CSV list of the country iso3 codes", value -> StringList.split(value, ","),
            Optionality.REQUIRED);
    private static final Switch<Sharding> SHARDING = new Switch<>("sharding",
            "File containing the sharding definition (Works with files only)", Sharding::forString,
            Optionality.REQUIRED);
    private static final Switch<File> OUTPUT = new Switch<>("output", "The output file", File::new,
            Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new CountryToShardListing().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Time overall = Time.now();
        final StringList countries = (StringList) command.get(COUNTRIES);
        logger.info("Listing for countries: {}", countries);
        final CountryBoundaryMap boundaries = (CountryBoundaryMap) command.get(BOUNDARIES);
        final File output = (File) command.get(OUTPUT);
        final Sharding sharding = (Sharding) command.get(SHARDING);
        try (SafeBufferedWriter writer = output.writer())
        {
            CountryShardListing.countryToShardList(countries, boundaries, sharding)
                    .forEach((country, shardSet) -> shardSet.forEach(
                            shard -> writer.writeLine(new CountryShard(country, shard).getName())));
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not match shards to boundaries", e);
        }
        logger.info("Finished in {}", overall.elapsedSince());
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BOUNDARIES, COUNTRIES, SHARDING, OUTPUT);
    }
}
