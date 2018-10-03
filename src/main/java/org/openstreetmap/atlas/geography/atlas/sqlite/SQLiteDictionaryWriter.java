package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;

/**
 * Writes the PackedAtlas string dictionary to a table in SQLite.
 *
 * @author hallahan
 */
public class SQLiteDictionaryWriter
{
    private static final String INSERT_SQL = "INSERT INTO dictionary VALUES (?, ?)";

    private final Connection connection;
    private final PreparedStatement insertStatement;
    private final PackedAtlasFieldDelegator delegator;

    public SQLiteDictionaryWriter(final Connection connection,
            final PackedAtlasFieldDelegator delegator) throws SQLException
    {
        this.connection = connection;
        this.insertStatement = this.connection.prepareStatement(INSERT_SQL);
        this.delegator = delegator;
    }

    public void write(final PackedAtlas atlas) throws SQLException
    {
        final IntegerDictionary<String> dictionary = this.delegator.dictionaryHandle();
        final int size = dictionary.size();

        // This is very important. When you're doing bulk inserts, you need to turn this off.
        // Performance is very bad without doing this!
        this.connection.setAutoCommit(false);

        for (int key = 0; key < size; ++key)
        {
            int column = 0;
            final String value = dictionary.word(key);
            this.insertStatement.setInt(++column, key);
            this.insertStatement.setString(++column, value);
            this.insertStatement.execute();
        }

        this.connection.commit();
        this.connection.setAutoCommit(true);
    }
}
