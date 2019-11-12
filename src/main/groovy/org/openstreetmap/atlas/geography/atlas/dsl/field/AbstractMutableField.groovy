package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateType

/**
 * An abstraction of a mutable field..
 *
 * @author Yazad Khambata
 */
class AbstractMutableField extends AbstractField implements MutableProperty {

    final EntityUpdateType entityUpdateType

    AbstractMutableField(final String name, final EntityUpdateType entityUpdateType) {
        super(name)
        this.entityUpdateType = entityUpdateType
    }

    @Override
    boolean equals(final o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        final AbstractMutableField that = (AbstractMutableField) o

        if (entityUpdateType != that.entityUpdateType) return false

        return true
    }

    @Override
    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (entityUpdateType != null ? entityUpdateType.hashCode() : 0)
        return result
    }
}
