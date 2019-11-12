package org.openstreetmap.atlas.geography.atlas.dsl.query.result

import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Results that can be committed.
 *
 * @author Yazad Khambata
 */
interface CommitableResult<E extends AtlasEntity> extends Result<E> {
    /**
     * The original Atlas to commit on.
     */
    AtlasMediator getAtlasMediatorToCommitOn()

    Change getChange()


}
