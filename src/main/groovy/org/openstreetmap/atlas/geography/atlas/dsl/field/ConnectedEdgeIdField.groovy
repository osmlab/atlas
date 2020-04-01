package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.items.ConnectedEdgeType
import org.openstreetmap.atlas.geography.atlas.items.Edge

import java.util.function.Function
import java.util.stream.Collectors

/**
 * Mapping of in and out edges in a node.
 *
 * @author Yazad Khambata
 */
class ConnectedEdgeIdField<C extends CompleteEntity> extends StandardMappedField<C, Set<Edge>, Set<Long>> {

    final String alias

    ConnectedEdgeIdField(final ConnectedEdgeType connectedEdgeType, final String alias) {
        super(toName(connectedEdgeType), toId)
        this.alias = alias
    }

    private static Function<Set<Edge>, Set<Long>> toId = { Set<Edge> edges -> edges?.stream().map { edge -> edge.identifier }.collect(Collectors.toSet()) }

    private static String toName(final ConnectedEdgeType connectedEdgeType) {
        toInfo(connectedEdgeType)["name"]
    }

    private static toInfo(ConnectedEdgeType connectedEdgeType) {
        connectedEdgeTypeInfoMapping[connectedEdgeType]
    }

    private static Map<ConnectedEdgeType, String> connectedEdgeTypeInfoMapping = [
            (ConnectedEdgeType.IN) : [name: "inEdges", alias: "inEdgeIds"],
            (ConnectedEdgeType.OUT): [name: "outEdges", alias: "outEdgeIds"]
    ]

    @Override
    String getAlias() {
        alias
    }

    @Override
    boolean equals(final o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        final ConnectedEdgeIdField that = (ConnectedEdgeIdField) o

        if (alias != that.alias) return false

        return true
    }

    @Override
    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (alias != null ? alias.hashCode() : 0)
        return result
    }
}
