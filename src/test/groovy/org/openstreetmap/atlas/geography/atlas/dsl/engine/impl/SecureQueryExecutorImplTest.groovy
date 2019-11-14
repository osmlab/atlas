package org.openstreetmap.atlas.geography.atlas.dsl.engine.impl

import org.junit.BeforeClass
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.engine.QueryExecutor
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.SchemeSupport

/**
 * @author Yazad Khambata
 */
class SecureQueryExecutorImplTest extends AbstractAQLTest {
    private static final String QUERY = """select node.id, node.osmId, node.tags from atlas.node limit 10"""
    private static final String QUERY_SIGNATURE = "mCw1ob4SX8a4vMrMvEqI7iGovs/aPKq3hbk/rZ2vUFNasF7pjEJjwFOp496xAwEp32bgiIuW5YxJASVDqQ5oEw=="

    private static QueryExecutor queryExecutor

    @BeforeClass
    static void setup() {
        AbstractAQLTest.setup()
        System.setProperty(AbstractQueryExecutorImpl.SYSTEM_PARAM_KEY, "DUMMY_SECRET")
        queryExecutor = new SecureQueryExecutorImpl()
    }

    @Test
    void testExecString() {
        queryExecutor.exec(usingButterflyPark().atlasMediator.atlas, QUERY, QUERY_SIGNATURE)
    }

    @Test
    void testExecStringBadSignature() {
        try {
            queryExecutor.exec(usingButterflyPark().atlasMediator.atlas, QUERY, "bad signature")
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Signature Mismatch")
            return
        }

        assert false
    }

    @Test
    void testExecFile() {
        def file = new File("/tmp/${UUID.randomUUID()}.aql")

        file << QUERY

        new FileReader(file).withCloseable { fileReader ->
            queryExecutor.exec(usingButterflyPark().atlasMediator.atlas, fileReader, QUERY_SIGNATURE)
        }
    }
}
