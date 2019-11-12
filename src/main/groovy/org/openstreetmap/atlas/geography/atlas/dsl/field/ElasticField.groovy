package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateOperation
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateType
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant

import java.util.function.BiConsumer

/**
 * An implementation of an Elastic field that can grow or shrink.
 *
 * @author Yazad Khambata
 */
@PackageScope
class ElasticField<C extends CompleteEntity, AV, RV> extends AbstractMutableField implements Elastic<C, AV, RV> {

    private Field composingField

    private BiConsumer<C, AV> addEnricher
    private BiConsumer<C, RV> removeEnricher

    ElasticField(final String name, final EntityUpdateType entityUpdateType, final Field composingField, final BiConsumer<C, AV> addEnricher, final BiConsumer<C, RV> removeEnricher) {
        super(name, entityUpdateType)

        this.composingField = composingField

        this.addEnricher = addEnricher
        this.removeEnricher = removeEnricher
    }

    @Override
    Mutant add(final Object value) {
        final Mutant.EntityUpdateMetadata entityUpdateMetadata = Mutant.EntityUpdateMetadata.builder()
                .field(composingField)
                .type(getEntityUpdateType())
                .operation(EntityUpdateOperation.ADD)
                .mutationValue(value)
                .build()

        Mutant.builder().updatingEntity(entityUpdateMetadata).build()
    }

    @Override
    void enrichAdd(final C completeEntity, final AV addValue) {
        addEnricher.accept(completeEntity, addValue)
    }

    @Override
    Mutant remove(final Object key) {
        final Mutant.EntityUpdateMetadata entityUpdateMetadata = Mutant.EntityUpdateMetadata.builder()
                .field(composingField)
                .type(getEntityUpdateType())
                .operation(EntityUpdateOperation.DELETENOP)
                .mutationValue(key)
                .build()

        Mutant.builder().updatingEntity(entityUpdateMetadata).build()
    }

    @Override
    void enrichRemove(final C completeEntity, final RV removeValue) {
        removeEnricher.accept(completeEntity, removeValue)
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
