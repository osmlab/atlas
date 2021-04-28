package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.ClasspathScheme
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.FileScheme
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

/**
 * Scheme (example file://, classpath://, factory://, ...) support registry.
 *
 * @author Yazad Khambata
 */
@Singleton
class SchemeSupport {

    private Map<String, Scheme> schemeMapping = [
            (FileScheme.SCHEME):FileScheme.instance,
            (ClasspathScheme.SCHEME):ClasspathScheme.instance
    ]

    static final String DEFAULT = FileScheme.SCHEME

    void register(final String schemeName, final Scheme scheme) {
        Valid.isTrue schemeName == scheme.name()

        schemeMapping.put(schemeName, scheme)
    }

    protected String toScheme(final String uri) {
        Valid.notEmpty uri

        //Default encoding is file:

        final int indexOfColon = uri.indexOf(/:/)

        if (indexOfColon < 0) {
            return SchemeSupport.DEFAULT
        }

        return uri.substring(0, indexOfColon)
    }

    protected Scheme toSchemeHandler(final String uri) {
        final String schemeName = toScheme(uri)

        final Scheme schemeHandler = schemeMapping[schemeName]

        Valid.notEmpty schemeHandler, "No handler found for ${uri}."

        schemeHandler
    }

    protected String toUriExcludingScheme(String uri) {
        uri.replace("${toScheme(uri)}:", "")
    }

    Atlas load(final String uri) {
        final Scheme schemeHandler = toSchemeHandler(uri)

        schemeHandler.loadAtlas(toUriExcludingScheme(uri))
    }

    Atlas loadOsmXml(final String uri) {
        final Scheme schemeHandler = toSchemeHandler(uri)

        schemeHandler.loadOsmXml(toUriExcludingScheme(uri))
    }

    InputStream loadFile(final String uri) {
        final Scheme schemeHandler = toSchemeHandler(uri)
        schemeHandler.loadFile(toUriExcludingScheme(uri))
    }
}
