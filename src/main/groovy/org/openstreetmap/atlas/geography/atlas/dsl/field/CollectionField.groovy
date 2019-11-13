package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateType

import java.util.function.BiConsumer

/**
 * A Collection field that stores more than one value, analogous to a field that is NOT in NF1.
 *
 * @author Yazad Khambata
 */
@PackageScope
class CollectionField<C extends CompleteEntity, OV, AV, RV> extends AbstractField implements Selectable, Overridable<C, OV>, Elastic<C, AV, RV>, Constrainable {

    @Delegate
    Selectable selectable

    @Delegate
    Overridable<C, OV> overridable

    @Delegate
    Elastic<C, AV, RV> elastic

    @Delegate
    Constrainable constrainable

    CollectionField(final String name, final EntityUpdateType entityUpdateType, final BiConsumer<C, OV> overrideEnricher, final BiConsumer<C, AV> addEnricher, final BiConsumer<C, RV> removeEnricher) {
        super(name)
        selectable = new SelectOnlyField(name)
        overridable = new OverridableFiled(name, entityUpdateType, this, overrideEnricher)
        elastic = new ElasticField(name, entityUpdateType, this, addEnricher, removeEnricher)
        constrainable = new ConstrainableFieldImpl(name)
    }

    @Override
    boolean equals(final o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        final CollectionField that = (CollectionField) o

        if (constrainable != that.constrainable) return false
        if (elastic != that.elastic) return false
        if (overridable != that.overridable) return false
        if (selectable != that.selectable) return false

        return true
    }

    @Override
    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (selectable != null ? selectable.hashCode() : 0)
        result = 31 * result + (overridable != null ? overridable.hashCode() : 0)
        result = 31 * result + (elastic != null ? elastic.hashCode() : 0)
        result = 31 * result + (constrainable != null ? constrainable.hashCode() : 0)
        return result
    }
}
