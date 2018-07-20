package org.openstreetmap.atlas.geography.atlas;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;

/**
 * Crawl map data, to flag issues for example.
 *
 * @author matthieun
 * @author mgostintsev
 */
public abstract class Crawler extends AtlasLoadingCommand
{
    private static final Switch<File> OUTPUT_FOLDER = new Switch<>("outputFolder",
            "Location of the output folder", File::new, Optionality.REQUIRED);

    private final Logger logger;

    public Crawler(final Logger logger)
    {
        this.logger = logger;
    }

    protected void initialize(final CommandMap command)
    {
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File inputFolder = (File) command.get(INPUT_FOLDER);
        final String atlasName = inputFolder.getName();
        final File outputFolder = (File) command.get(OUTPUT_FOLDER);
        initialize(command);
        if (inputFolder != null)
        {
            this.logger.info("Loading Atlas from {}", inputFolder);
            final Atlas atlas = loadAtlas(command);
            processAtlas(atlasName, atlas, outputFolder.getPath());
        }
        return 0;
    }

    protected abstract void processAtlas(String atlasName, Atlas atlas, String folder);

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(OUTPUT_FOLDER);
    }
}
