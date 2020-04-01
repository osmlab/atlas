package org.openstreetmap.atlas.geography.atlas.dsl.path

import org.junit.Test

import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * @author Yazad Khambata
 */
class PathUtilTest {

    @Test
    void testContentsFromClasspath() {
        final PathQueryFilePackCollection classpathQueryFilePackCollection =
                PathUtil.instance.aqlFilesFromClasspath("aql-files")

        classpathQueryFilePackCollection.stream().forEach { classpathQueryFilePack ->
            println classpathQueryFilePack
        }
    }

    @Test
    void testContentsFromPath() {
        final String physicalPathAsStr = "/tmp/aql-files"
        def classpath = "aql-files"

        def pathToDelete = Paths.get(physicalPathAsStr)
        PathUtil.instance.deleteRecursivelyQuietly(pathToDelete)

        final URL url = this.getClass().getClassLoader().getResource(classpath)
        PathUtil.instance.copyFolder(Paths.get(url.toURI()), Paths.get(physicalPathAsStr))

        final PathQueryFilePackCollection pathQueryFilePackCollection1 = PathUtil.instance.aqlFilesFromPath(physicalPathAsStr)
        final PathQueryFilePackCollection pathQueryFilePackCollection2 = PathUtil.instance.aqlFilesFromClasspath(classpath)

        assert pathQueryFilePackCollection1.stream().sorted().collect(Collectors.toList()) == pathQueryFilePackCollection2.stream().sorted().collect(Collectors.toList())
    }
}
