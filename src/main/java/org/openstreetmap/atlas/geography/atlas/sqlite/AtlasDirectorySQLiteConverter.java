package org.openstreetmap.atlas.geography.atlas.sqlite;

import static org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader.IS_ATLAS;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This CLI is similar to SQLiteConverter, except here you give it a directory to search for
 * atlases. You also give it a SQLite directory where those atlas shards will then be converted into
 * their equivalent SQLite database shard.
 *
 * @author hallahan
 */
public class AtlasDirectorySQLiteConverter extends Command
{

    private static final Logger logger = LoggerFactory
            .getLogger(AtlasDirectorySQLiteConverter.class);

    private static final Switch<Path> ATLAS_DIRECTORY = new Switch<>("atlasDirectory",
            "The directory of atlases to convert.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> SQLITE_DIRECTORY = new Switch<>("sqliteDirectory",
            "The directory to write SQLite databases.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Boolean> OVERWRITE = new Switch<>("overwrite",
            "Choose to automatically overwrite a database if it exists at the given path.",
            Boolean::new, Optionality.OPTIONAL, "false");

    private static final Switch<Boolean> BUILD_INDICES = new Switch<>("indices",
            "Choose to build column and spatial indices automatically.", Boolean::new,
            Optionality.OPTIONAL, "false");

    public static void main(final String[] args)
    {
        new AtlasDirectorySQLiteConverter().run(args);
    }

    private static List<File> fetchAtlasFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream().filter(IS_ATLAS)
                .collect(Collectors.toList());
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Path atlasDirectory = (Path) command.get(ATLAS_DIRECTORY);
        final Path sqliteDirectory = (Path) command.get(SQLITE_DIRECTORY);
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);
        final Boolean buildIndices = (Boolean) command.get(BUILD_INDICES);

        if (overwrite)
        {
            try
            {
                FileUtils.deleteDirectory(sqliteDirectory.toFile());
            }
            catch (final IOException noDelete)
            {
                logger.warn(
                        "Tried to delete SQLite database output directory {} for overwrite, but unable.",
                        sqliteDirectory);
            }
        }

        final List<File> atlases = fetchAtlasFilesInDirectory(atlasDirectory);
        logger.info("About to convert {} atlas shards into SQLite databases...", atlases.size());
        for (final File atlasFile : atlases)
        {
            final Atlas atlas = new AtlasResourceLoader().load(atlasFile);
            final String name = atlasFile.getName().split(FileSuffix.ATLAS.toString())[0];
            final String dbName = name + FileSuffix.SQLITE.toString();
            final File sqliteFile = new File(sqliteDirectory.resolve(dbName).toFile());
            atlas.saveAsSQLite(sqliteFile, buildIndices);
            logger.info("Saved Atlas SQLite database shard to {}", sqliteFile);
        }

        logger.info("Finished converting directory of atlases into SQLite database shards!");
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ATLAS_DIRECTORY, SQLITE_DIRECTORY, OVERWRITE, BUILD_INDICES);
    }
}
