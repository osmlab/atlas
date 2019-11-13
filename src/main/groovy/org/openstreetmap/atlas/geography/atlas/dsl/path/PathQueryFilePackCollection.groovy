package org.openstreetmap.atlas.geography.atlas.dsl.path

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.util.stream.Stream

/**
 * A collection of one or more PathQueryFilePack(s).
 *
 * @author Yazad Khambata
 */
@ToString
@EqualsAndHashCode
class PathQueryFilePackCollection implements Iterable<PathQueryFilePack> {
    List<PathQueryFilePack> classpathQueryFilePackList

    PathQueryFilePackCollection(final List<PathQueryFilePack> classpathQueryFilePackList) {
        this.classpathQueryFilePackList = classpathQueryFilePackList
    }

    @Override
    Iterator<PathQueryFilePack> iterator() {
        classpathQueryFilePackList.iterator()
    }

    Stream<PathQueryFilePack> stream() {
        classpathQueryFilePackList.stream()
    }
}
