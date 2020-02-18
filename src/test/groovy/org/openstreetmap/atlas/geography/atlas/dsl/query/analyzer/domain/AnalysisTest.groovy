package org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.BaseConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.GeometricSurfacesOverlapOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.GeometricSurfacesWithinOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.IdsInOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.ReorderingOptimization

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getEdge

/**
 * @author Yazad Khambata
 */
class AnalysisTest extends AbstractAQLTest {

    @Test
    void toPrettyStringSelect1() {
        def atlas = usingButterflyPark()

        final QueryBuilder select1 = select edge._ from atlas.edge where edge.hasId(478164185000001) or edge.hasId(528897519000001)
        verifyRun(select1, IdsInOptimization.class)
    }

    @Test
    void toPrettyStringSelect2() {
        def atlas = usingButterflyPark()

        final QueryBuilder select1 = select edge._ from atlas.edge where edge.hasTag(/surface/) and edge.hasId(478164185000001)
        verifyRun(select1, ReorderingOptimization.class)
    }

    @Test
    void toPrettyStringSelect3() {
        def atlas = usingAlcatraz()

        def multiPolygon = [
                TestConstants.Polygons.northernPartOfAlcatraz,
                TestConstants.Polygons.southernPartOfAlcatraz,
                TestConstants.Polygons.provincetownMassachusetts
        ]

        final QueryBuilder select1 = select edge._ from atlas.edge where edge.isWithin(multiPolygon)
        verifyRun(select1, GeometricSurfacesOverlapOptimization.class)
    }

    @Test
    void toPrettyStringSelect4() {
        def atlas = usingAlcatraz()

        def multiPolygon1 = [
                TestConstants.Polygons.northernPartOfAlcatraz
        ]

        def multiPolygon2 = [
                TestConstants.Polygons.southernPartOfAlcatraz,
                TestConstants.Polygons.provincetownMassachusetts
        ]

        final QueryBuilder select1 = select edge._ from atlas.edge where edge.isWithin(multiPolygon1) or edge.isWithin(multiPolygon2)
        verifyRun(select1, GeometricSurfacesOverlapOptimization.class, GeometricSurfacesWithinOptimization.class)
    }

    @Test
    void toPrettyStringUpdate1() {
        def atlas = usingButterflyPark()

        final QueryBuilder update = update atlas.edge set edge.addTag("a": "b") where edge.hasId(478164185000001) or edge.hasId(528897519000001)
        verifyRun(update, IdsInOptimization.class)
    }

    @Test
    void toPrettyStringDelete1() {
        def atlas = usingButterflyPark()

        final QueryBuilder delete1 = delete atlas.edge where edge.hasId(478164185000001) or edge.hasId(528897519000001)
        verifyRun(delete1, IdsInOptimization.class)
    }

    private <E> void verifyRun(QueryBuilder select1, final Class<QueryOptimizationTransformer<E>>... queryOptimizationTransformerClasses) {
        final Analysis analysis = run(select1)
        verify(analysis, queryOptimizationTransformerClasses)
    }

    private <E> Analysis<E> run(final QueryBuilder queryBuilder) {
        def result = exec queryBuilder
        assert result.relevantIdentifiers.size() >= 1
        final Analysis analysis = explain queryBuilder
        analysis
    }

    private <E> void verify(final Analysis<E> analysis, final Class<QueryOptimizationTransformer<E>>... queryOptimizationTransformerClasses) {
        assert queryOptimizationTransformerClasses != null
        assert queryOptimizationTransformerClasses.size() >= 1
        def string = analysis.toPrettyString()
        assert string.contains("[I] Was the query optimized?                             : true")
        for (final Class<QueryOptimizationTransformer<E>> queryOptimizationTransformerClass : queryOptimizationTransformerClasses) {
            def optimizerName = queryOptimizationTransformerClass.getSimpleName()
            assert string.contains(optimizerName)
        }
    }
}
