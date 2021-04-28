package org.openstreetmap.atlas.geography.atlas.dsl.util

import org.apache.commons.lang3.Validate
import org.junit.Test

/**
 * @author Yazad Khambata
 */
class GroovyEmptyTest {
    @Test
    void testEmpty1() {
        def var = null
        verifyEmpty(var)
    }

    @Test
    void testEmpty2() {
        def var = ""
        verifyEmpty(var)
    }

    @Test
    void testEmpty3() {
        def var = []
        verifyEmpty(var)
    }

    @Test
    void testEmpty4() {
        def var = [:]
        verifyEmpty(var)
    }

    private void verifyEmpty(var) {
        verifyEmptyWithAsserts(var)
        verifyEmptyWithValidate(var)
    }

    private void verifyEmptyWithValidate(var) {
        Validate.isTrue(!var)
    }

    private void verifyEmptyWithAsserts(var) {
        if (var) {
            assert false
        }

        assert true
    }
}
