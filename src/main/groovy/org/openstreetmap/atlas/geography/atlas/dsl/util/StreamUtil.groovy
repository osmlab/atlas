package org.openstreetmap.atlas.geography.atlas.dsl.util

import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Util to work with Java's stream API.
 *
 * @author Yazad Khambata
 */
final class StreamUtil {

    private StreamUtil() {}

    static <T> Stream<T> stream(final Iterable<T> iterable) {
        StreamSupport.stream(iterable.spliterator(), false)
    }

    static <T> Stream<T> stream(final Iterator<T> iterator) {
        final Iterable<T> iterable = { -> iterator }

        stream(iterable)
    }

    static <T> Stream<T> stream(Enumeration<T> enumeration) {
        /*
            inspired by https://stackoverflow.com/a/33243700/1165727
        */
        Valid.notEmpty enumeration

        StreamSupport.<T>stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            T next() {
                                enumeration.nextElement()
                            }

                            boolean hasNext() {
                                enumeration.hasMoreElements()
                            }
                        },
                        Spliterator.ORDERED), false)
    }

}
