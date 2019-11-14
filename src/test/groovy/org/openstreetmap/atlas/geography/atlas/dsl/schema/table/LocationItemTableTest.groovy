package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.items.LocationItem

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getExec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect

/**
 * @author Yazad Khambata
 */
class LocationItemTableTest extends AbstractAQLTest {

    @Test
    void testLocationField() {
        final AtlasSchema atlasSchema = usingAlcatraz()

        [atlasSchema.node, atlasSchema.point].forEach { LocationItemTable<LocationItem> table ->
            def select1 = select table.location from table limit 10

            final Result<LocationItem> locationItemResult = exec select1

            assert locationItemResult.relevantIdentifiers.size() == 10
        }
    }
}
