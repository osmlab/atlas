package org.openstreetmap.atlas.geography.atlas.dsl.query.result

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Idempotent results - associated with a select statement.
 *
 * @author Yazad Khambata
 */
class IdempotentResult<E extends AtlasEntity> extends AbstractResult<E> implements Result<E> {
    AtlasTable<E> table
    List<Long> relevantIdentifiers

    static IdempotentResultBuilder builder() {
        new IdempotentResultBuilder()
    }

    static class IdempotentResultBuilder<E extends AtlasEntity> {
        AtlasTable<E> table
        List<Long> relevantIdentifiers = []

        IdempotentResultBuilder with(AtlasTable<AtlasEntity> table) {
            this.table = table
            this
        }

        IdempotentResultBuilder with(final List<Long> relevantIdentifiers) {
            this.relevantIdentifiers = relevantIdentifiers
            this
        }

        IdempotentResultBuilder addingRelevantIdentifiers(final Long relevantIdentifier) {
            this.relevantIdentifiers += relevantIdentifier
            this
        }

        IdempotentResult<E> build() {
            final IdempotentResult<E> idempotentResult = new IdempotentResult<>()
            idempotentResult.setTable(table)
            idempotentResult.setRelevantIdentifiers(relevantIdentifiers ?: [])
            idempotentResult
        }
    }
}
