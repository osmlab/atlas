package org.openstreetmap.atlas.geography.atlas.multi;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class MultiAtlasLoaderCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(MultiAtlasLoaderCommand.class);

    private static final Switch<Atlas> FOLDER = new Switch<>("folder",
            "Folder containing the atlas files",
            path -> new AtlasResourceLoader().load(new File(path)), Optionality.REQUIRED);
    private static final Switch<String> OUTPUT = new Switch<>("output", "output atlas file",
            StringConverter.IDENTITY, Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new MultiAtlasLoaderCommand().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final CounterWithStatistic statistics = new CounterWithStatistic(logger);
        final Atlas multi = (Atlas) command.get(FOLDER);
        final File output = new File((String) command.get(OUTPUT));
        statistics.summary();
        final PackedAtlas packed = PackedAtlas.cloneFrom(multi);
        packed.save(output);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(FOLDER, OUTPUT);
    }
}
