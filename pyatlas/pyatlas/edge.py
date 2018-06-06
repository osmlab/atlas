import node
import atlas_entity


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
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        """
        Transform this Edge into its string representation.
        """
        result = '[ '
        result += 'Edge: id=' + str(self.get_identifier())

        string = ""
        string += str(self.start().get_identifier()) + ', '
        result += ', start=[' + string + ']'

        string = ""
        string += str(self.end().get_identifier()) + ', '
        result += ', end=[' + string + ']'

        result += ', geom=' + str(self.as_polyline())
        result += ', tags=' + str(self.get_tags())
        result += ' ]'
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
        return self.get_parent_atlas()._get_edgePolyLines()[self.index]

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

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this Edge.
        :return: the parent atlas
        """
        return self.parent_atlas
