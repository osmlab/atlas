package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveRelation;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

/**
 * @author matthieun
 */
public class AtlasPrimitiveRelationToOsmosisRelationConverter
        implements Converter<AtlasPrimitiveRelation, Relation>
{
    private static final ItemTypeToEntityTypeConverter ITEM_TYPE_TO_ENTITY_TYPE_CONVERTER = new ItemTypeToEntityTypeConverter();
    private static final TagMapToTagCollectionConverter TAG_MAP_TO_TAG_COLLECTION_CONVERTER = new TagMapToTagCollectionConverter();

    @Override
    public Relation convert(final AtlasPrimitiveRelation object)
    {
        final List<RelationMember> members = new ArrayList<>();
        final RelationBean bean = object.getRelationBean();
        for (int index = 0; index < bean.size(); index++)
        {
            members.add(new RelationMember(bean.getMemberIdentifiers().get(index),
                    ITEM_TYPE_TO_ENTITY_TYPE_CONVERTER.convert(bean.getMemberTypes().get(index)),
                    bean.getMemberRoles().get(index)));
        }
        return new Relation(
                new CommonEntityData(object.getIdentifier(), 0, new Date(), new OsmUser(0, "osm"),
                        0, TAG_MAP_TO_TAG_COLLECTION_CONVERTER.convert(object.getTags())),
                members);
    }

}
