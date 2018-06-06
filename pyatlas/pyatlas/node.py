import location
import atlas_entity
import edge


class Node(atlas_entity.AtlasEntity):
    """
    An Atlas Node.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Node. This should not be called directly.
        """
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        """
        Transform this Node into its string representation.
        """
        result = '[ '
        result += 'Node: id=' + str(self.get_identifier())
        result += ', location_latlon=' + str(self.get_location())

        string = ""
        for edge in self.in_edges():
            string += str(edge.get_identifier()) + ', '
        result += ', inEdges=[' + string + ']'

        string = ""
        for edge in self.out_edges():
            string += str(edge.get_identifier()) + ', '
        result += ', outEdges=[' + string + ']'

        result += ', tags=' + str(self.get_tags())
        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this node.
        :return: the Atlas id
        """
        return self.get_parent_atlas()._get_nodeIdentifiers().elements[
            self.index]

    def get_location(self):
        """
        Get the Location of this Node. Check the Location class for more
        information.
        :return: the location
        """
        long_location = self.get_parent_atlas()._get_nodeLocations().elements[
            self.index]
        return location.get_location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this Node's tags.
        :return: the dictionary
        """
        node_tag_store = self.get_parent_atlas()._get_nodeTags()
        return node_tag_store.to_key_value_dict(self.index)

    def in_edges(self):
        """
        Get a list of incoming Edges to this Node. The list is sorted by the
        Edges' Atlas IDs.
        :return:
        """
        result = []
        node_in_edges_indices = self.get_parent_atlas(
        )._get_nodeInEdgesIndices()
        for index in node_in_edges_indices.arrays[self.index].elements:
            result.append(edge.Edge(self.get_parent_atlas(), index))
        return sorted(result)

    def out_edges(self):
        """
        Get a list of outgoing Edges from this Node. The list is sorted by the
        Edges' Atlas IDs.
        :return:
        """
        result = []
        node_out_edges_indices = self.get_parent_atlas(
        )._get_nodeOutEdgesIndices()
        for index in node_out_edges_indices.arrays[self.index].elements:
            result.append(edge.Edge(self.get_parent_atlas(), index))
        return sorted(result)

    def connected_edges(self):
        """
        Get a list of all Edges connected to this Node. The list is sorted by
        the Edges' Atlas IDs.
        :return:
        """
        result = []
        for edge in self.in_edges():
            result.append(edge)
        for edge in self.out_edges():
            result.append(edge)
        return sorted(result)

    def get_relations(self):
        """
        Get the set of relations of which this Node is a member.
        :return: the set of relations
        """
        # TODO implement
        raise NotImplementedError

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this Node.
        :return: the parent atlas
        """
        return self.parent_atlas
