package org.openstreetmap.atlas.geography.atlas.dsl.query

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant
import org.openstreetmap.atlas.geography.atlas.dsl.query.commit.Commitable
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.MutantResult
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.Selector
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors
import java.util.stream.StreamSupport

/**
 * An update statement representation.
 *
 * @author Yazad Khambata
 */
@Builder
@ToString
class Update<E extends AtlasEntity> extends Query<E> implements Commitable {
    AtlasTable<E> table

    List<Mutant> mutants

    ConditionalConstructList conditionalConstructList

    @Override
    Statement type() {
        Statement.UPDATE
    }

    @Override
    Result execute(final ConsoleWriter consoleWriter) {
        final Selector<E> selector = Selector.<E> builder()
                .with(table)
                .with(conditionalConstructList)
                .with(this.type())
                .build()
        final Iterable<E> recordsToUpdate = selector.fetchMatchingEntities()

        consoleWriter.echo "----------------------------------------------------"
        consoleWriter.echo "Affected Records: "
        consoleWriter.echo "----------------------------------------------------"

        recordsToUpdate.forEach { consoleWriter.echo it }

        consoleWriter.echo "----------------------------------------------------"
        consoleWriter.echo "Mutants: "
        consoleWriter.echo "----------------------------------------------------"

        final List<Mutant> mutants = this.getMutants()

        mutants.forEach { consoleWriter.echo it }

        final Atlas atlasContext = table.atlasMediator.atlas

        final MutantResult.MutantResultBuilder mutantResultBuilder = MutantResult.builder()

        final Change change = StreamSupport.stream(recordsToUpdate.spliterator(), false).map { final E atlasEntity ->
            mutantResultBuilder.addingRelevantIdentifiers(atlasEntity.getIdentifier())

            final CompleteItemType completeItemType = CompleteItemType.from(table.getTableSetting().itemType)

            final CompleteEntity completeEntity = completeItemType.completeEntityFrom(atlasEntity)

            mutants.forEach { mutant ->
                mutant.entityUpdateMetadata.operation.perform(mutant.entityUpdateMetadata.field, completeEntity, mutant.entityUpdateMetadata.mutationValue)
            }

            completeEntity
        }.map { completeEntity ->
            //For Delete statements we must use remove instead of add.
            //FeatureChange.add(completeEntity, atlasContext) // This line should be uncommented after fix from Atlas.
            FeatureChange.add(completeEntity, atlasContext)
        }.collect(Collectors.collectingAndThen(Collectors.toList(), { listFeatureChanges -> ChangeBuilder.newInstance().addAll(listFeatureChanges).get() }))

        consoleWriter.echo "----------------------------------------------------"
        consoleWriter.echo "Change: "
        consoleWriter.echo "----------------------------------------------------"

        change.changes().forEach { featureChange ->
            consoleWriter.echo "${featureChange.identifier} ${featureChange.changeType} ${featureChange.bounds()} ${featureChange.getAfterView().tags}"
        }

        mutantResultBuilder.with(table).with(change).with(table.atlasMediator).build()
    }

    @Override
    Update<E> shallowCopy() {
        shallowCopy(false)
    }

    @Override
    Query<E> shallowCopy(final boolean excludeConditionalConstructList) {
        this.shallowCopyWithConditionalConstructList(shallowCopyConditionalConstructList(excludeConditionalConstructList))
    }

    @Override
    Query<E> shallowCopyWithConditionalConstructList(final ConditionalConstructList<E> conditionalConstructList) {
        Update.builder()
                .table(this.table)
                .mutants(this.mutants)
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
        """\
UPDATE
    ${table}
SET
    ${mutants}
WHERE
${conditionalConstructList.toPrettyString("\t")}
"""
    }
}
