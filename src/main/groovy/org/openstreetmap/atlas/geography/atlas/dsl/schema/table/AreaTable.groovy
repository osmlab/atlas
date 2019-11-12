package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.SelectOnlyField
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.Area

/**
 * The Area table.
 *
 * @author Yazad Khambata
 */
final class AreaTable extends BaseTable<Area> {
    SelectOnlyField asPolygon = null
    SelectOnlyField closedGeometry = null
    SelectOnlyField rawGeometry = null

    AreaTable() {
        this(null)
    }

    AreaTable(final AtlasMediator atlasMediator) {
        super(atlasMediator, TableSetting.AREA)
        autoSetFields(this)
    }
}
