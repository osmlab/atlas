package org.openstreetmap.atlas.geography.atlas.dsl.path

import org.apache.commons.lang3.Validate
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

/**
 * A util to work with Java NIO Paths.
 *
 * @author Yazad Khambata
 */
@Singleton
class PathUtil {
    private static final Logger log = LoggerFactory.getLogger(PathUtil.class);

    PathQueryFilePackCollection aqlFilesFromClasspath(final String classpathDirectory) {
        Valid.notEmpty classpathDirectory

        final URL url = this.getClass().getClassLoader().getResource(classpathDirectory)

        Validate.notNull(url, "url is NULL for ${classpathDirectory}")

        final Path basePath = Paths.get(url.toURI())
        aqlFilesFromBasePath(basePath)
    }

    PathQueryFilePackCollection aqlFilesFromPath(String aqlHomePath) {
        final Path basePath = Paths.get(aqlHomePath)
        aqlFilesFromBasePath(basePath)
    }

    void copyFolder(Path src, Path dest) {
        Files
                .walk(src)
                .forEach {
                    source -> copy(source, dest.resolve(src.relativize(source)))
                }
    }

    private void copy(Path source, Path dest) {
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
    }

    void deleteRecursivelyQuietly(Path pathToDelete) {
        try {
            Files.walk(pathToDelete).sorted(Comparator.reverseOrder()).forEach { path -> Files.delete(path) }
        } catch (Exception e) {
            log.warn("delete failed for ${pathToDelete}.", e)
        }
    }

    private PathQueryFilePackCollection aqlFilesFromBasePath(Path basePath) {
        final List<Path> paths = Files.walk(basePath)
                .filter { path -> !Files.isDirectory(path) }
                .collect(Collectors.toList())

        final Map<String, List<Path>> groupedFiles = paths.stream()
                .collect(Collectors.groupingBy { Path path -> path.toString().replace(".sig", "") })

        final PathQueryFilePackCollection classpathQueryFilePackCollection =
                groupedFiles.entrySet().stream()
                        .map { entry -> PathQueryFilePack.from(entry) }
                        .collect(
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        { list -> new PathQueryFilePackCollection(list) }
                                )
                        )

        classpathQueryFilePackCollection
    }
}
