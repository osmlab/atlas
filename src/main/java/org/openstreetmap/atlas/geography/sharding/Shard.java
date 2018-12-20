package org.openstreetmap.atlas.geography.sharding;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.Located;

/**
 * A {@link Sharding} shard.
 *
 * @author matthieun
 */
public interface Shard extends Located, Serializable, GeometryPrintable
{
    String getName();
}
