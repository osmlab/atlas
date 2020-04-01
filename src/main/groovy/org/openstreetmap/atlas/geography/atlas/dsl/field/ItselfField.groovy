package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ConstraintGenerator
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Represents the AtlasEntity itself. It has 2 main purposes. It can either be used as a "select *" (even though * is
 * not a valid literal in Groovy). Or it can be used for working with constraints where the
 * {@link org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BinaryOperation} is to be applied on the entire object and
 * not ant specific field.
 *
 * @author Yazad Khambata
 */
class ItselfField extends AbstractField implements Selectable, Constrainable {
    ItselfField() {
        super(Field.ITSELF)
    }

    @Override
    def read(final AtlasEntity atlasEntity) {
        return atlasEntity
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Class<E> atlasEntityClass) {
        was(params, this, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Class<E> atlasEntityClass) {
        has(params, this, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Field delegateField, final Class<E> atlasEntityClass) {
        return ConstraintGenerator.instance.was(params, delegateField, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Field delegateField, final Class<E> atlasEntityClass) {
        return ConstraintGenerator.instance.was(params, delegateField, ScanType.FULL, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final ScanType scanStrategy, final Class<E> atlasEntityClass) {
        was(params, this, scanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final ScanType scanStrategy, final Class<E> atlasEntityClass) {
        has(params, this, scanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint was(final Map params, final Field delegateField, final ScanType scanStrategy, final Class<E> atlasEntityClass) {
        return ConstraintGenerator.instance.was(params, delegateField, scanStrategy, atlasEntityClass)
    }

    @Override
    <E extends AtlasEntity> Constraint has(final Map params, final Field delegateField, final ScanType scanStrategy, final Class<E> atlasEntityClass) {
        return ConstraintGenerator.instance.was(params, delegateField, scanStrategy, atlasEntityClass)
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
