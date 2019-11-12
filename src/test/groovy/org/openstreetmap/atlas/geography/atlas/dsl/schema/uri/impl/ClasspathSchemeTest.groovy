package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl

import org.apache.commons.io.IOUtils
import org.junit.Ignore
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.SchemeSupport
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas
import org.openstreetmap.atlas.streaming.resource.File
import org.openstreetmap.atlas.streaming.resource.OutputStreamWritableResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method
import java.nio.charset.Charset

/**
 * @author Yazad Khambata
 */
class ClasspathSchemeTest {
    private static final Logger log = LoggerFactory.getLogger(ClasspathSchemeTest.class)

    @Test
    @Ignore
    void testDynamicClasspath() {
        def root = "/tmp/${UUID.randomUUID()}"
        def fileRoot = new java.io.File(root)

        def cp = "abc/xyz/pqr"
        def fileLoc = "${root}/${cp}"
        def dir = new java.io.File(fileLoc)
        dir.mkdirs()

        def f = new java.io.File("${fileLoc}/HelloWorld.txt")
        def text = "The quick brown fox jumps over the lazy dog."
        f << text

        log.info("${f}")

        final ClassLoader classLoader = ClassLoader.getSystemClassLoader()
        final Class classloaderClass = classLoader.getClass()
        final Method method = classloaderClass.getDeclaredMethod("addURL", [URL.class] as Class[])
        method.setAccessible(true)
        method.invoke(classLoader, [fileRoot.toURI().toURL()] as Object[])

        assert text == IOUtils.toString(this.class.getClassLoader().getResourceAsStream("abc/xyz/pqr/HelloWorld.txt"), Charset.defaultCharset())
    }

    @Test
    @Ignore
    void testClasspathResource() {
        final Map<String, Atlas> atlasMapping = [
                "atlas1/something1.atlas": loadOsmXml("classpath:${TestConstants.ALCATRAZ_CLASSPATH}"),
                "atlas1/something2.atlas": loadOsmXml("classpath:${TestConstants.BUTTERFLY_PARK_CLASSPATH}"),
                "atlas2/Alcatraz.atlas"  : loadOsmXml("classpath:${TestConstants.ALCATRAZ_CLASSPATH}"),
                "atlas2/Butterfly.atlas" : loadOsmXml("classpath:${TestConstants.BUTTERFLY_PARK_CLASSPATH}")
        ]

        final String classpathRoot = "/tmp/${UUID.randomUUID()}"

        def classpathRootAsFile = new java.io.File(classpathRoot)
        classpathRootAsFile.mkdirs()

        new java.io.File("${classpathRoot}/atlas1").mkdirs()
        new java.io.File("${classpathRoot}/atlas2").mkdirs()

        atlasMapping.entrySet().stream().forEach { entry ->
            final String relativePath = entry.getKey()
            final Atlas atlas = PackedAtlas.cloneFrom(entry.getValue())

            final String path = "$classpathRoot/${relativePath}"
            log.info(path)
            atlas.save(new OutputStreamWritableResource(new FileOutputStream(new java.io.File(path))))

            final Atlas reloadedAtlas = new AtlasResourceLoader().load(new File(new java.io.File(path)))
            assert reloadedAtlas
        }

        addToClasspath(classpathRootAsFile)

        final String uri = "classpath:/atlas1/something1.atlas,something2.atlas;/atlas2/Alcatraz.atlas,Butterfly.atlas"
        final Atlas atlas = loadAtlas(uri)

        println atlas
    }

    private void addToClasspath(java.io.File classpathRootAsFile) {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader()
        final Class classLoaderClass = classLoader.getClass()
        final Method method = classLoaderClass.getDeclaredMethod("addURL", [URL.class] as Class[])
        method.setAccessible(true)
        method.invoke(classLoader, [classpathRootAsFile.toURI().toURL()] as Object[])
    }

    @Test
    void testOsmXmlClasspathResource() {
        final String uri = "classpath:/data/Alcatraz/Alcatraz.osm;/data/ButterflyPark/ButterflyPark.osm"

        assert loadOsmXml(uri).size().nodeNumber == 167
    }

    private Atlas loadAtlas(final String uri) {
        SchemeSupport.instance.load(uri)
    }

    private Atlas loadOsmXml(final String uri) {
        SchemeSupport.instance.loadOsmXml(uri)
    }
}
