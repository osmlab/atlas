package org.openstreetmap.atlas.geography.atlas.dsl.schema

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting

/**
 * @author Yazad Khambata
 */
class TableSettingTest {

    @Test
    void sanity() {
        Arrays.stream(TableSetting.values()).forEach { tableSetting ->
            assert tableSetting.itemType.name() == tableSetting.name()
            assert tableSetting.completeItemType.name() == tableSetting.name()
        }
    }
}
