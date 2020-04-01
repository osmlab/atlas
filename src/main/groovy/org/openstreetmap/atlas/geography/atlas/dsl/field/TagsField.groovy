package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.apache.commons.lang3.tuple.Pair
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateType
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

import java.util.function.BiConsumer

/**
 * Represents the tags field in the entity tables.
 *
 * @author Yazad Khambata
 */
class TagsField<C extends CompleteEntity> extends CollectionField<C, Map<String, String>, Map<String, String>, String> {

    private static final BiConsumer<C, Map<String, String>> overrideEnricher = { completeEntity, overrideValue ->
        final Pair<String, String> pair = toPair(overrideValue)
        completeEntity.withTags(pair.getKey(), pair.getValue())
    }

    private static final BiConsumer<C, Map<String, String>> addEnricher = { completeEntity, addValue ->
        final Pair<String, String> pair = toPair(addValue)
        completeEntity.withAddedTag(pair.getKey(), pair.getValue())
    }

    private static final BiConsumer<C, String> removeEnricher = { completeEntity, removeValue -> completeEntity.withRemovedTag(removeValue) }

    TagsField(final String name) {
        super(name, EntityUpdateType.TAGS, overrideEnricher, addEnricher, removeEnricher)
    }

    private static Map.Entry<String, String> toPair(final Map<String, String> valueAsMap) {
        Valid.isTrue valueAsMap.size() == 1
        final Map.Entry<String, String> entry = valueAsMap.entrySet().iterator().next()

        Pair.of(entry.getKey(), entry.getValue())
    }

    @Override
    boolean equals(final Object o) {
        return super.equals(o)
    }

    @Override
    int hashCode() {
        return super.hashCode()
    }
}
