package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getRelation

/**
 * @author Yazad Khambata
 */
class AbstractQueryOptimizationTransformTest extends BaseOptimizationTest {
    @Test
    void testAppliesNoParams() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation

        assertNotApplicable(select1)
    }

    @Test
    void testApplies1Param() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1)

        assertNotApplicable(select1)
    }

    @Test
    void testApplies2ParamOr() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) or relation.hasId(2)

        assertApplicable(select1)
    }

    @Test
    void testApplies2ParamFullScanAnds() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasLastUserNameLike(/a/) and relation.hasTag(a: "b")

        assertNotApplicable(select1)
    }

    @Test
    void testApplies2ParamNonFullScanAnds() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.isWithin([
                [
                        [-63.171752227, 18.1418553393], [-62.9506523734, 18.1418553393], [-62.9506523734, 18.2749168824], [-63.171752227, 18.2749168824], [-63.171752227, 18.1418553393]
                ]
        ]) and relation.isWithin([
                [
                        [-63.171752227, 18.1418553393], [-62.9506523734, 18.1418553393], [-62.9506523734, 18.2749168824], [-63.171752227, 18.2749168824], [-63.171752227, 18.1418553393]
                ]
        ])

        assertApplicable(select1)
    }

    @Test
    void testApplies3ParamCase1() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) and relation.isWithin([[[1.567, 2.123], [2.234, 3.567], [3.678, 1.124]]]) and relation.hasLastUserNameLike(/a/)

        assertApplicable(select1)
    }

    @Test
    void testApplies3ParamCase2() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.isWithin([[[1, 2], [2, 3], [3, 1]]]) and relation.hasId(1) and relation.hasLastUserNameLike(/a/)

        assertApplicable(select1)
    }

    @Test
    void testApplies3ParamCase3() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasLastUserNameLike(/a/) and relation.isWithin([[[1, 2], [2, 3], [3, 1]]]) and relation.hasId(1)

        assertApplicable(select1)
    }

    protected boolean isApplicable(QueryBuilder select1) {
        isApplicableAtBaseLevel(select1)
    }

    @Override
    QueryOptimizationTransformer associatedOptimization() {
        throw new UnsupportedOperationException()
    }
}
