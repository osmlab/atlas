package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.impl.QueryAnalyzerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.exec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode

/**
 * @author Yazad Khambata
 */
class GeometricSurfacesWithinOptimizationTest extends BaseOptimizationTest {
    @Test
    void testEligible1() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.centralPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.southernPartOfAlcatraz])

        assertApplicable(select1)
    }

    @Test
    void testEligible2() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.centralPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.southernPartOfAlcatraz]) or node.hasIds(1,2,3)

        OptimizationTestHelper.instance.abstractQueryOptimizationTransform().isApplicable(QueryAnalyzerImpl.from(ExplainerImpl.instance.explain(select1.buildQuery())))
        assertNotApplicable(select1)
    }

    @Test
    void testEligible3() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz]) and node.isWithin([TestConstants.Polygons.southernPartOfAlcatraz])

        OptimizationTestHelper.instance.abstractQueryOptimizationTransform().isApplicable(QueryAnalyzerImpl.from(ExplainerImpl.instance.explain(select1.buildQuery())))
        assertNotApplicable(select1)
    }

    @Test
    void test() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.centralPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.southernPartOfAlcatraz])

        assertApplicable(select1)

        final Query<Node> transformedQuery = GeometricSurfacesWithinOptimization.instance.applyTransformation(QueryAnalyzerImpl.from(ExplainerImpl.instance.explain(select1)))

        def result1 = exec select1
        def result2 = transformedQuery.execute()

        assert result1.relevantIdentifiers.sort() == result2.relevantIdentifiers.sort()
    }

    @Override
    QueryOptimizationTransformer associatedOptimization() {
        GeometricSurfacesWithinOptimization.instance
    }
}
