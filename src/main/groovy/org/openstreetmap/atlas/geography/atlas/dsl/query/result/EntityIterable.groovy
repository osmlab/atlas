package org.openstreetmap.atlas.geography.atlas.dsl.query.result

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Stream

/**
 * Contract to iterate over a collection of entities in a result.
 *
 * @author Yazad Khambata
 */
@PackageScope
interface EntityIterable<E extends AtlasEntity> {
    Iterator<E> entityIterator()

    Iterator<E> entityIterator(final AtlasMediator atlasMediator)

    Stream<E> entityStream()

    Stream<E> entityStream(final AtlasMediator atlasMediator)
}
