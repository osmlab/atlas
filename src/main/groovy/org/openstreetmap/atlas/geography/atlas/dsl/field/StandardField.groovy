package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A general purpose field implementation that supports most operations.
 *
 * @author Yazad Khambata
 */
class StandardField<C extends CompleteEntity> extends AbstractField implements Selectable, Constrainable {

    @Delegate
    Selectable selectableField

    Constrainable constrainableField

    StandardField(final String name) {
        super(name)

        selectableField = new SelectOnlyField(name)
        constrainableField = new ConstrainableFieldImpl(name)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Class <E> atlasEntityClass) {
        this.was(params, this, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Field delegateField, final Class<E> atlasEntityClass) {
        constrainableField.was(params, delegateField, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Class<E> atlasEntityClass) {
        this.has(params, this, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Field delegateField, final Class<E> atlasEntityClass) {
        constrainableField.has(params, delegateField, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final ScanType bestCandidateScanStrategy, final Class <E> atlasEntityClass) {
        this.was(params, this, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Field delegateField, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        constrainableField.was(params, delegateField, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        this.has(params, this, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Field delegateField, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        constrainableField.has(params, delegateField, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    boolean equals(final o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        final StandardField that = (StandardField) o

        if (constrainableField != that.constrainableField) return false
        if (selectableField != that.selectableField) return false

        return true
    }

    @Override
    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (selectableField != null ? selectableField.hashCode() : 0)
        result = 31 * result + (constrainableField != null ? constrainableField.hashCode() : 0)
        return result
    }
}
