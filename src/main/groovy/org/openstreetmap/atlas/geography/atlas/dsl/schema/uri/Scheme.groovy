package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri

import org.openstreetmap.atlas.geography.atlas.Atlas

/**
 * A sample URI scheme like file or classpath.
 *
 * @author Yazad Khambata
 */
interface Scheme {
    String name()

    Atlas loadAtlas(final String uriExcludingScheme)

    Atlas loadOsmXml(final String uriExcludingScheme)

    /**
     * Allows only one file per call. Expects the file and not the parent directory.
     *
     * @param uriExcludingScheme
     * @return
     */
    InputStream loadFile(final String uriExcludingScheme)
}
