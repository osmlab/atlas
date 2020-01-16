package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.streaming.resource.InputStreamResource

import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * Classpath scheme implementation.
 *
 * @author Yazad Khambata
 */
@Singleton
class ClasspathScheme extends AbstractScheme {
    static final String SCHEME = "classpath"

    @Override
    String name() {
        SCHEME
    }

    private List<InputStreamResource> toInputStreamResources(String uriExcludingScheme) {
        Valid.notEmpty uriExcludingScheme

        final String[] dirsWithFiles = uriExcludingScheme.split(";")

        final List<InputStreamResource> inputStreamResources = Arrays.stream(dirsWithFiles).flatMap { String dirWithFiles ->
            final String[] files = dirWithFiles.split(",")

            final String dir = files[0].substring(0, files[0].lastIndexOf("/") + 1)

            files[0] = files[0].replace(dir, "")

            Arrays.stream(files).map { file -> "${dir}${file}" }
        }.map { classpathFilePath ->
            toInputResource(classpathFilePath)
        }.collect(Collectors.toList())
        inputStreamResources
    }

    protected InputStreamResource toInputResource(final String classpathFilePath) {
        final Supplier<InputStream> supplierOfInputStream = {
            final InputStream inputStream = getResourceAsStreamStrict(classpathFilePath)

            inputStream
        }

        def inputStreamResource = new InputStreamResource(supplierOfInputStream)
        inputStreamResource
    }

    /**
     *
     * @param uriExcludingScheme - example /data/atlas/XYZ/file1.atlas,file2.atlas;/data/atlas/ABC/file1.atlas,file2.atlas
     * @return
     */
    @Override
    Atlas loadAtlas(final String uriExcludingScheme) {
        List<InputStreamResource> inputStreamResources = toInputStreamResources(uriExcludingScheme)

        final Atlas atlas = new AtlasResourceLoader().load(inputStreamResources)

        atlas
    }

    @Override
    Atlas loadOsmXml(final String uriExcludingScheme) {
        final List<InputStreamResource> inputStreamResourceList = toInputStreamResources(uriExcludingScheme)

        final List<Atlas> atlases = inputStreamResourceList.stream()
                .map { inputStreamResource -> osmInputStreamResourceToAtlas(inputStreamResource) }
                .collect(Collectors.toList())

        merge(atlases)
    }

    @Override
    InputStream loadFile(final String uriExcludingScheme) {
        getResourceAsStreamStrict(uriExcludingScheme)
    }

    private InputStream getResourceAsStreamStrict(String uriExcludingScheme) {
        final InputStream inputStream = this.getClass().getResourceAsStream(uriExcludingScheme)

        Valid.isTrue inputStream != null, "[${uriExcludingScheme}] resuled in NULL inputStream."

        inputStream
    }
}
