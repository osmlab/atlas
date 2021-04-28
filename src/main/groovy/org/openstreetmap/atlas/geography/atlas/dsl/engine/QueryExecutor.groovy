package org.openstreetmap.atlas.geography.atlas.dsl.engine

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result

/**
 * Used as an integration point between other applications and processes.
 * The query is expected as an input String or a Reader.
 *
 * @author Yazad Khambata
 */
interface QueryExecutor {

    Result exec(Atlas atlas, String queryAsString, final String signature)

    Result exec(Atlas atlas, Reader queryAsReader, final String signature)
}
