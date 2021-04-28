package org.openstreetmap.atlas.geography.sharding;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;

/**
 * A {@link Sharding} shard.
 *
 * @author matthieun
 */
public interface Shard extends Located, Serializable, GeometryPrintable // NOSONAR
{
    /**
     * The separator character for data within a shard string. For example, this character should be
     * used to separate the country code from the shard name: USA_1-2-3. It should also be used to
     * separate metadata from the shard name: USA_4-5-6_zz/xx/yy
     */
    String SHARD_DATA_SEPARATOR = "_";

    /**
     * Get the name of this {@link Shard}. The result of this method should be parsable by
     * {@link StringToShardConverter}.
     * 
     * @return the parsable name of this {@link Shard}
     */
    String getName();
}
