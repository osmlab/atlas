package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.apache.commons.io.IOUtils
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.streaming.resource.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getUsing

/**
 * @author Yazad Khambata
 */
class UsingTest {
    private static final Logger log = LoggerFactory.getLogger(UsingTest.class);

    @Test
    void testUsingAtlasFromClasspath() {
        def atlasSchema = using "classpath:/org/openstreetmap/atlas/geography/atlas/ECU_6-16-31.atlas"
        def expectedNodeSize = 5

        verify(atlasSchema, expectedNodeSize)
    }

    @Test
    void testUsingTextAtlasFromClasspath() {
        def atlasSchema = using "classpath:/data/Alcatraz/Alcatraz.atlas.txt"
        def expectedNodeSize = 56

        verify(atlasSchema, expectedNodeSize)
    }

    @Test
    void testUsingAtlasFromFile() {
        final AtlasSchema atlasSchemaTemp = using "classpath:/org/openstreetmap/atlas/geography/atlas/ECU_6-16-31.atlas"
        final Atlas atlas = atlasSchemaTemp.atlasMediator.atlas
        def physicalPath = "/tmp/${UUID.randomUUID()}"
        atlas.save(new File("${physicalPath}/ECU_6-16-31.atlas"))

        final AtlasSchema atlasSchema = using "file:${physicalPath}"

        def expectedNodeSize = 5

        verify(atlasSchema, expectedNodeSize)
    }

    @Test
    void testUsingTextAtlasFromFile() {
        final InputStream inputStream = UsingTest.class.getResourceAsStream("/data/Alcatraz/Alcatraz.atlas.txt")

        def asText = IOUtils.toString(inputStream, Charset.defaultCharset())

        final String physicalPath = "/tmp/${UUID.randomUUID()}"
        new java.io.File(physicalPath).mkdirs()
        final String filePath = "${physicalPath}/Alcatraz.atlas.txt"
        new java.io.File(filePath) << asText

        final AtlasSchema atlasSchema = using "file:${physicalPath}"
        def expectedNodeSize = 56

        verify(atlasSchema, expectedNodeSize)
    }

    @Test
    void testUsingMultipleTextAtlasFromClasspath1() {
        def atlasSchema1 = using "classpath:/data/Alcatraz/Alcatraz.atlas.txt"
        def expectedNodeSize1 = 56

        verify(atlasSchema1, expectedNodeSize1)

        def atlasSchema2 = using "classpath:/data/ButterflyPark/ButterflyPark.atlas.txt"
        def expectedNodeSize2 = 111

        verify(atlasSchema2, expectedNodeSize2)

        def atlasSchema3 = using "classpath:/data/Alcatraz/Alcatraz.atlas.txt;/data/ButterflyPark/ButterflyPark.atlas.txt"
        def expectedNodeSize3 = expectedNodeSize1 + expectedNodeSize2

        verify(atlasSchema3, expectedNodeSize3)
    }

    @Test
    void testUsingMultipleTextAtlasFromClasspath2() {
        def atlasSchema = using "classpath:/org/openstreetmap/atlas/geography/atlas/command/DNK_Copenhagen/DNK_1.atlas.txt,DNK_2.atlas.txt,DNK_3.atlas.txt;/org/openstreetmap/atlas/geography/atlas/raw/sectioning/oneWayRing.atlas.txt,simpleBiDirectionalLine.atlas.txt"

        multiVerify(atlasSchema)
    }

    @Test
    void testUsingMultipleTextAtlasFromFile() {
        final Map<String, List<String>> classpathData = [
                '/org/openstreetmap/atlas/geography/atlas/command/DNK_Copenhagen': ['DNK_1.atlas.txt', 'DNK_2.atlas.txt', 'DNK_3.atlas.txt'],
                '/org/openstreetmap/atlas/geography/atlas/raw/sectioning'        : ['oneWayRing.atlas.txt', 'simpleBiDirectionalLine.atlas.txt']
        ]

        final String uuid = UUID.randomUUID().toString()

        classpathData.entrySet().stream().forEach({ entry ->
            final String root = entry.getKey()
            final List<String> fileNames = entry.getValue()

            def physicalPath = "/tmp/${uuid}/${root.hashCode()}"

            log.info("physicalPath: ${physicalPath}.")

            new java.io.File(physicalPath).mkdirs()
            fileNames.stream().forEach({ fileName ->
                final InputStream inputStream = UsingTest.class.getResourceAsStream("${root}/${fileName}")
                def physicalFilePath = "${physicalPath}/${fileName}"
                new java.io.File(physicalFilePath) << IOUtils.toString(inputStream, Charset.defaultCharset())
            })
        })

        def atlasSchema = using "file:/tmp/${uuid}"

        log.info("SIZE: ${atlasSchema.atlasMediator.atlas.size()}")

        multiVerify(atlasSchema)
    }

    private void multiVerify(AtlasSchema atlasSchema) {
        assert atlasSchema.node.all.size() == 2
        assert atlasSchema.point.all.size() == 1 + 12 + 4
        assert atlasSchema.edge.all.size() == 1
        assert atlasSchema.line.all.size() == 1 + 1
        assert atlasSchema.area.all.size() == 1
        assert atlasSchema.relation.all.size() == 0
    }

    private void verify(AtlasSchema atlasSchema, int expectedNodeSize) {
        def size = atlasSchema.node.all.size()
        assert size == expectedNodeSize
    }
}
