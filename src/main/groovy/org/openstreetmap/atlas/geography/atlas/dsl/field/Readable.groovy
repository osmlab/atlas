package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A field whose value can be read either by the AQL user or the engine.
 *
 * @author Yazad Khambata
 */
@PackageScope
interface Readable {
    def read(final AtlasEntity atlasEntity)
}
