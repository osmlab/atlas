package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.field.Constrainable
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.dsl.selection.AbstractConstraintTest
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
class BasicConstraintTest extends AbstractConstraintTest {
    @Test
    void testEqualsAndHashCode() {

        final Map<TableSetting, AtlasTable<AtlasEntity>> supportedTables = AtlasDB.supportedTables

        supportedTables.entrySet().stream().forEach { tableEntry ->

            final TableSetting tableSetting = tableEntry.key
            final AtlasTable<AtlasEntity> atlasTable = tableEntry.value

            final Map<String, Field> allFields = atlasTable.allFields

            allFields.entrySet().stream().forEach { fieldEntry ->
                final Field field = fieldEntry.value
                if (!(field instanceof Constrainable)) {
                    return
                }

                final BinaryOperation operation = BinaryOperations.eq
                final Class<AtlasEntity> memberClass = tableSetting.memberClass
                final ScanType scanType = ScanType.ID_UNIQUE_INDEX
                def valueToCheck = 1L

                final Constraint<AtlasEntity> constraint1 = createBasicConstraint(field, operation, valueToCheck, scanType, memberClass)
                final Constraint<AtlasEntity> constraint2 = createBasicConstraint(field, operation, valueToCheck, scanType, memberClass)

                assertEquals(constraint1, constraint2)

                def copy1 = constraint1.deepCopy()
                def copy2 = constraint2.deepCopy()

                assertEquals(copy1, copy2)
                assertEquals(copy1, constraint2)
            }
        }
    }

    private BasicConstraint createBasicConstraint(Field field, BinaryOperation operation, long valueToCheck, ScanType scanType, Class<AtlasEntity> memberClass) {
        BasicConstraint.builder()
                .field(field)
                .operation(operation)
                .valueToCheck(valueToCheck)
                .bestCandidateScanType(scanType)
                .atlasEntityClass(memberClass)
                .build()
    }
}
