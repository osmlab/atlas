package org.openstreetmap.atlas.geography.sharding.converters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Convert a SlippyTile to its string value
 *
 * @author tony
 */
public class SlippyTileConverter implements TwoWayConverter<SlippyTile, String>
{
    private static final int TILE_DIMENSIONS = 3;

    @Override
    public SlippyTile backwardConvert(final String slippyTileParameters)
    {
        final StringList splits = StringList.split(slippyTileParameters,
                SlippyTile.COORDINATE_SEPARATOR);
        if (splits.size() != TILE_DIMENSIONS)
        {
            throw new CoreException("Wrong format of input string {}", slippyTileParameters);
        }
        final int zoom = Integer.parseInt(splits.get(0));
        final int xAxis = Integer.parseInt(splits.get(1));
        final int yAxis = Integer.parseInt(splits.get(2));
        return new SlippyTile(xAxis, yAxis, zoom);
    }

    @Override
    public String convert(final SlippyTile slippyTile)
    {
        return slippyTile.getZoom() + SlippyTile.COORDINATE_SEPARATOR + slippyTile.getX()
                + SlippyTile.COORDINATE_SEPARATOR + slippyTile.getY();
    }
}
