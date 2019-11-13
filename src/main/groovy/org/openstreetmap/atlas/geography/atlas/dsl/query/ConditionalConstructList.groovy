package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors

/**
 * A List of Conditional Constructs in the order in which they appear in the query.
 * Optimization involves changes to the content and order of the contents of this list.
 *
 * @author Yazad Khambata
 */
class ConditionalConstructList<E extends AtlasEntity> implements List<ConditionalConstruct<E>> {
    @Delegate
    List<ConditionalConstruct<E>> conditionalConstructs

    ConditionalConstructList() {
        this([])
    }

    ConditionalConstructList(final List<ConditionalConstruct<E>> conditionalConstructs) {
        this.conditionalConstructs = conditionalConstructs
    }

    Optional<ConditionalConstruct<E>> getFirst() {
        final ConditionalConstruct<E> conditionalConstruct = conditionalConstructs.size() == 0 ? null : conditionalConstructs.get(0)
        Optional.ofNullable(conditionalConstruct)
    }

    List<ConditionalConstruct<E>> getExcludingFirst() {
        final int size = this.size()
        if (size <= 1) {
            return []
        }

        conditionalConstructs.subList(1, size)
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that)
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this)
    }

    ConditionalConstructList<E> deepCopy() {
        final List<ConditionalConstruct<E>> copyOfConditionalConstructs = conditionalConstructs.stream()
                .map { conditionalConstruct -> conditionalConstruct.deepCopy() }
                .collect(Collectors.toList())

        new ConditionalConstructList<E>(copyOfConditionalConstructs)
    }
}
