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
public class AtlasBigNodesToGeoJsonConverter implements Converter<Atlas, GeoJsonObject>
{
    @Override
    public GeoJsonObject convert(final Atlas atlas)
    {
        return new GeoJsonBuilder().create(
                Iterables.translate(new BigNodeFinder().find(atlas), BigNode::asGeoJsonBigNode));
    }
}
