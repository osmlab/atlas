package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.openstreetmap.atlas.geography.atlas.dsl.field.Constrainable
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.util.SingletonMap
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
@Singleton
class ConstraintGenerator {
    /**
     *
     * was(in: [1, 2, 3])
     * was(within: [ [10.5, 11.5], [12.5, 15.5], [16, 17], [5.5, 4.5], [10.5, 11.5] ])
     * was(eq: 10)
     * was(not: 10) OR is(ne: 10)
     * was(lt: 10)
     * was(gt: 10)
     * was(le: 10)
     * was(ge: 10)
     *
     * @param params
     * @param field
     * @param bestCandidateScanStrategy
     * @param atlasEntityClass
     * @return
     */
    def <E extends AtlasEntity> Constraint was(final Map<String, ?> params, final Field field, final ScanType bestCandidateScanStrategy, final Class<E> atlasEntityClass) {
        final Map<String, ?> entry = new SingletonMap<>(params)

        final String operationAsStr = entry.getKey()

        final Constraint constraint = BasicConstraint.builder()
                    .field((Constrainable) field)
                    .operation(BinaryOperations.fromToken(operationAsStr))
                    .valueToCheck(entry.getValue())
                    .atlasEntityClass(atlasEntityClass)
                    .bestCandidateScanType(bestCandidateScanStrategy)
                    .build()

        constraint
    }

    def <E extends AtlasEntity> Constraint was(final Map<String, ?> params, final Field field, final Class<E> atlasEntityClass) {
        this.was(params, field, ScanType.FULL, atlasEntityClass)
    }

    /**
     *
     * has(tag: [amenity: "college", "name:en": "Copenhagen Hospitality College"])
     * has(tag: ["amenity"])
     *
     * @param params
     * @return
     */
    Constraint has(final Map<String, ?> params, final Field field) {
        was(params, field)
    }
}
