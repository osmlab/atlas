package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the primary place where we get an atlas and a file and write a SQLite database.
 *
 * @author hallahan
 */
public class SQLiteWriter
{
    private static final Logger logger = LoggerFactory.getLogger(SQLiteWriter.class);

    private static final String CONNECTION_URL_TEMPLATE = "jdbc:sqlite:%s";

    private final Connection connection;
    private final boolean buildIndices;
    private final PackedAtlasFieldDelegator delegator;

    public SQLiteWriter(final File sqliteDatabase, final boolean buildIndices,
            final PackedAtlasFieldDelegator delegator) throws IOException, SQLException
    {
        this.buildIndices = buildIndices;
        this.delegator = delegator;

        final String url = String.format(CONNECTION_URL_TEMPLATE, sqliteDatabase);

        try
        {
            this.connection = DriverManager.getConnection(url);
        }
        catch (final SQLException sqlException)
        {
            logger.error("Unable to connect to Atlas SQLite database.", sqlException);
            throw sqlException;
        }

        try
        {
            final SQLiteSchema schema = new SQLiteSchema();
            schema.buildTables(this.connection);
            if (buildIndices)
            {
                schema.buildIndices(this.connection);
            }
        }
        catch (final IOException ioException)
        {
            logger.error("Unable to build Atlas SQLite schema in database.", ioException);
            throw ioException;
        }

    }

    public void write(final PackedAtlas atlas, final Predicate<AtlasEntity> matcher)
            throws SQLException
    {
        logger.info("Writing atlas as SQLite database...");
        final Time time = Time.now();
        new SQLiteMetadataWriter(this.connection).write(atlas);
        new SQLiteDictionaryWriter(this.connection, this.delegator).write(atlas);

        new SQLiteItemWriter(this.connection, this.buildIndices, this.delegator.nodeTagsHandle(),
                this.delegator.nodeIdentifierMapHandle(), ItemType.NODE).write(atlas);

        new SQLiteItemWriter(this.connection, this.buildIndices, this.delegator.edgeTagsHandle(),
                this.delegator.edgeIdentifierMapHandle(), ItemType.EDGE).write(atlas);

        new SQLiteItemWriter(this.connection, this.buildIndices, this.delegator.pointTagsHandle(),
                this.delegator.pointIdentifierMapHandle(), ItemType.POINT).write(atlas);

        new SQLiteItemWriter(this.connection, this.buildIndices, this.delegator.lineTagsHandle(),
                this.delegator.lineIdentifierMapHandle(), ItemType.LINE).write(atlas);

        new SQLiteItemWriter(this.connection, this.buildIndices, this.delegator.areaTagsHandle(),
                this.delegator.areaIdentifierMapHandle(), ItemType.AREA).write(atlas);

        logger.info("Finished writing atlas as SQLite database. Total time: {}",
                time.elapsedSince());
    }
}
