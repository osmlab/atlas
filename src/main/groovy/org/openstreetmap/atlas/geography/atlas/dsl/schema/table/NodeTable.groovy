package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.ConnectedEdgeIdField
import org.openstreetmap.atlas.geography.atlas.dsl.field.StandardField
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.ConnectedEdgeType
import org.openstreetmap.atlas.geography.atlas.items.Node

/**
 * The node table.
 *
 * @author Yazad Khambata
 */
final class NodeTable extends LocationItemTable<Node> {
    StandardField inEdges
    StandardField outEdges

    ConnectedEdgeIdField inEdgeIds = new ConnectedEdgeIdField(ConnectedEdgeType.IN, "inEdgeIds")
    ConnectedEdgeIdField outEdgeIds = new ConnectedEdgeIdField(ConnectedEdgeType.OUT, "outEdgeIds")

    NodeTable() {
        this(null)
    }

    NodeTable(AtlasMediator atlasMediator) {
        super(atlasMediator, TableSetting.NODE)
        autoSetFields(this)
    }
}
