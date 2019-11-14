package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Function

/**
 * Similar to a standard field but value is derived from a mapping function.
 *
 * @author Yazad Khambata
 */
class StandardMappedField<C extends CompleteEntity, T, R> extends StandardField {

    StandardMappedField(final String name, final Function<T, R> mapper) {
        super(name)

        selectableField = new SelectOnlyMappedField(name, mapper)
        constrainableField = new ConstrainableFieldImpl(name)
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
