package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.ClasspathScheme
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.FileScheme

/**
 * @author Yazad Khambata
 */
class SchemeSupportTest {

    @Test
    void testToScheme() {
        assert SchemeSupport.instance.toScheme("/tmp/data/atlas/XYZ") == SchemeSupport.DEFAULT
        assert SchemeSupport.instance.toScheme("file:/tmp/data/atlas/XYZ") == "file"
        assert SchemeSupport.instance.toScheme("classpath:/tmp/data/atlas/XYZ") == "classpath"
        assert SchemeSupport.instance.toScheme("factory:com.acme.myfactory.AtlasFactory#loadMyAtlas()") == "factory"
    }

    @Test
    void testToSchemeHandler() {
        assert SchemeSupport.instance.toSchemeHandler("/tmp/data/atlas/XYZ") == FileScheme.instance
        assert SchemeSupport.instance.toSchemeHandler("file:/tmp/data/atlas/XYZ") == FileScheme.instance
        assert SchemeSupport.instance.toSchemeHandler("classpath:/tmp/data/atlas/XYZ") == ClasspathScheme.instance
    }

    @Test
    void testLoadFile() {
        final InputStream inputStream = SchemeSupport.instance.loadFile("classpath:/data/polygon/jsonl/samples.jsonl")

        assert inputStream != null
        assert inputStream.available() > 0

        def count = inputStream.readLines().stream().count()
        assert count == 15
    }
}
