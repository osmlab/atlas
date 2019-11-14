package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.items.ConnectedNodeType
import org.openstreetmap.atlas.geography.atlas.items.Node

import java.util.function.Function

/**
 * Represents the mapped field for terminal nodes in an Edge table.
 *
 * @author Yazad Khambata
 */
class TerminalNodeIdField<C extends CompleteEntity> extends StandardMappedField<C, Node, Long> {
    final String alias

    TerminalNodeIdField(final ConnectedNodeType connectedEdgeType, final String alias) {
        super(toName(connectedEdgeType), toId)
        this.alias = alias
    }

    private static Function<Node, Long> toId = { Node node -> node.identifier }

    private static String toName(final ConnectedNodeType connectedNodeType) {
        toInfo(connectedNodeType)["name"]
    }

    private static toInfo(ConnectedNodeType connectedNodeType) {
        terminalNodeInfoMapping[connectedNodeType]
    }

    private static Map<ConnectedNodeType, String> terminalNodeInfoMapping = [
            (ConnectedNodeType.START) : [name: "start", alias: "startId"],
            (ConnectedNodeType.END): [name: "end", alias: "endId"]
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

        final TerminalNodeIdField that = (TerminalNodeIdField) o

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
