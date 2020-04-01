package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getExec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.edge

/**
 * @author Yazad Khambata
 */
class TagConditionTest extends AbstractAQLTest {
    @Test
    void testSelectStar() {
        def atlasSchema = usingAlcatraz()

        def selectFootway = select edge.id, edge.tags from atlasSchema.edge where edge.hasTag(highway: "footway")
        def selectSteps = select edge.id, edge.tags from atlasSchema.edge where edge.hasTag(highway: "steps")
        def selectOr = select edge.id, edge.tags from atlasSchema.edge where edge.hasTag(highway: "footway") or edge.hasTag(highway: "steps")
        def selectIn = select edge.id, edge.tags from atlasSchema.edge where edge.hasTag(highway: ["footway", "steps"])

        final Result result1 = exec selectFootway
        final Result result2 = exec selectSteps
        final Result result3 = exec selectOr
        final Result result4 = exec selectIn

        assert result1.relevantIdentifiers.size() >= 1
        assert result2.relevantIdentifiers.size() >= 1
        assert result3.relevantIdentifiers.size() >= 2

        assert (result1.relevantIdentifiers + result2.relevantIdentifiers).sort() == result3.relevantIdentifiers.sort()
        assert result3.relevantIdentifiers.sort() == result4.relevantIdentifiers.sort()
    }
}
