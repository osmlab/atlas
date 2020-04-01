package org.openstreetmap.atlas.geography.atlas.dsl.query.result

import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Results associated with a mutation like a delete or update statement.
 *
 * @author Yazad Khambata
 */
class MutantResult<E extends AtlasEntity> extends AbstractResult<E> implements CommitableResult<E> {
    AtlasTable<E> table

    Change change

    AtlasMediator atlasMediatorToCommitOn

    List<Long> relevantIdentifiers

    static MutantResultBuilder builder() {
        new MutantResultBuilder()
    }

    static class MutantResultBuilder<E extends AtlasEntity> {
        AtlasTable<E> table

        Change change

        AtlasMediator atlasMediatorToCommitOn

        List<Long> relevantIdentifiers = []

        MutantResultBuilder with(final AtlasTable<E> table) {
            this.table = table

            this
        }

        MutantResultBuilder with(final Change change) {
            this.change = change

            this
        }

        MutantResultBuilder with(final AtlasMediator atlasMediatorToCommitOn) {
            this.atlasMediatorToCommitOn = atlasMediatorToCommitOn

            this
        }

        MutantResultBuilder with(final List<Long> relevantIdentifiers) {
            this.relevantIdentifiers = relevantIdentifiers

            this
        }

        MutantResultBuilder addingRelevantIdentifiers(final Long relevantIdentifier) {
            this.relevantIdentifiers += relevantIdentifier

            this
        }

        MutantResult build() {
            final MutantResult<E> mutantResult = new MutantResult<>()

            mutantResult.setTable(table)
            mutantResult.setChange(change)
            mutantResult.setAtlasMediatorToCommitOn(atlasMediatorToCommitOn)
            mutantResult.setRelevantIdentifiers(relevantIdentifiers?:[])

            mutantResult
        }
    }
}
