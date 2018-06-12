import node
import atlas_entity
import entity_type
import identifier_converters


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

    def get_connected_edges(self):
        """
        Get a frozenset of the Edges connected at the ends of the Nodes of this
        Edge. The set will not contain the Edge this is method called on, but
        will contain the reversed Edge if this Edge is part of a two-way road.
        """
        result = set()
        for edge0 in self.get_end().get_connected_edges():
            if self != edge0:
                result.add(edge0)
        for edge0 in self.get_start().get_connected_edges():
            if self != edge0:
                result.add(edge0)
        return result

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

    def has_reversed_edge(self):
        """
        Checks if this Edge is a member of a bidirectional Edge pairing.
        """
        return self.get_parent_atlas().edge(-1 * self.get_identifier()) is not None

    def is_reversed_edge(self, candidate):
        """
        Check if the candidate Edge is the bidirectional reverse of this Edge.
        """
        if candidate.get_type() != entity_type.EntityType.EDGE:
            return False
        return self.get_identifier() == -1 * candidate.get_identifier()

    def get_reversed_edge(self):
        """
        Get the bidirectional pair Edge to this Edge, if it exists. Returns None
        if it does not.
        """
        if not self.has_reversed_edge():
            return None
        return self.get_parent_atlas().edge(-1 * self.get_identifier())

    def get_highway_tag_value(self):
        """
        Get the value of the "highway" tag of this Edge, if present. Returns
        None if there is no "highway" tag.
        """
        tags = self.get_tags()
        if 'highway' in tags:
            return tags['highway']
        else:
            return None

    def is_connected_at_end_to(self, candidates):
        """
        Given a set of AtlasEntity candidates, test if this edge is directly
        connected at its end to at least one of the candidates.
        """
        for entity in candidates:
            if entity.get_type() == entity_type.EntityType.NODE:
                if self.get_end() == entity:
                    return True
            if entity.get_type() == entity_type.EntityType.EDGE:
                if self.get_end() == entity.get_start():
                    return True
        return False

    def is_connected_at_start_to(self, candidates):
        """
        Given a set of AtlasEntity candidates, test if this edge is directly
        connected at its start to at least one of the candidates.
        """
        for entity in candidates:
            if entity.get_type() == entity_type.EntityType.NODE:
                if self.get_start() == entity:
                    return True
            if entity.get_type() == entity_type.EntityType.EDGE:
                if self.get_start() == entity.get_end():
                    return True
        return False

    def is_way_sectioned(self):
        """
        Determine if this Edge is a way-sectioned road.
        """
        return identifier_converters.get_way_section_index(self.get_identifier()) != 0

    def get_type(self):
        """
        Implement superclass get_type(). Always returns EntityType.EDGE.
        """
        return entity_type.EntityType.EDGE
