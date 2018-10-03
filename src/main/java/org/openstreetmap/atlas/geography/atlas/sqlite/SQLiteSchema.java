package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is our one-stop-shop where we set up the database schema.
 *
 * @author hallahan
 */
class SQLiteSchema
{
    private static final Logger logger = LoggerFactory.getLogger(SQLiteSchema.class);

    // Nice to be public so you can know about what version our schema is.
    public static final int SCHEMA_VERSION = 1;

    // CACHE_SIZE is in pages, so 1,000,000 pages * 4096 bytes = 4GB
    // Tweak this for your machine's needs.
    public static final int CACHE_SIZE = 1_000_000;

    private static final String TABLES_FILE_NAME_TEMPLATE = "org/openstreetmap/atlas/sqlite/tables_v%s.sql";
    private static final String INDICES_FILE_NAME_TEMPLATE = "org/openstreetmap/atlas/sqlite/indices_v%s.sql";
    private static final String USER_VERSION_TEMPLATE = "PRAGMA user_version = %s";
    private static final String CACHE_SIZE_TEMPLATE = "PRAGMA cache_size = %s";

    private final String tables;
    private final String indices;

    /**
     * The constructor loads the schema file of the matching schema version we are using.
     */
    SQLiteSchema() throws IOException, NullPointerException
    {
        try
        {
            String resourceName = String.format(TABLES_FILE_NAME_TEMPLATE, SCHEMA_VERSION);
            String fileName = Objects
                    .requireNonNull(getClass().getClassLoader().getResource(resourceName))
                    .getFile();
            this.tables = FileUtils.readFileToString(new java.io.File(fileName),
                    StandardCharsets.UTF_8);

            resourceName = String.format(INDICES_FILE_NAME_TEMPLATE, SCHEMA_VERSION);
            fileName = Objects.requireNonNull(getClass().getClassLoader().getResource(resourceName))
                    .getFile();
            this.indices = FileUtils.readFileToString(new java.io.File(fileName),
                    StandardCharsets.UTF_8);

        }
        catch (final NullPointerException nullPointerException)
        {
            logger.error("Unable to find Atlas SQLite schema file in resources.",
                    nullPointerException);
            throw nullPointerException;
        }
        catch (final IOException noSchema)
        {
            logger.error("Unable to read SQLite schema file in resources.", noSchema);
            throw noSchema;
        }
    }

    void buildIndices(final Connection connection) throws SQLException
    {
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate(this.indices);
        }
        catch (final SQLException noSchemaUpdate)
        {
            logger.error(
                    "Unable to build indices for SQLite database. Are you sure you haven't already made indices?",
                    noSchemaUpdate);
            throw noSchemaUpdate;
        }
    }

    void buildTables(final Connection connection) throws SQLException
    {
        try (Statement statement = connection.createStatement())
        {
            // set cache_size pragma
            statement.executeUpdate(String.format(CACHE_SIZE_TEMPLATE, CACHE_SIZE));

            // set user_version pragma
            statement.executeUpdate(String.format(USER_VERSION_TEMPLATE, SCHEMA_VERSION));

            // TODO Investigate cache spill for bulk insert performance
            // https://www.sqlite.org/pragma.html#pragma_cache_spill

            statement.executeUpdate(this.tables);
        }
        catch (final SQLException noSchemaUpdate)
        {
            logger.error(
                    "Unable to build table schema for SQLite database. Are you sure you are operating on a fresh database file?",
                    noSchemaUpdate);
            throw noSchemaUpdate;
        }
    }
}
