package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.SelectOnlyField
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.Line

/**
 * The Line table.
 *
 * @author Yazad Khambata
 */
final class LineTable extends BaseTable<Line> {
    SelectOnlyField asPolyLine = null
    SelectOnlyField numberOfShapePoints = null

    LineTable() {
        this(null)
    }

    LineTable(AtlasMediator atlasMediator) {
        super(atlasMediator, TableSetting.LINE)
        autoSetFields(this)
    }
}
