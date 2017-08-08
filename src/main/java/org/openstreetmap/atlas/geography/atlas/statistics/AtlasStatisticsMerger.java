package org.openstreetmap.atlas.geography.atlas.statistics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * @author matthieun
 */
public class AtlasStatisticsMerger extends Command
{
    private static final Switch<List<File>> INPUT = new Switch<>("input",
            "The input folder containing all the shard stat files", value ->
            {
                final File folder = new File(value);
                return folder.listFilesRecursively().stream()
                        .filter(file -> file.getName().endsWith(".csv.gz"))
                        .collect(Collectors.toList());
            });
    private static final Switch<File> OUTPUT = new Switch<>("output",
            "The output folder for the country stats", File::new);

    public static void main(final String[] args)
    {
        new AtlasStatisticsMerger().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        @SuppressWarnings("unchecked")
        final List<File> inputs = (List<File>) command.get(INPUT);
        final File output = (File) command.get(OUTPUT);
        output.mkdirs();
        final MultiMap<String, AtlasStatistics> stats = new MultiMap<>();
        for (final File input : inputs)
        {
            final String country = input.getName().split("_")[0];
            final AtlasStatistics stat = AtlasStatistics.fromResource(input);
            stats.add(country, stat);
        }
        final Map<String, AtlasStatistics> countryStats = stats.reduceByKey(AtlasStatistics::merge);
        countryStats.forEach((country, stat) -> stat.save(output.child(country + ".csv")));
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT, OUTPUT);
    }

}
