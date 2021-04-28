package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.apache.commons.math3.util.CombinatoricsUtils
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.impl.QueryAnalyzerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BinaryOperations
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.Relation

import java.util.stream.Collectors

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.exec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.not
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getRelation

/**
 * @author Yazad Khambata
 */
class IdsInOptimizationTest extends BaseOptimizationTest {

    @Test
    void testEligibility1() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) or relation.hasId(2)

        assertApplicable(select1)
    }

    @Test
    void testEligibility2() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasIds(1, 3) or relation.hasId(2)

        assert isApplicableAtBaseLevel(select1)
        assertApplicable(select1)
    }

    @Test
    void testEligibility3() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasIds(1, 3) or relation.hasLastUserName("whatever")

        OptimizationTestHelper.instance.abstractQueryOptimizationTransform().isApplicable(QueryAnalyzerImpl.from(ExplainerImpl.instance.explain(select1)))
        assertNotApplicable(select1)
    }

    @Test
    void testEligibility4() {
        def atlas = usingAlcatraz()
        def select1 = select relation._ from atlas.relation where relation.hasId(1) or not(relation.hasId(2))

        assertNotApplicable(select1)
    }

    @Test
    void testEligibility() {
        def atlas = usingAlcatraz()

        final QueryBuilder inner1 = select relation.id from atlas.relation where relation.hasIds(5369365000000, 5369366000000, 5369367000000)
        final QueryBuilder inner2 = select relation.id from atlas.relation where relation.hasIds(7551669000000, 7553474000000, 7625293000000)

        def constraint1 = relation.hasId(2698310000000)
        def constraint2 = relation.hasId(2714283000000)
        def constraint3 = relation.hasIds(3085693000000, 3087373000000)
        def constraint4 = relation.hasIds(6160034000000, 338580000000)
        def constraint5 = relation.hasIds(inner1)
        def constraint6 = relation.hasIds(inner2)

        final List<Constraint<Relation>> constraints = [
                constraint1, constraint2, constraint3, constraint4, constraint5, constraint6
        ]

        //Order unimportant here
        final Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(6, 2)

        while (iterator.hasNext()) {
            final int[] indexes = iterator.next()

            assert indexes.length == 2

            final QueryBuilder select1 = select(relation.id).from(atlas.relation).where(constraints[indexes[0]]).or(constraints[indexes[1]])

            assertApplicable(select1)



            final Query<Relation> optimizedQuery = IdsInOptimization.instance.applyTransformation(QueryAnalyzerImpl.from(ExplainerImpl.instance.explain(select1)))

            assert optimizedQuery.conditionalConstructList.size() == 1
            assert optimizedQuery.conditionalConstructList.get(0).constraint.field == relation.id
            assert optimizedQuery.conditionalConstructList.get(0).constraint.bestCandidateScanType == ScanType.ID_UNIQUE_INDEX
            assert optimizedQuery.conditionalConstructList.get(0).constraint.operation == BinaryOperations.inside

            final Result<Relation> result1 = exec select1
            final Result<Relation> result2 = optimizedQuery.execute()

            assert result1.relevantIdentifiers.stream().sorted().collect(Collectors.toList()) == result2.relevantIdentifiers.stream().sorted().collect(Collectors.toList())
        }
    }

    @Override
    QueryOptimizationTransformer associatedOptimization() {
        IdsInOptimization.instance
    }
}
