package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.selection.AbstractConstraintTest
import org.openstreetmap.atlas.geography.atlas.items.Node

import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode

/**
 * @author Yazad Khambata
 */
class NotConstraintTest extends AbstractConstraintTest {
    @Test
    void testEqualsAndHashCode() {
        final Constraint<Node> constraint1 = BasicConstraint.builder()
                .field(node.identifier)
                .operation(BinaryOperations.eq)
                .valueToCheck(1L)
                .bestCandidateScanType(ScanType.ID_UNIQUE_INDEX)
                .atlasEntityClass(Node.class)
                .build()

        final Constraint<Node> constraint2 = BasicConstraint.builder()
                .field(node.identifier)
                .operation(BinaryOperations.eq)
                .valueToCheck(1L)
                .bestCandidateScanType(ScanType.ID_UNIQUE_INDEX)
                .atlasEntityClass(Node.class)
                .build()

        assertEquals(constraint1, constraint2)


        final Constraint<Node> notConstraint1 = new NotConstraint(constraint1)
        final Constraint<Node> notConstraint2 = new NotConstraint(constraint2)

        assertEquals(notConstraint1, notConstraint2)
        def copy1 = notConstraint1.deepCopy()
        def copy2 = notConstraint2.deepCopy()
        assertEquals(copy1, copy2)
        assertEquals(notConstraint1, copy2)

        assert notConstraint1 != constraint1
        assert notConstraint2 != constraint2
    }
}
