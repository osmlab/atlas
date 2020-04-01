package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl

import groovy.io.FileType
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.streaming.resource.File
import org.openstreetmap.atlas.streaming.resource.InputStreamResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * File scheme implementation.
 *
 * @author Yazad Khambata
 */
@Singleton
class FileScheme extends AbstractScheme {
    static final String SCHEME = "file"
    private static final Logger log = LoggerFactory.getLogger(FileScheme.class);

    @Override
    String name() {
        SCHEME
    }

    /**
     * @param uriExcludingScheme - the dir path (not file) and no file: to be prepended.
     * @return
     */
    @Override
    Atlas loadAtlas(final String uriExcludingScheme) {
        log.info("Load Recursive: ${uriExcludingScheme}")
        final Atlas atlas = new AtlasResourceLoader().loadRecursively(new File(uriExcludingScheme))
        atlas
    }

    /**
     * @param uriExcludingScheme - the dir path (not file) and no file: to be prepended.
     * @return
     */
    @Override
    Atlas loadOsmXml(final String uriExcludingScheme) {
        final List<File> list = toFiles(uriExcludingScheme)

        final List<Atlas> atlases = list.stream()
                .map { file ->
                    byte[] bytes = file.readBytesAndClose()

                    Valid.notEmpty bytes

                    final Supplier<InputStreamResource> supplier = { new ByteArrayInputStream(bytes) }
                    final InputStreamResource inputStreamResource = new InputStreamResource(supplier)

                    osmInputStreamResourceToAtlas(inputStreamResource)
                }
                .collect(Collectors.toList())

        merge(atlases)
    }

    @Override
    InputStream loadFile(final String uriExcludingScheme) {
        new FileInputStream(new java.io.File(uriExcludingScheme))
    }

    private List<File> toFiles(String uriExcludingScheme) {
        Valid.notEmpty uriExcludingScheme

        def dir = new java.io.File(uriExcludingScheme)

        Valid.isTrue dir.isDirectory() && dir.canRead(), "${uriExcludingScheme} needs to be a directory (${dir.isDirectory()}) with read access (${dir.canRead()})."

        final List<File> list = []

        dir.eachFileRecurse(FileType.FILES) { file ->
            list << new File(file.toString())
        }
        list
    }
}
