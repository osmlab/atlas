package org.openstreetmap.atlas.geography.atlas.dsl.sanity

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getExec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getArea
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getEdge
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getLine
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getPoint
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getRelation

/**
 * @author Yazad Khambata
 */
class DataSanity extends AbstractAQLTest {
    private static final Logger log = LoggerFactory.getLogger(DataSanity.class);

    @Test
    void testSelect1() {
        def atlas = usingAlcatraz()

        def select1

        select1 = select node._ from atlas.node limit 5
        exec select1

        select1 = select point._ from atlas.point limit 5
        exec select1

        select1 = select edge._ from atlas.edge limit 5
        exec select1

        select1 = select line._ from atlas.line limit 5
        exec select1

        select1 = select relation._ from atlas.relation limit 5
        exec select1

        select1 = select area._ from atlas.area limit 5
        exec select1
    }

    @Test
    void testSelect2() {
        def atlas = usingButterflyPark()

        def select1

        select1 = select node.tags from atlas.node limit 5
        exec select1

        select1 = select point._ from atlas.point limit 5
        exec select1

        select1 = select edge._ from atlas.edge limit 5
        exec select1

        select1 = select line._ from atlas.line limit 5
        exec select1

        select1 = select relation._ from atlas.relation limit 5
        exec select1

        select1 = select area._ from atlas.area limit 5
        exec select1
    }

    @Test
    void testWithin() {
        final AtlasSchema atlas = usingAlcatraz()

        def select1

        def polygonsNorth = [TestConstants.Polygons.northernPartOfAlcatraz]
        def polygonsSouth = [TestConstants.Polygons.southernPartOfAlcatraz]

        [node: node, point: point, edge: edge, line: line, relation: relation, area: area].entrySet().stream().forEach { entry ->
            def tableName = entry.getKey()
            log.info("${tableName}")

            def table = entry.getValue()

            def list = [north: polygonsNorth, south: polygonsSouth].entrySet().stream().map { polygonEntry ->
                def direction = polygonEntry.getKey()
                log.info("${tableName}-${direction}")

                def polygon = polygonEntry.getValue()

                select1 = select table._ from atlas[tableName] where table.isWithin(polygon)
                final Result<AtlasEntity> result = exec select1
                result.relevantIdentifiers
            }.collect(Collectors.toList())

            assert list.size() == 2

            final List<Long> northIds = list.get(0)
            final List<Long> southIds = list.get(1)

            log.info("${tableName} northIds:: ${northIds}.")
            log.info("${tableName} southIds:: ${southIds}.")

            assert !(northIds.size() == 0 ^ southIds.size() == 0)
            assert (northIds as Set).intersect(southIds as Set).size() == 0

            log.info("---")
        }
    }
}
