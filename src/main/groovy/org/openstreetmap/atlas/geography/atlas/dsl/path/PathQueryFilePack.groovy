package org.openstreetmap.atlas.geography.atlas.dsl.path

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

import java.nio.file.Path

/**
 * A domain the represents the query and related information like file name and signature.
 *
 * @author Yazad Khambata
 */
@Builder
@ToString(includeNames = true, excludes = ["fileName"])
@EqualsAndHashCode(excludes = ["fileName"])
class PathQueryFilePack implements Comparable<PathQueryFilePack> {
    String fileName
    String query
    String signature

    static PathQueryFilePack from(Map.Entry<String, List<Path>> entry) {
        final String key = entry.getKey()
        final List<Path> values = entry.getValue()

        Valid.isTrue values.size() >= 1 && values.size() <= 2

        String query = ""
        String signature = ""

        for (def path : values) {
            if (path.toString() == key) {
                query = path.toFile().text
            } else {
                signature = path.toFile().text
            }
        }

        PathQueryFilePack.builder()
                .fileName(key)
                .query(query)
                .signature(signature)
                .build()
    }

    @Override
    int compareTo(final PathQueryFilePack other) {
        final int queryCompare = Objects.compare(this.query, other.query, Comparator.naturalOrder())

        if (queryCompare == 0) {
            return Objects.compare(this.signature, other.signature, Comparator.naturalOrder())
        }

        return queryCompare
    }
}
