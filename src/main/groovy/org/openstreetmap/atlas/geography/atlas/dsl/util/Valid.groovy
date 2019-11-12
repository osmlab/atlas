package org.openstreetmap.atlas.geography.atlas.dsl.util

import org.apache.commons.lang3.Validate

/**
 * A validation util.
 *
 * @author Yazad Khambata
 */
class Valid {
    static void notEmpty(object) {
        if (!object) {
            throwException("", null)
        }
    }

    static void notEmpty(object, final String message) {
        try {
            notEmpty(object)
        } catch (e) {
            throwException(message, e)
        }
    }

    static void isTrue(boolean expr) {
        Validate.isTrue(expr)
    }

    static void isTrue(boolean expr, final String message) {
        try {
            isTrue(expr)
        } catch (e) {
            throwException(message, e)
        }
    }

    private static void throwException(String message, Exception e) {
        throw new IllegalStateException(message, e)
    }
}
