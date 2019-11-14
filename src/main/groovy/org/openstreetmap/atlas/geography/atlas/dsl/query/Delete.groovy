package org.openstreetmap.atlas.geography.atlas.dsl.query

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.query.commit.Commitable
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.MutantResult
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.Selector
import org.openstreetmap.atlas.geography.atlas.dsl.util.StreamUtil
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors
import java.util.stream.StreamSupport

/**
 * Representation of a Delete statement.
 *
 * @author Yazad Khambata
 */
@Builder
@ToString
class Delete<E extends AtlasEntity> extends Query<E> implements Commitable {
    AtlasTable<E> table

    ConditionalConstructList conditionalConstructList

    @Override
    Statement type() {
        Statement.DELETE
    }

    @Override
    Result execute(final ConsoleWriter consoleWriter) {
        final Selector<E> selector = Selector.<E> builder()
                .with(table)
                .with(conditionalConstructList)
                .with(this.type())
                .build()
        final Iterable<E> recordsToDelete = selector.fetchMatchingEntities()

        consoleWriter.echo "----------------------------------------------------"
        consoleWriter.echo "Directly Affected Records: "
        consoleWriter.echo "----------------------------------------------------"

        recordsToDelete.forEach { consoleWriter.echo it }

        consoleWriter.echo "Note:- Deletes cascade as updates and deletes to other tables. The cascade changes are not shown here. Use the diff command for more details."

        final Atlas atlasContext = table.atlasMediator.atlas

        final Change change = StreamUtil.stream(recordsToDelete)
                .map { E entity -> FeatureChange.remove( CompleteItemType.from(entity.getType()).completeEntityFrom(entity), atlasContext) }
                .collect(Collectors.collectingAndThen(Collectors.toList(), { listFeatureChanges -> ChangeBuilder.newInstance().addAll(listFeatureChanges).get() }))

        final List<Long> relevantIdentifiers = StreamUtil.stream(recordsToDelete)
                .map { E entity -> entity.getIdentifier() }
                .collect(Collectors.toList())

        final Result result = MutantResult.builder()
                .with(table)
                .with(change)
                .with(relevantIdentifiers)
                .with(table.atlasMediator)
                .build()

        result
    }

    @Override
    Delete<E> shallowCopy() {
        this.shallowCopy(false)
    }

    @Override
    Query<E> shallowCopy(final boolean excludeConditionalConstructList) {
        this.shallowCopyWithConditionalConstructList(shallowCopyConditionalConstructList(excludeConditionalConstructList))
    }

    @Override
    Query<E> shallowCopyWithConditionalConstructList(final ConditionalConstructList<E> conditionalConstructList) {
        Delete.builder()
                .table(this.table)
                .conditionalConstructList(conditionalConstructList)
                .build()
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that)
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this)
    }

    @Override
    String toPrettyString() {
        """
DELETE
    ${table}
WHERE
    ${conditionalConstructList}
        """
    }
}
