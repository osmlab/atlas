package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A constrained field is a field that can be used in the where clause of a statement.
 *
 * @author Yazad Khambata
 */
interface Constrainable extends Readable {

    def <E extends AtlasEntity> Constraint was(Map params, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint has(Map params, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint was(Map params, Field delegateField, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint has(Map params, Field delegateField, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint was(Map params, ScanType bestCandidateScanStrategy, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint has(Map params, ScanType bestCandidateScanStrategy, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint was(Map params, Field delegateField, ScanType bestCandidateScanStrategy, Class<E> atlasEntityClass)

    def <E extends AtlasEntity> Constraint has(Map params, Field delegateField, ScanType bestCandidateScanStrategy, Class<E> atlasEntityClass)

}
