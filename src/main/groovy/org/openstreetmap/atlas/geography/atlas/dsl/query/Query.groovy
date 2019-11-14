package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.openstreetmap.atlas.geography.Rectangle
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.QuietConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Abstraction of a query.
 *
 * @author Yazad Khambata
 */
abstract class Query<E extends AtlasEntity> {
    abstract Statement type()

    Result execute() {
        execute(StandardOutputConsoleWriter.getInstance())
    }

    abstract Result execute(final ConsoleWriter consoleWriter)

    Result executeQuietly() {
        execute(QuietConsoleWriter.getInstance())
    }

    abstract String toPrettyString()

    abstract ConditionalConstructList<E> getConditionalConstructList()

    abstract AtlasTable<E> getTable()

    Atlas atlas() {
        def atlas = getTable().atlasMediator.atlas

        Valid.notEmpty atlas

        atlas
    }

    Rectangle bounds() {
        atlas().bounds()
    }

    abstract Query<E> shallowCopy()

    abstract Query<E> shallowCopy(final boolean excludeConditionalConstructList)

    abstract Query<E> shallowCopyWithConditionalConstructList(final ConditionalConstructList<E> conditionalConstructList)

    protected ConditionalConstructList shallowCopyConditionalConstructList(boolean excludeConditionalConstructList) {
        excludeConditionalConstructList == false ? this.conditionalConstructList : null
    }
}
