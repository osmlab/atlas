package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode

/**
 * @author Yazad Khambata
 */
class GeometricSurfacesOverlapOptimizationTest extends BaseOptimizationTest {

    @Test
    void testIsApplicable1() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz])

        assertApplicable(select1)
    }

    @Test
    void testIsApplicable2() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz]) and node.isWithin([TestConstants.Polygons.southernPartOfAlcatraz])

        assertApplicable(select1)
    }

    @Test
    void testIsApplicable3() {
        def atlas = usingAlcatraz()

        final QueryBuilder select1 = select node.id from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.centralPartOfAlcatraz]) or node.isWithin([TestConstants.Polygons.southernPartOfAlcatraz])

        assertApplicable(select1)
    }

    @Test
    void testIsApplicable4() {
        def atlas = usingButterflyPark()

        final QueryBuilder select1 = select node.id from atlas.node where node.isWithin([TestConstants.Polygons.northernPartOfAlcatraz, TestConstants.Polygons.southernPartOfAlcatraz])

        assertApplicable(select1)
    }

    @Override
    QueryOptimizationTransformer associatedOptimization() {
        GeometricSurfacesOverlapOptimization.instance
    }
}
