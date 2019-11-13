package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity

/**
 * A field whose contents can grow or shrink.
 *
 * @author Yazad Khambata
 */
@PackageScope
interface Elastic<C extends CompleteEntity, AV, RV> extends Growable<C, AV>, Shrinkable<C, RV> {
}
