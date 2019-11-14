package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.Point

/**
 * The point table.
 *
 * @author Yazad Khambata
 */
final class PointTable extends LocationItemTable<Point> {
    PointTable() {
        this(null)
    }

    PointTable(AtlasMediator atlasMediator) {
        super(atlasMediator, TableSetting.POINT)
        autoSetFields(this)
    }
}
