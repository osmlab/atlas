import node


class Edge:
    """
    An Atlas Edge. Logically corresponds to the PackedEdge class from the
    Java implementation.
    """

    def __init__(self, parent_atlas, index):
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        result = '['
        result += 'Edge: id=' + str(self.get_identifier())
        result += ', tags=' + str(self.get_tags())

        string = ""
        string += str(self.start().get_identifier()) + ', '
        result += ', start=[' + string + ']'

        string = ""
        string += str(self.end().get_identifier()) + ', '
        result += ', end=[' + string + ']'

        result += ']'
        return result

    def __eq__(self, other):
        """
        Check if Edges are equal. Edges are considered equal if they have the
        same identifiers and the same parent Atlas.
        """
        return self.get_identifier() == other.get_identifier(
        ) and self.get_parent_atlas() == other.get_parent_atlas()

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
        # TODO implement
        raise NotImplementedError

    def get_identifier(self):
        """
        Get the Atlas identifier of this Edge.
        :return: the Atlas id
        """
        return self.get_parent_atlas()._get_edgeIdentifiers().elements[
            self.index]

    def get_tags(self):
        """
        Get a dictionary of this Edge's tags.
        :return: the dictionary
        """
        edge_tag_store = self.get_parent_atlas()._get_edgeTags()
        return edge_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the set of relations of which this Edge is a member.
        :return: the set of relations
        """
        # TODO implement
        raise NotImplementedError

    def connected_nodes(self):
        """
        Get a frozenset of the Nodes connected to this Edge.
        """
        result = set()
        result.add(self.start())
        result.add(self.end())
        return frozenset(result)

    def start(self):
        """
        Get the starting Node of this Edge.
        """
        edge_start_node_index = self.get_parent_atlas(
        )._get_edgeStartNodeIndex()
        index = edge_start_node_index.elements[self.index]
        return node.Node(self.get_parent_atlas(), index)

    def end(self):
        """
        Get the ending Node of this Edge.
        """
        edge_end_node_index = self.get_parent_atlas()._get_edgeEndNodeIndex()
        index = edge_end_node_index.elements[self.index]
        return node.Node(self.get_parent_atlas(), index)

    def is_master_edge(self):
        """
        Checks if Edge is a master edge by checking the identifier.
        """
        return self.get_identifier() > 0

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this Edge.
        :return: the parent atlas
        """
        return self.parent_atlas
