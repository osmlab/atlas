package org.openstreetmap.atlas.geography.sharding.converters;

import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.sharding.CountryShard;
import org.openstreetmap.atlas.geography.sharding.GeoHashTile;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Convert a string representation of a {@link Shard} to a concrete {@link Shard} object. Any time a
 * new {@link Shard} implementation is added, this class needs to be updated. See the Javadoc for
 * the {@link StringToShardConverter#convert(String)} method for more information.
 *
 * @author lcram
 */
public class StringToShardConverter implements Converter<String, Shard>
{
    private static final String COUNTRY_CODE_REGEX = "^[A-Z][A-Z0-9][A-Z0-9]$";
    private static final String SLIPPY_TILE_REGEX = "^[0-9]+\\-[0-9]+\\-[0-9]+$";
    private static final String GEOHASH_TILE_REGEX = "^(?:(?![ailo])[0-9a-z])+$";

    /**
     * Convert a string into a concrete {@link Shard} object. This method attempts to handle all
     * possible shard strings that exist in the wild. Note that this method will correctly handle
     * country shards in the form "ABC_1-2-3", where "_" is the declared country-shard separator. To
     * extract metadata from the shard string (e.g. ABC_1-2-3_xx/yy/zz), see
     * {@link StringToShardConverter#convertWithMetadata}.
     * 
     * @param shardString
     *            the shard in string format
     * @return the constructed {@link Shard}
     */
    @Override
    public Shard convert(final String shardString)
    {
        return convertWithMetadata(shardString).getFirst();
    }

    /**
     * Convert the shard string, and also get any metadata appended to the end of the string.
     * 
     * @param shardString
     *            the shard in string format
     * @return a {@link Tuple} containing the constructed {@link Shard} as well as the metadata.
     */
    public Tuple<Shard, Optional<String>> convertWithMetadata(final String shardString)
    {
        final StringList shardSplit = StringList.split(shardString, Shard.SHARD_DATA_SEPARATOR, 2);

        try
        {
            return convertHelper(shardSplit);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Could not parse shard string: {}", shardString, exception);
        }
    }

    private Tuple<Shard, Optional<String>> convertHelper(final StringList shardSplit)
    {
        /*
         * In this case, the shardSplit should contain just the shard string. For e.g. "1-2-3" for
         * SlippyTile or "bb2r5" for GeoHashTile.
         */
        if (shardSplit.size() == 1)
        {
            return shardFromSplitExcludingCountryShard(shardSplit);
        }
        /*
         * In this case, the shardSplit should contain a country code, a shard string, and
         * optionally some additional metadata introduced by downstream client code. For e.g. we may
         * have "ABC_1-2-2" or "DEF_bb2r5" or "GHI_1-2-3_additionalmetadata". For the purposes of
         * shard parsing, we ignore the additional metadata. Note that we use a recursive call to
         * recover the subshard, in the case of a nested CountryShard (e.g.
         * ABC_DEF_1-2-3_somemetadata). It is also possible the the shardSplit is just a shard
         * string and some metadata (e.g. 1-2-3_somemetadata). In that case, we ignore country code
         * parsing.
         */
        else if (shardSplit.size() > 1)
        {
            if (shardSplit.get(0).matches(COUNTRY_CODE_REGEX))
            {
                final StringList newListWithoutLeadingCountryCode = StringList
                        .split(shardSplit.get(1), Shard.SHARD_DATA_SEPARATOR, 2);
                final Tuple<Shard, Optional<String>> conversionResult = convertHelper(
                        newListWithoutLeadingCountryCode);
                return new Tuple<>(new CountryShard(shardSplit.get(0), conversionResult.getFirst()),
                        conversionResult.getSecond());
            }
            return shardFromSplitExcludingCountryShard(shardSplit);
        }
        /*
         * Otherwise we have to fail.
         */
        else
        {
            throw new CoreException("Split list {} had invalid size {}, must be at least 1",
                    shardSplit, shardSplit.size());
        }
    }

    /**
     * A helper to convert a {@link StringList} into a {@link Shard} object, but excluding the
     * {@link CountryShard} implementation. This method will ignore any trailing metadata.
     * 
     * @param shardSplit
     *            the {@link StringList} containing the shard info
     * @return the constructed {@link Shard}
     */
    private Tuple<Shard, Optional<String>> shardFromSplitExcludingCountryShard(
            final StringList shardSplit)
    {
        if (shardSplit.size() < 1)
        {
            throw new CoreException("Split list {} had invalid size {}, must be at least 1",
                    shardSplit, shardSplit.size());
        }

        final Shard shard;
        final String shardString = shardSplit.get(0);
        if (shardString.matches(SLIPPY_TILE_REGEX))
        {
            shard = SlippyTile.forName(shardString);
        }
        else if (shardString.matches(GEOHASH_TILE_REGEX))
        {
            shard = GeoHashTile.forName(shardString);
        }
        else
        {
            throw new CoreException("Unrecognized shard component: {}", shardString);
        }

        if (shardSplit.size() > 1)
        {
            return new Tuple<>(shard, Optional.of(shardSplit.get(1)));
        }
        return new Tuple<>(shard, Optional.empty());
    }
}
