package org.openstreetmap.atlas.geography.atlas.dsl.engine.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest

/**
 * @author Yazad Khambata
 */
class InsecureQueryExecutorImplTest extends AbstractAQLTest {
    private static final String QUERY = """select node.id, node.osmId, node.tags from atlas.node limit 10"""

    @Test
    void testExecString() {
        new InsecureQueryExecutorImpl().exec(usingButterflyPark().atlasMediator.atlas, QUERY, null)
    }

    @Test
    void testExecFile() {
        def file = new File("/tmp/${UUID.randomUUID()}.aql")

        file << QUERY

        new FileReader(file).withCloseable { fileReader ->
            new InsecureQueryExecutorImpl().exec(usingButterflyPark().atlasMediator.atlas, fileReader, null)
        }
    }
}
