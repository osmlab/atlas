package org.openstreetmap.atlas.geography.atlas.dsl.mutant

/**
 * Currently the only entity update supported but Geometry and relation updates will be supported in the future.
 *
 * @author Yazad Khambata
 */
enum EntityUpdateType {

    TAGS(EntityUpdateBehaviorsSupported.builder().add().update().delete().override().upsert().deleteNOP().build());

    final EntityUpdateBehaviorsSupported entityUpdateBehaviorsSupported

    EntityUpdateType(final EntityUpdateBehaviorsSupported entityUpdateBehaviorsSupported) {
        this.entityUpdateBehaviorsSupported = entityUpdateBehaviorsSupported
    }
}
