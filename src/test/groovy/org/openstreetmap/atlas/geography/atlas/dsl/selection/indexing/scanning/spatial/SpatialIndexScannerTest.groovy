package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.spatial

import org.apache.commons.lang3.time.StopWatch
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon.GeometricSurfaceSupport
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit

/**
 * @author Yazad Khambata
 */
class SpatialIndexScannerTest extends AbstractAQLTest {
    private static final Logger log = LoggerFactory.getLogger(SpatialIndexScannerTest.class);

    def polygon = [
            TestConstants.Polygons.centralPartOfAlcatraz
    ]

    @Test
    void testFetch1() {
        final AtlasSchema atlas = usingAlcatraz()

        assert SpatialIndexScanner.instance.fetch(atlas.node, polygon).count { true } == 48
    }

    @Test
    void testFetch2() {
        final AtlasSchema atlas = usingAlcatraz()

        assert SpatialIndexScanner.instance.fetch(atlas.edge, polygon).count { true } == 102
    }

    @Test
    void testFetchEmpty() {
        final AtlasSchema atlas = usingButterflyPark() //no overlap

        SpatialIndexScanner.instance.fetch(atlas.node, polygon).forEach { assert false }
    }

    @Test
    void testFetchAllTables() {
        final AtlasSchema atlasSchema = usingButterflyPark()

        (1..5).forEach { i ->
            log.info "ROUND-${i}"

            Arrays.stream(TableSetting.values()).forEach { tableSetting ->
                final StopWatch stopWatch1 = new StopWatch()

                stopWatch1.start()
                final Long countFetchedFromIndex = SpatialIndexScanner.instance.fetch(atlasSchema[tableSetting.tableName()], polygon).count {
                    true
                }
                stopWatch1.stop()

                final StopWatch stopWatch2 = new StopWatch()
                stopWatch2.start()
                final Long countFromFullScan = tableSetting.getAll(atlasSchema.atlasMediator.atlas, { entity -> entity.within(GeometricSurfaceSupport.instance.toGeometricSurface(polygon)) }).count {
                    true
                }
                stopWatch2.stop()

                assert countFetchedFromIndex == countFromFullScan, "${tableSetting} counts mismatched."

                log.info "Table: ${tableSetting} -> Fetched Records: ${countFetchedFromIndex}; in ${stopWatch1.getTime(TimeUnit.MILLISECONDS)} millis (optionalIndexInfo); ${stopWatch2.getTime(TimeUnit.MILLISECONDS)} millis (Full Scan)."
            }
        }
    }
}
