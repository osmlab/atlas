package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import java.util.Date;

import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveLocationItem;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;

/**
 * @author matthieun
 */
public class AtlasPrimitiveLocationItemToOsmosisNodeConverter
        implements Converter<AtlasPrimitiveLocationItem, Node>
{
    private static final TagMapToTagCollectionConverter TAG_MAP_TO_TAG_COLLECTION_CONVERTER = new TagMapToTagCollectionConverter();

    @Override
    public Node convert(final AtlasPrimitiveLocationItem object)
    {
        return new Node(
                new CommonEntityData(object.getLocation().asConcatenation(), 0, new Date(),
                        new OsmUser(0, "osm"), 0,
                        TAG_MAP_TO_TAG_COLLECTION_CONVERTER.convert(object.getTags())),
                object.getLocation().getLatitude().asDegrees(),
                object.getLocation().getLongitude().asDegrees());
    }
}
