package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
class FieldTest extends AbstractAQLTest {

    @Test
    void testEqualsAndHashCode() {
        final AtlasSchema atlasSchema1 = usingAlcatraz()
        final AtlasSchema atlasSchema2 = usingButterflyPark()

        final Map<TableSetting, AtlasTable<AtlasEntity>> tables1 = atlasSchema1.allTables
        final Map<TableSetting, AtlasTable<AtlasEntity>> tables2 = atlasSchema2.allTables

        tables1.entrySet().stream().forEach { tableEntry ->
            final AtlasTable<AtlasEntity> atlasTable = tableEntry.getValue()
            println "${atlasTable.class.simpleName}"

            final Map<String, Field> fieldMap = atlasTable.getAllFields()

            fieldMap.entrySet().stream().forEach { fieldEntry ->
                def tableField1 = fieldEntry.value
                println "\t${fieldEntry.key} -> ${ tableField1}"

                def tableField2 = tables2[tableEntry.key].getAllFields()[fieldEntry.key]
                assert tableField2 == tableField1
                assert tableField2.hashCode() == tableField1.hashCode()
            }
        }
    }
}
