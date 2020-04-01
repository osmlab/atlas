package org.openstreetmap.atlas.geography.atlas.dsl.mutant

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field

/**
 * Represents a change or mutation.
 *
 * @param V - the value type. Some examples, for add tag the value will be a Map<String, String> with one Entry.
 * In case of a tag remove it would be One String. In case of an overite it would be a Map<String, String> of zero
 * or more Entries.
 *
 * @author Yazad Khambata
 */
@ToString
class Mutant<V> {

    MutationType mutationType

    EntityUpdateMetadata<V> entityUpdateMetadata

    @Builder
    @ToString
    class EntityUpdateMetadata<V> {
        //Note: field and entityUpdateType is redundant info at this point.
        Field field

        EntityUpdateType type

        EntityUpdateOperation operation

        V mutationValue

        @Override
        boolean equals(final Object that) {
            EqualsBuilder.reflectionEquals(this, that)
        }

        @Override
        int hashCode() {
            HashCodeBuilder.reflectionHashCode(this)
        }
    }

    static MutantBuilder builder() {
        new MutantBuilder()
    }

    static class MutantBuilder<V> {

        private MutationType mutationType

        private EntityUpdateMetadata<V> entityUpdateMetadata

        //Delete does not need a mutant just yet. And create needs some design thought.

        MutantBuilder updatingEntity(final EntityUpdateMetadata<V> entityUpdateMetadata) {
            this.mutationType = MutationType.UPDATE_ENTITY
            this.entityUpdateMetadata = entityUpdateMetadata
            this
        }

        Mutant<V> build() {
            final Mutant<V> mutant = new Mutant<V>()
            mutant.mutationType = mutationType
            mutant.entityUpdateMetadata = entityUpdateMetadata
            mutant
        }
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that)
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this)
    }
}
