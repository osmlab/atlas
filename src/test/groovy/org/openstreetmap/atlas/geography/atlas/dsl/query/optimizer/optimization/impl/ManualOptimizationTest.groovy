package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.apache.commons.math3.util.CombinatoricsUtils
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.items.Area
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.Relation

import java.util.stream.Collectors
import java.util.stream.StreamSupport

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.exec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getArea

/**
 * @author Yazad Khambata
 */
class ManualOptimizationTest extends AbstractAQLTest {

    def polygonOfNorthernPart = [
            [
                    [-122.4237920517,37.8265958156],[-122.4251081994,37.8270138686],[-122.425258081,37.8266032148],[-122.4256749392,37.8271988471],[-122.4259465995,37.8276908875],[-122.4265742287,37.8279942492],[-122.4259231805,37.8283198066],[-122.4240871311,37.8285232793],[-122.4229396001,37.8282865111],[-122.4226070504,37.8278943619],[-122.422499323,37.8276057981],[-122.4232534147,37.8270471648],[-122.4237920517,37.8265958156]
            ]
    ]

    final long id1 = 28000041000000
    final long id2 = 24433395000000
    final long id3 = 660870452000000
    final long id4 = 396629347000000
    final long id5 = 27996732000000

    final Long[] ids = [id1, id2, id3, id4, id5] as Long[]

    final Long[] idsGroup1 = [id1, id2, id3] as Long[]
    final Long[] idsGroup2 = [id4, id5] as Long[]

    @Test
    void testReorderingOptimization() {
        def atlasSchema = usingAlcatraz()

        final Constraint<Relation> constraint1 = area.hasIds(ids)
        final Constraint<Relation> constraint2 = area.isWithin(polygonOfNorthernPart)
        final Constraint<Relation> constraint3 = area.hasLastUserNameLike(/a/)

        final List<Constraint<Relation>> preferredOrder = [constraint1, constraint2, constraint3]

        final Iterable<List<Constraint<Relation>>> iterable = {
            new PermutationGenerator<>(preferredOrder)
        }

        final int sum = StreamSupport.stream(iterable.spliterator(), false)
                .mapToInt() { permutation ->
                    final QueryBuilder qb1 = select(area._).from(atlasSchema.area).where(permutation[0]).and(permutation[1]).and(permutation[2])
                    final Result<Relation> result1 = exec qb1
                    assert result1.relevantIdentifiers as Set == expectedIdsInResult()

                    final List<Constraint<Relation>> constraintsInPreferredOrder = permutation.stream()
                            .sorted(Comparator.comparing { Constraint<Relation> constraint -> constraint.bestCandidateScanType.preferntialRank })
                            .collect(Collectors.toList())

                    final QueryBuilder qb2 = select(area._).from(atlasSchema.area).where(constraintsInPreferredOrder[0]).and(constraintsInPreferredOrder[1]).and(constraintsInPreferredOrder[2])
                    final Result<Relation> result2 = exec qb2
                    assert result2.relevantIdentifiers as Set == expectedIdsInResult()

                    1
                }
                .sum()

        assert sum == CombinatoricsUtils.factorial(preferredOrder.size())
    }

    private Set<Integer> expectedIdsInResult() {
        [id1, id2, id3, id4, id5] as Set
    }

    @Test
    void testInOptimization1() {
        def atlasSchema = usingAlcatraz()

        final List<Constraint<Relation>> constraints = Arrays.stream(ids)
                .map { area.hasId(it) }
                .collect(Collectors.toList())

        assert constraints.size() == 5

        final QueryBuilder qb1 = select(area._).from(atlasSchema.area)
                .where(constraints[0])
                .or(constraints[1])
                .or(constraints[2])
                .or(constraints[3])
                .or(constraints[4])

        final QueryBuilder qb2 = select(area._).from(atlasSchema.area)
            .where(area.hasIds(ids))

        assertExecEquals(qb1, qb2)


        final Query query3 = qb1.buildQuery().shallowCopyWithConditionalConstructList(inIdsConditionalConstructList())

        assert qb2.buildQuery() == query3
    }

    private ConditionalConstructList<AtlasEntity> inIdsConditionalConstructList() {
        new ConditionalConstructList<>([ConditionalConstruct.builder().clause(Statement.Clause.WHERE).constraint(area.hasIds(ids)).build()])
    }

    @Test
    void testInOptimization2() {
        def atlasSchema = usingAlcatraz()

        final QueryBuilder qb1 = select(area._).from(atlasSchema.area)
                .where(area.hasIds(idsGroup1))
                .or(area.hasIds(idsGroup2))

        final QueryBuilder qb2 = select(area._).from(atlasSchema.area)
                .where(area.hasIds(ids))

        assertExecEquals(qb1, qb2)

        final Query query3 = qb1.buildQuery().shallowCopyWithConditionalConstructList(inIdsConditionalConstructList())

        assert qb2.buildQuery() == query3
    }


    @Test
    void testInOptimization3() {
        def atlasSchema = usingAlcatraz()

        final QueryBuilder inner1 = select area._ from atlasSchema.area where area.hasIds(idsGroup1)
        final QueryBuilder inner2 = select area._ from atlasSchema.area where area.hasIds(idsGroup2)
        final QueryBuilder qb1 = select(area._).from(atlasSchema.area)
                .where(area.hasIds(inner1)).or(area.hasIds(inner2))

        final QueryBuilder qb2 = select(area._).from(atlasSchema.area)
                .where(area.hasIds(ids))

        assertExecEquals(qb1, qb2)

        final Long[] idsExtracted = qb2.buildQuery().conditionalConstructList.get(0).constraint.valueToCheck

        assert Arrays.stream(ids).sorted().collect(Collectors.toList()) == Arrays.stream(idsExtracted).sorted().collect(Collectors.toList())
    }

    private void assertExecEquals(QueryBuilder qb1, QueryBuilder qb2) {
        assert !qb1.is(qb2)

        final Result<Area> result1 = exec qb1
        final Result<Area> result2 = exec qb2

        assert toIds(result1) == toIds(result2)
    }

    private List<Long> toIds(Result result) {
        result.relevantIdentifiers.stream().sorted().collect(Collectors.toList())
    }
}
