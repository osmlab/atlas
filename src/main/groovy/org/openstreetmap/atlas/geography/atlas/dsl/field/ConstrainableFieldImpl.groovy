package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ConstraintGenerator
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
@PackageScope
class ConstrainableFieldImpl extends AbstractField implements Constrainable {

    @Delegate
    private Readable readableField

    ConstrainableFieldImpl(final String name) {
        super(name)

        this.readableField = new SelectOnlyField(name)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Class<E> atlasEntityClass) {
        this.was(params, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Class<E> atlasEntityClass) {
        this.has(params, ScanType.FULL, atlasEntityClass)
    }


    @Override
    <E extends AtlasEntity> Constraint was(Map params, Field delegateField, Class<E> atlasEntityClass) {
        this.was(params, delegateField, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(Map params, Field delegateField, Class<E> atlasEntityClass) {
        this.has(params, delegateField, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        was(params, this, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        has(params, this, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Field delegateField, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        return ConstraintGenerator.instance.was(params, delegateField, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Field delegateField, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        return ConstraintGenerator.instance.was(params, delegateField, bestCandidateScanStrategy, atlasEntityClass)
    }

    @Override
    boolean equals(final o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        final ConstrainableFieldImpl that = (ConstrainableFieldImpl) o

        if (readableField != that.readableField) return false

        return true
    }

    @Override
    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (readableField != null ? readableField.hashCode() : 0)
        return result
    }
}
