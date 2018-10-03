package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasLoadingCommand;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to save an Atlas as a SQLite database.
 *
 * @author hallahan
 */
public class SQLiteConverter extends AtlasLoadingCommand
{
    private static final Logger logger = LoggerFactory.getLogger(SQLiteConverter.class);

    private static final Switch<File> SQLITE_DATABASE = new Switch<>("output",
            "The SQLite database file to contain atlas data.", File::new, Optionality.REQUIRED);

    private static final Switch<Boolean> OVERWRITE = new Switch<>("overwrite",
            "Choose to automatically overwrite a database if it exists at the given path.",
            Boolean::new, Optionality.OPTIONAL, "false");

    private static final Switch<Boolean> BUILD_INDICES = new Switch<>("indices",
            "Choose to build column and spatial indices automatically.", Boolean::new,
            Optionality.OPTIONAL, "false");

    public static void main(final String[] args)
    {
        new SQLiteConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File outputFile = (File) command.get(SQLITE_DATABASE);
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);
        final Boolean buildIndices = (Boolean) command.get(BUILD_INDICES);

        if (overwrite)
        {
            try
            {
                Files.delete(Paths.get(outputFile.getPath()));
            }
            catch (final IOException noDelete)
            {
                logger.warn("Tried to delete database {} for overwrite, but unable.", outputFile);
            }
        }

        final Atlas atlas = loadAtlas(command);
        atlas.saveAsSQLite(outputFile, buildIndices);
        logger.info("Saved Atlas SQLite database to {}", outputFile);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(SQLITE_DATABASE, OVERWRITE, BUILD_INDICES);
    }
}
