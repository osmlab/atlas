package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;

/**
 * We write applicable metadata about a given atlas to a metadata table.
 *
 * @author hallahan
 */
public class SQLiteMetadataWriter
{
    private static final String INSERT_SQL = "INSERT INTO  metadata values(?,?)";

    private static final int KEY_IDX = 1;
    private static final int VALUE_IDX = 2;

    private static final String NAME = "name";

    private final PreparedStatement statement;

    SQLiteMetadataWriter(final Connection connection) throws SQLException
    {
        this.statement = connection.prepareStatement(INSERT_SQL);
    }

    public void write(final PackedAtlas atlas) throws SQLException
    {
        final String name = atlas.getName();
        insertKeyValue(NAME, name);
    }

    private void insertKeyValue(final String key, final String value) throws SQLException
    {
        this.statement.setString(KEY_IDX, key);
        this.statement.setString(VALUE_IDX, value);
        this.statement.execute();
    }
}
