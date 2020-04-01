package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors

/**
 * Executes immediately and sets the identifiers, so the inner query can be used safely in an update statement's
 * where clause without concerns of side-effects.
 *
 * The inner query ignores the select list and looks for the Ids in
 * the {@link org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result}.
 *
 * @author Yazad Khambata
 */
class InnerSelectWrapper<E extends AtlasEntity> {
    Long[] identifiers

    private InnerSelectWrapper(final Select<E> select) {
        this(toRelevantIdentifiers(select) as Long[])
    }

    private InnerSelectWrapper(final QueryBuilder selectQueryBuilder) {
        this(toSelect(selectQueryBuilder))
    }

    private InnerSelectWrapper(final Long[] identifiers) {
        this.identifiers = identifiers
    }

    private static <E extends AtlasEntity> Select<E> toSelect(QueryBuilder selectQueryBuilder) {
        Valid.notEmpty selectQueryBuilder
        Valid.isTrue selectQueryBuilder.getBase() == Statement.SELECT

        (Select<E>) selectQueryBuilder.buildQuery()
    }

    private static <E extends AtlasEntity> List<Long> toRelevantIdentifiers(Select<E> select) {
        Valid.notEmpty select

        select.executeQuietly().relevantIdentifiers
    }

    static <E extends AtlasEntity> InnerSelectWrapper<E> from(final QueryBuilder selectQueryBuilder) {
        new InnerSelectWrapper<>(selectQueryBuilder)
    }

    static <E extends AtlasEntity> InnerSelectWrapper<E> from(final Long[] identifiers) {
        new InnerSelectWrapper<>(identifiers)
    }

    boolean equals(Object obj) {
        if (obj == null)
            return false

        if (obj.is(this))
            return true

        if (obj.getClass() != getClass())
            return false

        final InnerSelectWrapper that = (InnerSelectWrapper) obj

        sortedList(this.identifiers) == sortedList(that.identifiers)
    }

    @Override
    int hashCode() {
        new HashCodeBuilder()
                .appendSuper(0) //Don't append super.
                .append(sortedList(this.identifiers))
                .toHashCode()
    }

    private List<Long> sortedList(final Long[] identifiers) {
        Arrays.stream(identifiers).sorted().collect(Collectors.toList())
    }

    @Override
    String toString() {
        "<Inner Query: ${sortedList(this.identifiers)}>"
    }
}
