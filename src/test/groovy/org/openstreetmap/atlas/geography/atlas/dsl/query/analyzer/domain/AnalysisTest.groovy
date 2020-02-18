package org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.GeometricSurfacesOverlapOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.GeometricSurfacesWithinOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.IdsInOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.ReorderingOptimization

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class AnalysisTest extends AbstractAQLTest {

    @Test
    void toPrettyString1() {
        def atlas = usingButterflyPark()

        def select1 = select edge._ from atlas.edge where edge.hasId(478164185000001) or edge.hasId(528897519000001)
        def analysis = run(select1)
        verify(analysis, IdsInOptimization.class)
    }

    @Test
    void toPrettyString2() {
        def atlas = usingButterflyPark()

        def select1 = select edge._ from atlas.edge where edge.hasTag(/surface/) and edge.hasId(478164185000001)
        def analysis = run(select1)
        verify(analysis, ReorderingOptimization.class)
    }

    @Test
    void toPrettyString3() {
        def atlas = usingAlcatraz()

        def multiPolygon = [
                TestConstants.Polygons.northernPartOfAlcatraz,
                TestConstants.Polygons.southernPartOfAlcatraz,
                TestConstants.Polygons.provincetownMassachusetts
        ]

        def select1 = select edge._ from atlas.edge where edge.isWithin(multiPolygon)
        def analysis = run(select1)
        verify(analysis, GeometricSurfacesOverlapOptimization.class)
    }

    @Test
    void toPrettyString4() {
        def atlas = usingAlcatraz()

        def multiPolygon1 = [
                TestConstants.Polygons.northernPartOfAlcatraz
        ]

        def multiPolygon2 = [
                TestConstants.Polygons.southernPartOfAlcatraz,
                TestConstants.Polygons.provincetownMassachusetts
        ]

        def select1 = select edge._ from atlas.edge where edge.isWithin(multiPolygon1) or edge.isWithin(multiPolygon2)
        def analysis = run(select1)
        verify(analysis, GeometricSurfacesOverlapOptimization.class, GeometricSurfacesWithinOptimization.class)
    }


    private <E> Analysis<E> run(select1) {
        def result = exec select1
        assert result.relevantIdentifiers.size() >= 1
        def analysis = explain select1
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
