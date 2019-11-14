package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl

import org.apache.commons.io.IOUtils
import org.junit.Ignore
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.SchemeSupport
import org.openstreetmap.atlas.streaming.resource.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Yazad Khambata
 */
class FileSchemeTest extends AbstractAQLTest {
    private static final Logger log = LoggerFactory.getLogger(FileSchemeTest.class)

    private String saveInTmp(List<String> fileNames, String baseClasspath) {
        final String fileBasePath = "/tmp/${UUID.randomUUID()}"

        new File(fileBasePath).mkdirs()

        fileNames.stream()
                .map { fileName -> "${baseClasspath}${fileName}" }
                .map { classpath -> ClassLoader.getResourceAsStream(classpath) }
                .forEach { InputStream inputStream ->
                    final String physicalFileName = "${fileBasePath}/${UUID.randomUUID()}.atlas"
                    OutputStream outputStream = new FileOutputStream(physicalFileName)
                    IOUtils.copy(inputStream, outputStream)

                    outputStream.flush()

                    outputStream.close()
                }

        assert new java.io.File(fileBasePath).list().size() == fileNames.size()
        fileBasePath
    }

    @Test
    void testLoad() {
        final String basePath = "/tmp/${UUID.randomUUID()}"
        new java.io.File(basePath).mkdirs()

        def atlas1 = usingButterflyPark().atlasMediator.atlas
        def atlas2 = usingAlcatraz().atlasMediator.atlas

        atlas1.cloneToPackedAtlas().save(new File(new java.io.File("${basePath}/ButterflyPark.atlas")))
        atlas2.cloneToPackedAtlas().save(new File(new java.io.File("${basePath}/Alcatraz.atlas")))

        final Atlas reloadedAtlas = SchemeSupport.instance.load("file:${basePath}")

        log.info("reloadedAtlas size:: ${reloadedAtlas.size()}")

        def number = reloadedAtlas.size().nodeNumber
        assert number == 167
    }

    @Test
    @Ignore
    void testLoadOsmXml() {
        final String baseClasspath = "/data/"

        final List<String> fileNames = ["Alcatraz/Alcatraz.osm", "ButterflyPark/ButterflyPark.osm"]

        String fileBasePath = saveInTmp(fileNames, baseClasspath)

        assert SchemeSupport.instance.loadOsmXml("file:${fileBasePath}").size().nodeNumber == 167
    }
}
