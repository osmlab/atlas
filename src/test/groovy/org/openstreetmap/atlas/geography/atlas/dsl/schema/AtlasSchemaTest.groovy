package org.openstreetmap.atlas.geography.atlas.dsl.schema

import org.apache.commons.io.IOUtils
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.ClasspathScheme
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.FileScheme
import org.openstreetmap.atlas.geography.atlas.dsl.util.StreamUtil
import org.openstreetmap.atlas.geography.atlas.items.Node

/**
 * @author Yazad Khambata
 */
class AtlasSchemaTest extends AbstractAQLTest {

    @Test
    void testOsmFileLoadFromClasspath() {
        def classpath = "/data/Alcatraz/Alcatraz.osm"
        def atlas = ClasspathScheme.instance.loadOsmXml(classpath)
        assert atlas.numberOfNodes() == 56
    }

    @Test
    void testOsmFileLoadFromLocalFileSystem() {
        def fileName1 = "Alcatraz.osm"

        def baseClasspath = "/data/Alcatraz"

        def physicalPath = "/tmp/${UUID.randomUUID().toString()}"

        new File(physicalPath).mkdir()

        [fileName1].stream().forEach { fileName ->
            final String classpath = "${baseClasspath}/${fileName}"

            final InputStream inputStream = this.getClass().getResourceAsStream(classpath)

            final OutputStream outputStream = new FileOutputStream(new File("${physicalPath}/${fileName}"))
            IOUtils.copy(inputStream, outputStream)

            outputStream.close()
        }

        final Atlas atlas = FileScheme.instance.loadOsmXml(physicalPath)
        assert atlas.numberOfNodes() == 56
    }

    @Test
    void testAtlasFileLoad() {
        final AtlasSchema atlas1 = usingAlcatraz()
        final Iterable<Node> all = atlas1.node.getAll()

        assert StreamUtil.stream(all).count() == 56
    }


    @Test
    void testEqualsAndHashCode() {
        final AtlasSchema atlas1 = usingAlcatraz()
        final AtlasSchema atlas2 = usingAlcatraz()
        final AtlasSchema atlas3 = usingButterflyPark()
        final AtlasSchema atlas4 = usingButterflyPark()

        assert atlas1 == atlas1
        assert atlas1 == atlas2
        assert atlas1.hashCode() == atlas2.hashCode()

        assert atlas3 == atlas3
        assert atlas3 == atlas4
        assert atlas3.hashCode() == atlas4.hashCode()

        assert atlas1 != atlas3
        assert atlas1.hashCode() != atlas3.hashCode()
    }
}
