package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.SelectOnlyField
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.Relation

/**
 * The relation table.
 *
 * @author Yazad Khambata
 */
final class RelationTable extends BaseTable<Relation> {
    SelectOnlyField allRelationsWithSameOsmIdentifier = null
    SelectOnlyField allKnownOsmMembers = null
    SelectOnlyField members = null
    SelectOnlyField osmRelationIdentifier = null
    SelectOnlyField isMultiPolygon = null

    RelationTable() {
        this(null)
    }

    RelationTable(AtlasMediator atlasMediator) {
        super(atlasMediator, TableSetting.RELATION)
        autoSetFields(this)
    }
}
