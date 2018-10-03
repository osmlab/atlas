package org.openstreetmap.atlas.geography.atlas.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedTagStore;
import org.openstreetmap.atlas.geography.converters.WkbLocationConverter;
import org.openstreetmap.atlas.utilities.maps.LongToLongMap;

/**
 * To save all of the data within an atlas, we need to get each {@link AtlasItem} type and iterate
 * though them with inserts into the database. This class declares that a given writer needs to get
 * the atlas to do its job.
 *
 * @author hallahan
 * @author lcram
 */
public class SQLiteItemWriter
{
    private static final String INSERT_SQL = "INSERT INTO %s VALUES (?, ?, ?, ?)";
    private static final String INSERT_TAGS_SQL = "INSERT INTO %s_tags VALUES (?, ?, ?)";
    private static final String INSERT_RTREE_SQL = "INSERT INTO %s_rtree VALUES (?, ?, ?, ?, ?)";

    // database column constants
    private static final int FIRST_COLUMN = 1;
    private static final int SECOND_COLUMN = 2;
    private static final int THIRD_COLUMN = 3;
    private static final int FOURTH_COLUMN = 4;
    private static final int FIFTH_COLUMN = 5;

    private final Connection connection;
    private final PreparedStatement itemStatement;
    private final PreparedStatement tagsStatement;
    private final Optional<PreparedStatement> rtreeStatementOptional;

    private final LongToLongMap identifierToTagStoreIndex;
    private final PackedTagStore tagStore;
    private final ItemType itemType;

    SQLiteItemWriter(final Connection connection, final boolean buildIndices,
            final PackedTagStore tagStore, final LongToLongMap identifierToTagStoreIndex,
            final ItemType itemType) throws SQLException
    {
        this.tagStore = tagStore;
        this.identifierToTagStoreIndex = identifierToTagStoreIndex;
        this.itemType = itemType;
        this.connection = connection;

        // Construct the prepared statements
        this.itemStatement = connection.prepareStatement(
                String.format(INSERT_SQL, this.itemType.toString().toLowerCase()));
        this.tagsStatement = connection.prepareStatement(
                String.format(INSERT_TAGS_SQL, this.itemType.toString().toLowerCase()));
        if (buildIndices)
        {
            this.rtreeStatementOptional = Optional.of(connection.prepareStatement(
                    String.format(INSERT_RTREE_SQL, this.itemType.toString().toLowerCase())));
        }
        else
        {
            this.rtreeStatementOptional = Optional.empty();
        }
    }

    public void write(final PackedAtlas atlas) throws SQLException
    {
        // This is very important. When you're doing bulk inserts, you need to turn this off.
        // Performance is very bad without doing this!
        this.connection.setAutoCommit(false);

        final Iterable<? extends AtlasItem> items = getItemIterableOfCorrectType(atlas,
                this.itemType);

        for (final AtlasItem item : items)
        {
            // INSERT feature data
            final long atlasId = item.getIdentifier();
            this.itemStatement.setLong(FIRST_COLUMN, atlasId);
            this.itemStatement.setString(SECOND_COLUMN, "md5sum_here");

            final Optional<String> shardName = atlas.metaData().getShardName();
            if (shardName.isPresent())
            {
                this.itemStatement.setString(THIRD_COLUMN, shardName.get());
            }
            else
            {
                this.itemStatement.setString(THIRD_COLUMN, null);
            }

            final byte[] wkb = getWKBOfItem(item);
            this.itemStatement.setBytes(FOURTH_COLUMN, wkb);
            this.itemStatement.execute();

            // INSERT tag data for the feature
            final long tagStoreIndex = this.identifierToTagStoreIndex.get(new Long(atlasId));
            final int[] keyIndices = this.tagStore.getKeyArrayAtIndex(tagStoreIndex);
            final int[] valueIndices = this.tagStore.getValueArrayAtIndex(tagStoreIndex);

            if (keyIndices.length != valueIndices.length)
            {
                throw new CoreException(
                        "key and value array length mismatch, something went wrong");
            }

            for (int index = 0; index < keyIndices.length; index++)
            {
                this.tagsStatement.setLong(FIRST_COLUMN, atlasId);
                this.tagsStatement.setInt(SECOND_COLUMN, keyIndices[index]);
                this.tagsStatement.setInt(THIRD_COLUMN, valueIndices[index]);
                this.tagsStatement.execute();
            }

            // INSERT spatial information if enabled
            if (this.rtreeStatementOptional.isPresent())
            {
                final PreparedStatement unwrappedStatement = this.rtreeStatementOptional.get();

                final Rectangle bounds = item.bounds();
                final Location lowerLeft = bounds.lowerLeft();
                final Location upperRight = bounds.upperRight();
                final double minLon = lowerLeft.getLongitude().asDegrees();
                final double minLat = lowerLeft.getLatitude().asDegrees();
                final double maxLon = upperRight.getLongitude().asDegrees();
                final double maxLat = upperRight.getLatitude().asDegrees();

                // feature id and bounding box in rtree
                unwrappedStatement.setLong(FIRST_COLUMN, atlasId);
                unwrappedStatement.setDouble(SECOND_COLUMN, minLon);
                unwrappedStatement.setDouble(THIRD_COLUMN, maxLon);
                unwrappedStatement.setDouble(FOURTH_COLUMN, minLat);
                unwrappedStatement.setDouble(FIFTH_COLUMN, maxLat);

                unwrappedStatement.execute();
            }
        }

        this.connection.commit();
        this.connection.setAutoCommit(true);
    }

    private Iterable<? extends AtlasItem> getItemIterableOfCorrectType(final PackedAtlas atlas,
            final ItemType itemType)
    {
        final Iterable<? extends AtlasItem> items;
        switch (itemType)
        {
            case NODE:
                items = atlas.nodes();
                break;
            case EDGE:
                items = atlas.edges();
                break;
            case POINT:
                items = atlas.points();
                break;
            case LINE:
                items = atlas.lines();
                break;
            case AREA:
                items = atlas.areas();
                break;
            default:
                throw new CoreException("Unknown entity type {}", itemType);
        }

        return items;
    }

    private byte[] getWKBOfItem(final AtlasItem item)
    {
        byte[] wkb = null;

        if (item.getType() != this.itemType)
        {
            throw new CoreException(
                    "ItemType {} of AtlasItem {} did not match expected ItemType {}",
                    item.getType(), item, this.itemType);
        }

        switch (item.getType())
        {
            case NODE:
                wkb = new WkbLocationConverter().convert(((Node) item).getLocation());
                break;
            case EDGE:
                wkb = ((Edge) item).asPolyLine().toWkb();
                break;
            case POINT:
                wkb = new WkbLocationConverter().convert(((Point) item).getLocation());
                break;
            case LINE:
                wkb = ((Line) item).asPolyLine().toWkb();
                break;
            case AREA:
                wkb = ((Area) item).asPolygon().toWkb();
                break;
            default:
                throw new CoreException("Unknown entity type {}", item.getType());
        }
        return wkb;
    }
}
