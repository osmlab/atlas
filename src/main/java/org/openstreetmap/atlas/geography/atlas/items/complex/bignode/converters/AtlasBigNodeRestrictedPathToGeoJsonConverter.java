package org.openstreetmap.atlas.geography.atlas.items.complex.bignode.converters;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * @author matthieun
 */
public class AtlasBigNodeRestrictedPathToGeoJsonConverter implements Converter<Atlas, GeoJsonObject>
{
    private final int logFrequency;

    public AtlasBigNodeRestrictedPathToGeoJsonConverter(final int logFrequency)
    {
        this.logFrequency = logFrequency;
    }

    @Override
    public GeoJsonObject convert(final Atlas atlas)
    {
        return new GeoJsonBuilder(this.logFrequency).create(Iterables
                .translateMulti(new BigNodeFinder().find(atlas), BigNode::asGeoJsonRestrictedPath));
    }
}
