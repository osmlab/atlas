package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import java.util.Date;

import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveArea;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

/**
 * @author matthieun
 */
public class AtlasPrimitiveAreaToOsmosisWayConverter implements Converter<AtlasPrimitiveArea, Way>
{
    private static final LocationIterableToWayNodeListConverter LOCATION_ITERABLE_TO_WAY_NODE_LIST_CONVERTER = new LocationIterableToWayNodeListConverter();
    private static final TagMapToTagCollectionConverter TAG_MAP_TO_TAG_COLLECTION_CONVERTER = new TagMapToTagCollectionConverter();

    @Override
    public Way convert(final AtlasPrimitiveArea object)
    {
        return new Way(
                new CommonEntityData(object.getIdentifier(), 0, new Date(), new OsmUser(0, "osm"),
                        0, TAG_MAP_TO_TAG_COLLECTION_CONVERTER.convert(object.getTags())),
                LOCATION_ITERABLE_TO_WAY_NODE_LIST_CONVERTER
                        .convert(object.getPolygon().closedLoop()));
    }
}
