package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateOperation
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateType
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant

import java.util.function.BiConsumer

/**
 * An implementation of a general overridable field.
 *
 * @author Yazad Khambata
 */
@PackageScope
class OverridableFiled<C extends CompleteEntity, OV> extends AbstractMutableField implements Overridable<C, OV> {

    private Field composingField

    private BiConsumer<C, OV> overrideEnricher

    OverridableFiled(final String name, final EntityUpdateType entityUpdateType, final Field composingField, final BiConsumer<C, OV> overrideEnricher) {
        super(name, entityUpdateType)
        this.composingField = composingField
        this.overrideEnricher = overrideEnricher
    }

    @Override
    Mutant to(final Object value) {
        final Mutant.EntityUpdateMetadata entityUpdateMetadata = Mutant.EntityUpdateMetadata.builder()
                .field(composingField)
                .type(getEntityUpdateType())
                .operation(EntityUpdateOperation.OVERRIDE)
                .mutationValue(value)
                .build()

        Mutant.builder().updatingEntity(entityUpdateMetadata).build()
    }

    @Override
    void enrichOverride(final C completeEntity, final OV overrideValue) {
        overrideEnricher.accept(completeEntity, overrideValue)
    }

    @Override
    boolean equals(final o) {
        super.equals(o)
    }

    @Override
    int hashCode() {
        super.hashCode()
    }
}
