package org.openstreetmap.atlas.geography.boundary;

import java.util.ArrayList;

import org.openstreetmap.atlas.exception.CoreException;
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
 * Command that writes to file the shards intersecting each country boundary
 *
 * @author james-gage
 */
public class CountryToShardListCacher extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(CountryToShardListCacher.class);
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
        new CountryToShardListCacher().run(args);
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
                    .forEach((country, shardSet) ->
                    {
                        final ArrayList<String> shardNames = new ArrayList<>();
                        shardSet.forEach(shard -> shardNames.add(shard.getName()));
                        writer.writeLine(country + "||"
                                + shardNames.toString().replace("[", "").replace("]", ""));
                    });
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
