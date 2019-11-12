package org.openstreetmap.atlas.geography.atlas.dsl.schema

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.*
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Defines the structure of tables and views inside the schema.
 *
 * The tables or views are NOT concerned with rendering.
 *
 * @author Yazad Khambata
 */
class AtlasDB {

    /*
     * Table References - used in the select list and where clause.
     */
    static NodeTable node = new NodeTable()
    static PointTable point = new PointTable()
    static LineTable line = new LineTable()
    static EdgeTable edge = new EdgeTable()
    static RelationTable relation = new RelationTable()
    static AreaTable area = new AreaTable()

    static <E extends AtlasEntity, T extends AtlasTable<E>> Map<TableSetting, T> getSupportedTables() {
        [
                (TableSetting.NODE): node,
                (TableSetting.POINT): point,
                (TableSetting.LINE): line,
                (TableSetting.EDGE): edge,
                (TableSetting.RELATION): relation,
                (TableSetting.AREA): area
        ]
    }
}
