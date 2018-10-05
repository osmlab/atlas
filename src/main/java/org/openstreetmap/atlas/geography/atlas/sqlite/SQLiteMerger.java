package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.openstreetmap.atlas.streaming.resource.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLiteMerger takes a path to an output database file that it will create. It then can attach
 * Atlas SQLite shard databases and merge those into this output database.
 *
 * @author hallahan
 */
public class SQLiteMerger
{

    private static final Logger logger = LoggerFactory.getLogger(SQLiteMerger.class);

    private static final String CONNECTION_URL_TEMPLATE = "jdbc:sqlite:%s";

    private static final String ATTACH_SQL = "ATTACH DATABASE'%s' as merge";
    private static final String DETACH_SQL = "DETACH DATABASE merge";
    private static final String MERGE_EDGES_SQL = "INSERT OR IGNORE INTO edge SELECT * FROM merge.edge";
    private static final String MERGE_EDGES_TAGS_SQL = "INSERT OR IGNORE INTO edge_tags SELECT * FROM merge.edge_tags";

    private final Connection connection;
    private final SQLiteSchema schema;

    /**
     * This class lets you specify a path to where you'd like an output database with your merged
     * data. Then, you call the #merge method to attach and merge in a shard database.
     *
     * @param output
     *            The path to the merge database.
     * @throws SQLException
     *             If an attach, merge, or detach statement fails.
     * @throws IOException
     *             If connecting to a database fails.
     */
    public SQLiteMerger(final Path output) throws SQLException, IOException
    {
        final String url = String.format(CONNECTION_URL_TEMPLATE, output);

        try
        {
            this.connection = DriverManager.getConnection(url);
        }
        catch (final SQLException sqlException)
        {
            logger.error("Unable to connect to merge output SQLite database.", sqlException);
            throw sqlException;
        }

        try
        {
            this.schema = new SQLiteSchema();
            this.schema.buildTables(this.connection);
        }
        catch (final IOException ioException)
        {
            logger.error("Unable to build Atlas SQLite table schema in merge output database.",
                    ioException);
            throw ioException;
        }

    }

    public void buildIndices() throws SQLException
    {
        try
        {
            this.schema.buildIndices(this.connection);
        }
        catch (final SQLException sqlException)
        {
            logger.error("Unable to build indices on merge database.");
            throw sqlException;
        }
    }

    public void merge(final File mergeDatabase) throws SQLException
    {
        final String databaseName = mergeDatabase.getName();

        try (Statement statement = this.connection.createStatement())
        {
            // Attach merge database.
            final String attachSql = String.format(ATTACH_SQL, mergeDatabase.getAbsolutePath());
            statement.execute(attachSql);
            logger.info("Attached database {} for merge.", databaseName);

            // Do the merge!
            statement.executeUpdate(MERGE_EDGES_SQL);
            statement.executeUpdate(MERGE_EDGES_TAGS_SQL);

            // Detach merge database.
            statement.execute(DETACH_SQL);
            logger.info("Merge complete. Detached database {}.", databaseName);

        }
        catch (final SQLException sqlException)
        {
            logger.error("Unable to merge database {}.", databaseName, sqlException);
            throw sqlException;
        }
    }

}
