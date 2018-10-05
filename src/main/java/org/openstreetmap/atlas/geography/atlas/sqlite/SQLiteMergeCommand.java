package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a directory full of SQLite atlases and merges them into a single SQLite atlas.
 *
 * @author hallahan
 */
public class SQLiteMergeCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(SQLiteMergeCommand.class);

    private static final Switch<Path> MERGE_DIRECTORY_SWITCH = new Switch<>("mergeDirectory",
            "The path to the directory with SQLite databases you would like to merge.", Paths::get,
            Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_SWITCH = new Switch<>("output",
            "The path of the output SQLite database.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Boolean> OVERWRITE = new Switch<>("overwrite",
            "Choose to automatically overwrite the output database if it exists at the given path.",
            Boolean::new, Optionality.OPTIONAL, "false");

    private static final Switch<Boolean> BUILD_INDICES = new Switch<>("indices",
            "Choose to build column and spatial indices automatically.", Boolean::new,
            Optionality.OPTIONAL, "true");

    public static void main(final String[] args)
    {
        new SQLiteMergeCommand().run(args);
    }

    /**
     * Give you a recursive list of files with .db SQLite extension for a given directory.
     *
     * @param directory
     *            The directory you want the files from
     * @return All of the .db SQLite files in that directory.
     */
    private static List<File> fetchSQLiteFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream()
                .filter(FileSuffix.resourceFilter(FileSuffix.SQLITE)).collect(Collectors.toList());
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Path mergeDirectory = (Path) command.get("mergeDirectory");
        final Path output = (Path) command.get("output");
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);
        final Boolean buildIndices = (Boolean) command.get(BUILD_INDICES);

        if (overwrite)
        {
            try
            {
                Files.delete(output);
            }
            catch (final IOException noDelete)
            {
                logger.warn(
                        "Tried to delete SQLite database output database {} for overwrite, but unable.",
                        output);
            }
        }

        final List<File> mergeFiles = fetchSQLiteFilesInDirectory(mergeDirectory);
        logger.info("About to merge {} Atlas SQLite files into a single database file...",
                mergeFiles.size());
        try
        {
            final Time mergeTime = Time.now();
            final SQLiteMerger merger = new SQLiteMerger(output);
            for (final File file : mergeFiles)
            {
                merger.merge(file);
                logger.info("Merged {}.", file.getName());
            }
            logger.info("Took {} to merge the databases.", mergeTime.elapsedSince());

            if (buildIndices)
            {
                logger.info("Building indices in output database...");
                final Time indicesTime = Time.now();
                merger.buildIndices();
                logger.info("Took {} to build indices.", indicesTime.elapsedSince());

            }

        }
        catch (final SQLException sqlException)
        {
            logger.error("Exiting due to SQLException.", sqlException);
            return -1;
        }
        catch (final IOException ioException)
        {
            logger.error("Exiting due to IOException.", ioException);
            return -1;
        }

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(MERGE_DIRECTORY_SWITCH, OUTPUT_SWITCH, OVERWRITE,
                BUILD_INDICES);
    }
}
