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
    private static final String SEPARATOR = "-";
    private static final int TILE_DIMENSIONS = 3;

    @Override
    public SlippyTile backwardConvert(final String slippyTileParameters)
    {
        final StringList splits = StringList.split(slippyTileParameters, SEPARATOR);
        if (splits.size() != TILE_DIMENSIONS)
        {
            throw new CoreException("Wrong format of input string {}", slippyTileParameters);
        }
        final int zoom = Integer.valueOf(splits.get(0));
        final int xAxis = Integer.valueOf(splits.get(1));
        final int yAxis = Integer.valueOf(splits.get(2));
        return new SlippyTile(xAxis, yAxis, zoom);
    }

    @Override
    public String convert(final SlippyTile slippyTile)
    {
        return slippyTile.getZoom() + SEPARATOR + slippyTile.getX() + SEPARATOR + slippyTile.getY();
    }
}
