package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.SelectOnlyField
import org.openstreetmap.atlas.geography.atlas.dsl.field.TerminalNodeIdField
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.ConnectedNodeType
import org.openstreetmap.atlas.geography.atlas.items.Edge

/**
 * The Edge table.
 *
 * @author Yazad Khambata
 */
final class EdgeTable extends BaseTable<Edge> {
    SelectOnlyField isWaySectioned = null
    SelectOnlyField isMasterEdge = null
    SelectOnlyField isClosed = null
    SelectOnlyField isZeroLength = null
    SelectOnlyField connectedEdges = null
    SelectOnlyField connectedNodes = null
    SelectOnlyField start = null
    SelectOnlyField end = null

    TerminalNodeIdField startId = new TerminalNodeIdField(ConnectedNodeType.START, "startId")
    TerminalNodeIdField endId = new TerminalNodeIdField(ConnectedNodeType.END, "endId")

    SelectOnlyField hasReverseEdge = null
    SelectOnlyField highwayTag = null
    SelectOnlyField inEdges = null
    SelectOnlyField outEdges = null
    SelectOnlyField reversed = null
    SelectOnlyField length = null

    EdgeTable() {
        this(null)
    }

    EdgeTable(AtlasMediator atlasMediator) {
        super(atlasMediator, TableSetting.EDGE)
        autoSetFields(this)
    }
}
