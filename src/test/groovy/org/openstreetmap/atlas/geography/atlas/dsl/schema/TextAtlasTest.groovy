package org.openstreetmap.atlas.geography.atlas.dsl.schema

import org.junit.Assert
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.ClasspathScheme
import org.openstreetmap.atlas.streaming.resource.File
import org.openstreetmap.atlas.streaming.resource.OutputStreamWritableResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Yazad Khambata
 */
class TextAtlasTest {
    private static final Logger log = LoggerFactory.getLogger(TextAtlasTest.class)

    @Test
    void test1() {
        def map = [
                ALCATRAZ : TestConstants.ALCATRAZ_CLASSPATH,
                BUTTERFLY: TestConstants.BUTTERFLY_PARK_CLASSPATH
        ]

        map.forEach({ key, value ->
            final Atlas atlas = ClasspathScheme.instance.loadOsmXml(value)

            final String textAtlasPath = "/tmp/${key}-${UUID.randomUUID()}.atlas.txt"

            atlas.saveAsText(new OutputStreamWritableResource(new FileOutputStream(new java.io.File(textAtlasPath))))

            final Atlas textAtlas = new TextAtlasBuilder().read(new File(new java.io.File(textAtlasPath)))

            Assert.assertEquals(atlas, textAtlas)
        })
    }

    @Test
    void test2() {
        def map = [
                BUTTERFLY: TestConstants.BUTTERFLY_PARK_CLASSPATH,
                ALCATRAZ : TestConstants.ALCATRAZ_CLASSPATH,
        ]

        map.forEach({ key, value ->
            final Atlas atlas = ClasspathScheme.instance.loadOsmXml(value)

            println "${key} : ${atlas.size()}"

            final String textAtlasPath = "/tmp/${key}-${System.nanoTime()}.atlas.txt"
            atlas.saveAsText(new OutputStreamWritableResource(new FileOutputStream(new java.io.File(textAtlasPath))))
            final Atlas textAtlas = new TextAtlasBuilder().read(new File(new java.io.File(textAtlasPath)))

            Assert.assertEquals(atlas, textAtlas)
        })
    }
}
