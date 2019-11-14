package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.SelectOnlyField
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.LocationItem

/**
 * Abstraction of Location item tables.
 *
 * @author Yazad Khambata
 */
abstract class LocationItemTable<L extends LocationItem> extends BaseTable<L> {
    SelectOnlyField location = null

    LocationItemTable(final AtlasMediator atlasMediator, final TableSetting tableSetting) {
        super(atlasMediator, tableSetting)
    }
}
