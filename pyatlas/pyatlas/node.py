import location
import atlas_entity
import edge
import entity_type


class Node(atlas_entity.AtlasEntity):
    """
    An Atlas Node. A Node is like a Point, except it is part of a navigable
    structure. It supports the notion of incoming and outgoing edges.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Node. This should not be called directly.
        """
        super(Node, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Node.
        """
        result = '[ '
        result += 'Node: id=' + str(self.get_identifier())
        result += ', location_latlon=' + str(self.get_location())
        result += ', tags=' + str(self.get_tags())

        string = ""
        for edge0 in self.get_in_edges():
            string += str(edge0.get_identifier()) + ','
        result += ', inEdges=[' + string + ']'

        string = ""
        for edge0 in self.get_out_edges():
            string += str(edge0.get_identifier()) + ','
        result += ', outEdges=[' + string + ']'

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this node.
        """
        return self.get_parent_atlas()._get_nodeIdentifiers().elements[self.index]

    def get_location(self):
        """
        Get the Location of this Node. Check the Location class for more
        information.
        """
        long_location = self.get_parent_atlas()._get_nodeLocations().elements[self.index]
        return location.get_location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this Node's tags.
        """
        node_tag_store = self.get_parent_atlas()._get_nodeTags()
        return node_tag_store.to_key_value_dict(self.index)

    def get_in_edges(self):
        """
        Get a list of incoming Edges to this Node. The list is sorted by the
        Edges' Atlas IDs.
        """
        result = []
        node_in_edges_indices = self.get_parent_atlas()._get_nodeInEdgesIndices()
        for index in node_in_edges_indices.arrays[self.index].elements:
            result.append(edge.Edge(self.get_parent_atlas(), index))
        return sorted(result)

    def get_out_edges(self):
        """
        Get a list of outgoing Edges from this Node. The list is sorted by the
        Edges' Atlas IDs.
        """
        result = []
        node_out_edges_indices = self.get_parent_atlas()._get_nodeOutEdgesIndices()
        for index in node_out_edges_indices.arrays[self.index].elements:
            result.append(edge.Edge(self.get_parent_atlas(), index))
        return sorted(result)

    def get_connected_edges(self):
        """
        Get a list of all Edges connected to this Node. The list is sorted by
        the Edges' Atlas IDs.
        """
        result = []
        for edge0 in self.get_in_edges():
            result.append(edge0)
        for edge0 in self.get_out_edges():
            result.append(edge0)
        return sorted(result)

    def get_relations(self):
        """
        Get the frozenset of Relations of which this Node is a member.
        Returns an empty set if this Node is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_nodeIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Implement superclass get_type(). Always returns NODE.
        """
        return entity_type.EntityType.NODE
