package org.openstreetmap.atlas.geography.atlas.dsl.mutant

import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

import java.util.stream.Collectors

/**
 * A domain that represents the nature of operations supported during a mutation on a Mutable field.
 *
 * @author Yazad Khambata
 */
class EntityUpdateBehaviorsSupported {
    /**
     * Add an item inside the values.
     */
    private boolean add = false

    /**
     * Update one of the values.
     */
    private boolean update = false

    /**
     * Delete one of the items in the value.
     */
    private boolean delete = false

    /**
     * Update the complete value.
     */
    private boolean override = false

    /**
     * Upsert which is a <a href="https://en.wiktionary.org/wiki/Appendix:Glossary#blend">blend</a>
     * of update and insert.
     *
     * See <a href="https://en.wiktionary.org/wiki/upsert">wiktionary</a>.
     */
    private boolean upsert = false

    /**
     * Delete <a href="https://en.wikipedia.org/wiki/NOP_(code)">No Operation (NOP or NOOP)</a>
     * means if the value being deleted doesn't exist it completes silently.
     */
    private boolean deleteNOP = false

    boolean isEntityUpdateBehavior(final EntityUpdateOperation entityUpdateBehavior) {
        this[entityUpdateBehavior.opName]
    }

    Set<EntityUpdateOperation> supportedEntityUpdateBehavior() {
        Arrays.stream(EntityUpdateOperation.values()).filter { isEntityUpdateBehavior(it) }.collect(Collectors.toSet())
    }

    private static class SupportedUpdateBehaviorBuilder {

        private boolean add = false
        private boolean update = false
        private boolean delete = false
        private boolean override = false
        private boolean upsert = false
        private boolean deleteNOP = false

        SupportedUpdateBehaviorBuilder add() {
            this.add = true
            this
        }

        SupportedUpdateBehaviorBuilder update() {
            this.update = true
            this
        }

        SupportedUpdateBehaviorBuilder delete() {
            this.delete = true
            this
        }

        SupportedUpdateBehaviorBuilder override() {
            this.override = true
            this
        }

        SupportedUpdateBehaviorBuilder upsert() {
            this.upsert = true

            this
        }

        SupportedUpdateBehaviorBuilder deleteNOP() {
            this.deleteNOP = true

            this
        }

        EntityUpdateBehaviorsSupported build() {
            final EntityUpdateBehaviorsSupported supportedUpdateBehavior = new EntityUpdateBehaviorsSupported()
            supportedUpdateBehavior.add = this.add
            supportedUpdateBehavior.update = this.update
            supportedUpdateBehavior.delete = this.delete
            supportedUpdateBehavior.override = this.override

            if (upsert) {
                Valid.isTrue add && update, "Add and Upsert must be supported to support UPSERT."
            }
            supportedUpdateBehavior.upsert = upsert

            if (deleteNOP) {
                Valid.isTrue delete, "Delete must be supported to support DELETE-NOP."
            }
            supportedUpdateBehavior.deleteNOP = deleteNOP

            supportedUpdateBehavior
        }
    }

    static SupportedUpdateBehaviorBuilder builder() {
        new SupportedUpdateBehaviorBuilder()
    }
}
