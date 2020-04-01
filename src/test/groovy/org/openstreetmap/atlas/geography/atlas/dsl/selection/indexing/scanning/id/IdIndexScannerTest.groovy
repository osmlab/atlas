package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.id

import org.apache.commons.lang3.time.StopWatch
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.BaseTable
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.NodeTable
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.ItemType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import java.util.function.Predicate

/**
 * @author Yazad Khambata
 */
class IdIndexScannerTest extends AbstractAQLTest {
    private static final Logger log = LoggerFactory.getLogger(IdIndexScannerTest.class);

    @Test
    void testFetchSingle() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final NodeTable nodeTable = anguilaSchema.node

        assert IdIndexScanner.instance.fetch(nodeTable, 307459622000000).count { true } == 1
    }

    @Test
    void testFetchMultiple() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final NodeTable nodeTable = anguilaSchema.node

        assert IdIndexScanner.instance.fetch(nodeTable, 307459622000000, 307446836000000, 307351652000000).count {
            true
        } == 3
    }

    @Test
    void testFetchMultipleList() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final NodeTable nodeTable = anguilaSchema.node

        assert IdIndexScanner.instance.fetch(nodeTable, [307459622000000, 307446836000000, 307351652000000]).count {
            true
        } == 3
    }

    @Test
    void testFetchSingleEmpty() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final NodeTable nodeTable = anguilaSchema.node

        IdIndexScanner.instance.fetch(nodeTable, -1).forEach { assert false }
    }

    @Test
    void testFetchMultipleEmpty() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final NodeTable nodeTable = anguilaSchema.node

        IdIndexScanner.instance.fetch(nodeTable, -1, -2, -3).forEach { assert false }
    }

    @Test
    void testFetchMultipleListEmpty() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final NodeTable nodeTable = anguilaSchema.node

        IdIndexScanner.instance.fetch(nodeTable, [-1, -2, -3]).forEach { node -> log.info "${node}" }
    }

    @Test
    void testFetchMultipleAll() {
        final AtlasSchema anguilaSchema = usingAlcatraz()

        final Map<ItemType, List<Long>> idsByEntity = [
                (ItemType.NODE)    : [307459622000000, 307446836000000],
                (ItemType.POINT)   : [4553243887000000, 3054493437000000],
                (ItemType.EDGE)    : [27989500000000, 27996878000000],
                (ItemType.LINE)    : [99202295000000, 128245365000000],
                (ItemType.AREA)    : [24433389000000, 27999864000000],
                (ItemType.RELATION): [9451753000000] //only one available.
        ]

        (1..5).forEach {
            log.info("ROUND-${it}")
            Arrays.stream(TableSetting.values()).forEach { tableSetting ->
                final BaseTable atlasTable = (BaseTable) anguilaSchema[tableSetting.tableName()]

                final ItemType itemType = tableSetting.itemType
                final List<Long> ids = idsByEntity[itemType]

                assert ids.size() > 0

                final Predicate<AtlasEntity> predicate = atlasTable.hasIds(ids as Long[]).toPredicate(itemType.memberClass)

                final StopWatch stopWatchIdx = new StopWatch()
                stopWatchIdx.start()
                final Long countWithIndex = IdIndexScanner.instance.fetch(atlasTable, ids).count { true }
                stopWatchIdx.stop()

                final StopWatch stopWatchFull = new StopWatch()
                stopWatchFull.start()
                final Long countWithFullScan = tableSetting.getAll(anguilaSchema.atlasMediator.atlas, predicate).count {
                    true
                }
                stopWatchFull.stop()

                assert countWithIndex == ids.size()
                assert countWithIndex == countWithFullScan, "${itemType} count mismatched."

                log.info "Table: ${itemType} -> Records: ${countWithIndex}; ${stopWatchIdx.getTime(TimeUnit.MILLISECONDS)} millis (Index), ${stopWatchFull.getTime(TimeUnit.MILLISECONDS)} millis (Full)"
            }
        }
    }
}
