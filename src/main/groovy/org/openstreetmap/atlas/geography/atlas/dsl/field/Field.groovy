package org.openstreetmap.atlas.geography.atlas.dsl.field

/**
 * A Field of a table.
 *
 * @author Yazad Khambata
 */
interface Field {

    String ITSELF = "_"

    String getName()

    String getAlias()
}
