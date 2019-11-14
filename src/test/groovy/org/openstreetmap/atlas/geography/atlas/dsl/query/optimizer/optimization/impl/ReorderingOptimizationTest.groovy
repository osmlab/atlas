package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.apache.commons.math3.util.CombinatoricsUtils
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.impl.QueryAnalyzerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.items.Relation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.StreamSupport

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getRelation
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.node

/**
 * @author Yazad Khambata
 */
class ReorderingOptimizationTest extends BaseOptimizationTest {
    private static final Logger log = LoggerFactory.getLogger(ReorderingOptimizationTest.class)
    @Test
    void testAppliesForOrCase1() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) or relation.hasId(2)

        assertNotApplicableForReorderingOptimizationOnly(select1)
    }

    @Test
    void testAppliesForOrCase2() {
        def atlas = usingAlcatraz()
        
        def select1 = select relation._ from atlas.relation where relation.hasId(1) or relation.hasId(2) and relation.hasTag(aaa: "bbb")

        assertNotApplicableForReorderingOptimizationOnly(select1)
    }

    @Test
    void testAppliesForCase3() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasTag(aaa1: "bbb") and relation.hasTag(aaa: "bbb")

        assertNotApplicableForBoth(select1)
    }

    @Test
    void testAppliesCase4() {
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

        assertNotApplicableForReorderingOptimizationOnly(select1)
    }

    @Test
    void testAppliesCase5() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) and relation.isWithin([
                [
                        [-63.171752227, 18.1418553393], [-62.9506523734, 18.1418553393], [-62.9506523734, 18.2749168824], [-63.171752227, 18.2749168824], [-63.171752227, 18.1418553393]
                ]
        ])

        assertNotApplicableForReorderingOptimizationOnly(select1)
    }

    @Test
    void testAppliesCase6() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.isWithin([
                [
                        [-63.171752227, 18.1418553393], [-62.9506523734, 18.1418553393], [-62.9506523734, 18.2749168824], [-63.171752227, 18.2749168824], [-63.171752227, 18.1418553393]
                ]
        ]) and relation.hasId(1)

        assertApplicable(select1)
    }

    @Test
    void testAppliesCase7() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.isWithin([
                [
                        [-63.171752227, 18.1418553393], [-62.9506523734, 18.1418553393], [-62.9506523734, 18.2749168824], [-63.171752227, 18.2749168824], [-63.171752227, 18.1418553393]
                ]
        ]) and relation.hasId(1) and relation.hasTag(aaa1: "bbb")

        assertApplicable(select1)
    }

    @Test
    void testAppliesCase8() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) or relation.hasId(2) and relation.hasTag(aaa: "bbb")

        assertNotApplicableForReorderingOptimizationOnly(select1)
    }

    @Test
    void testAppliesCase9() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) and not(relation.hasId(1))

        assertNotApplicableForBoth(select1)
    }

    @Test
    void testAppliesCase10() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where not(relation.hasId(1)) and relation.hasId(1)

        assertApplicable(select1)
    }

    @Test
    void testOptimization() {
        def atlas = usingAlcatraz()

        def polygon = [
                TestConstants.Polygons.northernPartOfAlcatraz
        ]

        def ids = [1641119524000000, 307457176000000, 3202364308000000, 307446836000000, 1417681468000000] as Long[]

        final Constraint<Node> constraint1 = node.hasIds(ids)
        final Constraint<Node> constraint2 = node.isWithin(polygon)
        final Constraint<Node> constraint3 = node.hasLastUserNameLike(/.+/) //regex for at least one character

        final List<Constraint<Node>> preferredOrder = [constraint1, constraint2, constraint3]

        //Order is important here
        final Iterable<List<Constraint<Node>>> iterable = {
            new PermutationGenerator<>(preferredOrder)
        }

        final int sum = StreamSupport.stream(iterable.spliterator(), false)
                .filter { permutation ->
                    def passed = permutation != preferredOrder && permutation.get(0) != constraint1

                    passed
                }
                .mapToInt { permutation ->
                    log.info("constraint1:: $constraint1")
                    log.info("constraint2:: $constraint2")
                    log.info("constraint3:: $constraint3")

                    final QueryBuilder optimalQueryBuilder = select(node._).from(atlas.node).where(constraint1).and(constraint2).and(constraint3)
                    final Result<Node> result1 = exec optimalQueryBuilder
                    assert result1.relevantIdentifiers as Set == ids as Set

                    final QueryBuilder potentiallySubOptimalQueryBuilder = select(node._).from(atlas.node).where(permutation[0]).and(permutation[1]).and(permutation[2])
                    final Result<Relation> result2 = exec potentiallySubOptimalQueryBuilder
                    assert result2.relevantIdentifiers as Set == ids as Set

                    def optimalQuery = optimalQueryBuilder.buildQuery()
                    def potentiallySubOptimalQuery = potentiallySubOptimalQueryBuilder.buildQuery()

                    assertApplicable(potentiallySubOptimalQueryBuilder)

                    final OptimizationRequest optimizationRequest = QueryAnalyzerImpl.from(ExplainerImpl.instance.explain(potentiallySubOptimalQuery))

                    def queryOptimizedWithReordering = ReorderingOptimization.instance.applyTransformation(optimizationRequest)
                    assert optimalQuery.conditionalConstructList == queryOptimizedWithReordering.conditionalConstructList
                    assert optimalQuery == queryOptimizedWithReordering

                    final Result<Relation> result3 = queryOptimizedWithReordering.execute()
                    assert result3.relevantIdentifiers as Set == ids as Set

                    1
                }
                .sum()

        assert sum == CombinatoricsUtils.factorial(preferredOrder.size()) - CombinatoricsUtils.factorial(2)
    }

    protected void assertNotApplicableForReorderingOptimizationOnly(select1) {
        assert isApplicableAtBaseLevel(select1)

        assert isApplicableAtBaseLevel(select1)
    }

    protected void assertNotApplicableForBoth(select1) {
        super.assertNotApplicable(select1)

        assert !isApplicableAtBaseLevel(select1)
    }

    @Override
    QueryOptimizationTransformer associatedOptimization() {
        ReorderingOptimization.instance
    }
}
