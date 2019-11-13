package org.openstreetmap.atlas.geography.atlas.dsl.schema

import groovy.transform.ToString
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.*
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors

/**
 * The table references in this class are used in the from clause of select or update statement of update.
 *
 * @author Yazad Khambata
 */
@ToString(includes = ["uri"])
class AtlasSchema {
    final NodeTable node
    final PointTable point
    final LineTable line
    final EdgeTable edge
    final RelationTable relation
    final AreaTable area

    String uri

    AtlasMediator atlasMediator

    AtlasSchema(String uri) {

        this(new AtlasMediator(uri), uri)
    }

    AtlasSchema(AtlasMediator atlasMediator) {
        this(atlasMediator, null)
    }

    AtlasSchema(final AtlasMediator atlasMediator, final String uri) {
        this.atlasMediator = atlasMediator
        this.uri = uri

        this.node = new NodeTable(atlasMediator)
        this.point = new PointTable(atlasMediator)
        this.line = new LineTable(atlasMediator)
        this.edge = new EdgeTable(atlasMediator)
        this.relation = new RelationTable(atlasMediator)
        this.area = new AreaTable(atlasMediator)
    }

    List<String> getAllTableNames() {
        Arrays.stream(TableSetting.values()).map { tableSetting -> tableSetting.name().toLowerCase() }.collect(Collectors.toList())
    }

    private final static String[] EXCLUDE = ["node", "point", "line", "edge", "relation", "area", "uri"]

    def <E extends AtlasEntity> Map<TableSetting, AtlasTable<E>> getAllTables() {
        [
                (TableSetting.NODE)    : node,
                (TableSetting.POINT)   : point,
                (TableSetting.LINE)    : line,
                (TableSetting.EDGE)    : edge,
                (TableSetting.RELATION): relation,
                (TableSetting.AREA)    : area
        ]
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that, EXCLUDE)
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this, EXCLUDE)
    }
}
