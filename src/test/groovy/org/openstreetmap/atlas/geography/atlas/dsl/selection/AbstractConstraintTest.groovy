package org.openstreetmap.atlas.geography.atlas.dsl.selection

import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint

/**
 * @author Yazad Khambata
 */
class AbstractConstraintTest {
    protected void assertEquals(Constraint constraint1, Constraint constraint2) {
        assert constraint1 == constraint2
        assert constraint1.hashCode() == constraint2.hashCode()
    }
}
