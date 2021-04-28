package org.openstreetmap.atlas.geography.atlas.dsl.mutant

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.field.Growable
import org.openstreetmap.atlas.geography.atlas.dsl.field.MutableProperty
import org.openstreetmap.atlas.geography.atlas.dsl.field.Overridable
import org.openstreetmap.atlas.geography.atlas.dsl.field.Shrinkable

/**
 * Enum that keeps track of the actual mutation operation.
 *
 * @author Yazad Khambata
 */
enum EntityUpdateOperation {
    ADD {
        @Override
        <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
            ((Growable)mutableProperty).enrichAdd(completeEntity, value)
        }
    },

    UPDATE {
        @Override
        <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
            performUpdate(mutableProperty, completeEntity, value)
        }
    },

    DELETE {
        @Override
        <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
            ((Shrinkable)mutableProperty).enrichRemove(completeEntity, value)
        }
    },

    OVERRIDE {
        @Override
        <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
            ((Overridable)mutableProperty).enrichOverride(completeEntity, value)
        }
    },

    UPSERT {
        @Override
        <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
            performUpdate(mutableProperty, completeEntity, value)
        }
    },

    DELETENOP("deleteNOP") {
        @Override
        <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
            ((Shrinkable)mutableProperty).enrichRemove(completeEntity, value)
        }
    };

    private String opName

    EntityUpdateOperation() {
        this(null)
    }

    EntityUpdateOperation(final String opName) {
        this.opName = opName
    }

    String getOpName() {
        opName?:this.name().toLowerCase()
    }

    abstract <V> void perform(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value)


    private <V> void performUpdate(final MutableProperty mutableProperty, final CompleteEntity completeEntity, final V value) {
        //Update value operation can be improved by creating "Updatable fields" as its own interface.
        throw new UnsupportedOperationException()
    }
}
