package org.openstreetmap.atlas.geography.atlas.dsl.query.result

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.ItemType

import java.util.stream.Stream

/**
 * Java's stream helper to work with entities.
 *
 * @author Yazad Khambata
 */
@PackageScope
final class EntityStreamHelper {

    private EntityStreamHelper() {}

    static <E extends AtlasEntity> Stream<E> stream(final AtlasTable<E> table, List<Long> relevantIdentifiers) {
        final ItemType itemType = getItemType(table)
        final AtlasMediator atlasMediator = getAtlasMediator(table)

        stream(itemType, atlasMediator, relevantIdentifiers)
    }

    static <E extends AtlasEntity> Stream<E> stream(final ItemType itemType, final AtlasMediator atlasMediator, List<Long> relevantIdentifiers) {
        (relevantIdentifiers ?: []).stream().map  { id -> itemType.entityForIdentifier(atlasMediator.atlas, id) }
    }

    static <E extends AtlasEntity> Iterator<E> iterator(final AtlasTable<E> table, List<Long> relevantIdentifiers) {
        final ItemType itemType = getItemType(table)
        final AtlasMediator atlasMediator = getAtlasMediator(table)

        iterator(itemType, atlasMediator, relevantIdentifiers)
    }

    static <E extends AtlasEntity> Iterator<E> iterator(final ItemType itemType, final AtlasMediator atlasMediator, List<Long> relevantIdentifiers) {
        stream(itemType, atlasMediator, relevantIdentifiers).iterator()
    }

//    private static <E extends AtlasEntity> Atlas getAtlas(AtlasTable<E> table) {
//        getAtlasMediator(table).atlas
//    }

    private static <E extends AtlasEntity> AtlasMediator getAtlasMediator(AtlasTable<E> table) {
        table.atlasMediator
    }

    private static <E extends AtlasEntity> ItemType getItemType(AtlasTable<E> table) {
        table.tableSetting.itemType
    }
}
