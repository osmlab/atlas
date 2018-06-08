import node
import atlas_entity
import entity_type


class Edge(atlas_entity.AtlasEntity):
    """
    A unidirectional Atlas Edge. Bidirectional OSM ways are represented with
    two opposing Edges, where one of them is the master Edge. The master Edge
    will have a positive identifier and the same traffic direction as OSM.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Edge. This should not be called directly.
        """
        super(Edge, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Edge.
        """
        result = '[ '
        result += 'Edge: id=' + str(self.get_identifier())

        string = ""
        string += str(self.get_start().get_identifier()) + ','
        result += ', start=[' + string + ']'

        string = ""
        string += str(self.get_end().get_identifier()) + ','
        result += ', end=[' + string + ']'

        result += ', geom=' + str(self.as_polyline())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def __eq__(self, other):
        """
        Check if Edges are equal. Edges are considered equal if they have the
        same identifiers and the same parent Atlas.
        """
        return self.get_identifier() == other.get_identifier() and self.get_parent_atlas(
        ) == other.get_parent_atlas()

    def __lt__(self, other):
        """
        Custom implementation of less-than so that collections of Edges can be
        easily sorted.
        """
        return self.get_identifier() < other.get_identifier()

    def as_polyline(self):
        """
        Get the PolyLine geometry of this Edge.
        """
        return self.get_parent_atlas()._get_edgePolyLines()[self.index]

    def get_identifier(self):
        """
        Get the Atlas identifier of this Edge.
        :return: the Atlas id
        """
        return self.get_parent_atlas()._get_edgeIdentifiers().elements[self.index]

    def get_tags(self):
        """
        Get a dictionary of this Edge's tags.
        """
        edge_tag_store = self.get_parent_atlas()._get_edgeTags()
        return edge_tag_store.to_key_value_dict(self.index)

    def get_bounds(self):
        """
        Get the bounding Rectangle of this Edge.
        """
        return self.as_polyline().get_bounds()

    def get_relations(self):
        """
        Get the frozenset of Relations of which this Edge is a member.
        Returns an empty set if this Edge is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_edgeIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_connected_nodes(self):
        """
        Get a frozenset of the Nodes connected to this Edge.
        """
        result = set()
        result.add(self.get_start())
        result.add(self.get_end())
        return frozenset(result)

    def get_start(self):
        """
        Get the starting Node of this Edge.
        """
        edge_start_node_index = self.get_parent_atlas()._get_edgeStartNodeIndex()
        index = edge_start_node_index.elements[self.index]
        return node.Node(self.get_parent_atlas(), index)

    def get_end(self):
        """
        Get the ending Node of this Edge.
        """
        edge_end_node_index = self.get_parent_atlas()._get_edgeEndNodeIndex()
        index = edge_end_node_index.elements[self.index]
        return node.Node(self.get_parent_atlas(), index)

    def get_master_edge(self):
        """
        Get the master for this Edge. Returns itself if this is the master Edge.
        """
        if self.is_master_edge():
            return self
        else:
            return self.get_parent_atlas().edge(-1 * self.get_identifier())

    def is_master_edge(self):
        """
        Checks if this Edge is a master edge.
        """
        return self.get_identifier() > 0

    def get_type(self):
        """
        Implement superclass get_type(). Always returns EntityType.EDGE.
        """
        return entity_type.EntityType.EDGE
