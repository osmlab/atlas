package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.SchemeSupport
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method

/**
 * @author Yazad Khambata
 */
class ClasspathSchemeTest {
    private static final Logger log = LoggerFactory.getLogger(ClasspathSchemeTest.class)

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
