package org.openstreetmap.atlas.geography.atlas.dsl.query

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.field.Selectable
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.IdempotentResult
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.IdempotentResult.IdempotentResultBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.Selector
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A representation of a Select query.
 *
 * @author Yazad Khambata
 */
@Builder
@ToString
class Select<E extends AtlasEntity> extends Query<E> {
    List<Field> fieldsToSelect

    AtlasTable<E> table

    ConditionalConstructList conditionalConstructList

    Long limit

    def <E extends AtlasEntity> Map<String, Object> read(final E entity) {
        final Map map = new LinkedHashMap()

        fieldsToSelect.forEach({ fieldToSelect ->
            map.put(fieldAlias(fieldToSelect), ((Selectable) fieldToSelect).read(entity))
        })

        map
    }

    private String fieldAlias(final Field fieldToSelect) {
        fieldToSelect.alias
    }

    @Override
    Statement type() {
        Statement.SELECT
    }

    @Override
    Result execute(final ConsoleWriter consoleWriter) {
        final Selector<E> selector = Selector.<E> builder()
                .with(table)
                .with(conditionalConstructList)
                .with(this.type())
                .build()

        final Iterable<E> entities = selector.fetchMatchingEntities()

        int index = 0

        final boolean checkLimit = limit > 0

        final IdempotentResultBuilder idempotentResultBuilder = IdempotentResult.builder()
        idempotentResultBuilder.setTable(table)

        for (E entity : entities) {
            ++index

            if (checkLimit && index > limit) {
                break
            }

            idempotentResultBuilder.addingRelevantIdentifiers(entity.getIdentifier())

            if (!consoleWriter.isTurnedOff()) {
                consoleWriter.echo "${index}\t :: ${read(entity)}"
            }
            entity.getIdentifier()
        }

        idempotentResultBuilder.build()
    }

    @Override
    Select<E> shallowCopy() {
        shallowCopy(false)
    }

    @Override
    Query<E> shallowCopy(final boolean excludeConditionalConstructList) {
        this.shallowCopyWithConditionalConstructList(shallowCopyConditionalConstructList(excludeConditionalConstructList))
    }

    @Override
    Query<E> shallowCopyWithConditionalConstructList(final ConditionalConstructList<E> conditionalConstructList) {
        Select.builder()
                .fieldsToSelect(this.fieldsToSelect)
                .table(this.table)
                .conditionalConstructList(conditionalConstructList)
                .limit(this.limit)
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
SELECT
    ${fieldsToSelect}
FROM
    ${table}
WHERE
    ${conditionalConstructList}
LIMIT
    ${limit}
        """
    }
}
