"""
This module defines the Atlas entity types. Entities are features that can be
queried from the Atlas, so things like Nodes, Lines, Relations, etc. The Atlas
entities themselves are flyweight classes, and simply store references into the
Atlas feature arrays.

In general, the entity classes in this module should not be instantiated
directly. Instead, entity objects should be obtained through the appropriate
Atlas API method.
"""

import identifier_converters
import geometry


class EntityType(object):
    """
    An enum for AtlasEntity types. Valid settings are NODE, EDGE, AREA, LINE,
    POINT, and RELATION.
    """

    def __init__(self):
        raise NotImplementedError

    # these MUST match the Java implementation for serialization compatibility
    NODE = 0
    EDGE = 1
    AREA = 2
    LINE = 3
    POINT = 4
    RELATION = 5

    _strs = {
        NODE: "NODE",
        EDGE: "EDGE",
        AREA: "AREA",
        LINE: "LINE",
        POINT: "POINT",
        RELATION: "RELATION",
    }


class AtlasEntity(geometry.Boundable):
    """
    A tagged, located entity in an Atlas. Can be a member of a relation.
    An AtlasEntity should not be instantiated directly. Use one of the
    appropriate sub-classes.
    """

    def __init__(self, parent_atlas):
        """
        AtlasEntity should not be instantiated directly.
        """
        self.parent_atlas = parent_atlas

    def __eq__(self, other):
        """
        Determine if this AtlasEntity is equal to another. Two entities are
        considered equal if they have the same identifier and the same type.
        """
        return self.get_identifier() == other.get_identifier() and self.get_type(
        ) == other.get_type()

    def __ne__(self, other):
        """
        Determine if this AtlasEntity is NOT equal to another. Inverse of the
        comparison made by the __eq__() method.
        """
        return not self.__eq__(other)

    def __hash__(self):
        """
        Compute a hashcode for this AtlasEntity.
        """
        return self.get_identifier() * 31 + self.get_type()

    def get_identifier(self):
        """
        Get the Atlas identifier of this AtlasEntity.
        """
        raise NotImplementedError('subclass must implement')

    def get_tags(self):
        """
        Get a dictionary of this AtlasEntity's tags.
        """
        raise NotImplementedError('subclass must implement')

    def bounds(self):
        """
        Compute the bounding Rectangle of this AtlasEntity.
        """
        raise NotImplementedError('subclass must implement')

    def intersects(self, polygon):
        """
        Check if this AtlasEntity intersects some Polygon.
        """
        raise NotImplementedError('subclass must implement')

    def relations(self):
        """
        Get the set of Relations of which this AtlasEntity is a member.
        """
        raise NotImplementedError('subclass must implement')

    def get_type(self):
        """
        Get the EntityType of this AtlasEntity
        """
        raise NotImplementedError('subclass must implement')

    def get_osm_identifier(self):
        """
        Get the OSM identifier of this AtlasEntity.
        """
        atlas_id = self.get_identifier()
        return identifier_converters.get_osm_identifier(atlas_id)

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this AtlasEntity.
        """
        return self.parent_atlas

    def _get_relations_helper(self, relation_map, index):
        """
        Subclasses of AtlasEntity can use this helper function to
        avoid code duplication in their relations() implementations.
        """
        relation_set = set()

        if index not in relation_map:
            return relation_set

        for relation_index in relation_map[index]:
            relation = Relation(self.get_parent_atlas(), relation_index)
            relation_set.add(relation)

        return frozenset(relation_set)


class Point(AtlasEntity):
    """
    An Atlas Point. Points are non-navigable.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Point. This should not be called directly.
        """
        super(Point, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Point.
        """
        result = '[ '
        result += 'Point: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_location())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Point.
        """
        return self.get_parent_atlas()._get_pointIdentifiers().elements[self.index]

    def as_location(self):
        """
        Get the Location of this Point.
        """
        long_location = self.get_parent_atlas()._get_pointLocations().elements[self.index]
        return geometry.location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this Point's tags.
        """
        point_tag_store = self.get_parent_atlas()._get_pointTags()
        return point_tag_store.to_key_value_dict(self.index)

    def bounds(self):
        """
        Compute the bounding Rectangle of this Point.
        """
        return self.as_location().bounds()

    def intersects(self, polygon):
        """
        Check if this Point intersects some Polygon.
        """
        return self.as_location().intersects(polygon)

    def relations(self):
        """
        Get the frozenset of Relations of which this Point is a member.
        Returns an empty set if this Point is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_pointIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Overrides superclass get_type(). Always returns EntityType.POINT.
        """
        return EntityType.POINT


class Line(AtlasEntity):
    """
    An Atlas Line. Lines are non-navigable.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Line. This should not be called directly.
        """
        super(Line, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Line.
        """
        result = '[ '
        result += 'Line: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_polyline())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def as_polyline(self):
        """
        Get the PolyLine geometry of this Line.
        """
        return self.get_parent_atlas()._get_linePolyLines()[self.index]

    def get_identifier(self):
        """
        Get the Atlas identifier of this Line.
        """
        return self.get_parent_atlas()._get_lineIdentifiers().elements[self.index]

    def get_tags(self):
        """
        Get a dictionary of this Line's tags.
        """
        line_tag_store = self.get_parent_atlas()._get_lineTags()
        return line_tag_store.to_key_value_dict(self.index)

    def bounds(self):
        """
        Compute the bounding Rectangle of this Line.
        """
        return self.as_polyline().bounds()

    def intersects(self, polygon):
        """
        Check if this Line intersects some Polygon.
        """
        return self.as_polyline().intersects(polygon)

    def relations(self):
        """
        Get the frozenset of Relations of which this Line is a member.
        Returns an empty set if this Line is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_lineIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Overrides superclass get_type(). Always returns EntityType.LINE.
        """
        return EntityType.LINE


class Area(AtlasEntity):
    """
    An Atlas Area.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Area. This should not be called directly.
        """
        super(Area, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Area.
        """
        result = '[ '
        result += 'Area: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_polygon())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Area.
        """
        return self.get_parent_atlas()._get_areaIdentifiers().elements[self.index]

    def as_polygon(self):
        """
        Get the Polygon geometry of this Area.
        """
        return self.get_parent_atlas()._get_areaPolygons()[self.index]

    def get_tags(self):
        """
        Get a dictionary of this Area's tags.
        """
        area_tag_store = self.get_parent_atlas()._get_areaTags()
        return area_tag_store.to_key_value_dict(self.index)

    def bounds(self):
        """
        Compute the bounding Rectangle of this Area.
        """
        return self.as_polygon().bounds()

    def intersects(self, polygon):
        """
        Check if this Area intersects some Polygon.
        """
        return self.as_polygon().intersects(polygon)

    def relations(self):
        """
        Get the frozenset of Relations of which this Area is a member.
        Returns an empty set if this Area is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_areaIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Overrides superclass get_type(). Always returns EntityType.AREA.
        """
        return EntityType.AREA


class Node(AtlasEntity):
    """
    An Atlas Node. A Node is like a Point, except it is part of a navigable
    structure that can be traversed using the Node and Edge API methods.
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
        result += ', geom=' + str(self.as_location())
        result += ', tags=' + str(self.get_tags())

        string = ""
        for edge in self.in_edges():
            string += str(edge.get_identifier()) + ','
        result += ', inEdges=[' + string + ']'

        string = ""
        for edge in self.out_edges():
            string += str(edge.get_identifier()) + ','
        result += ', outEdges=[' + string + ']'

        string = ''
        for relation in self.relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this node.
        """
        return self.get_parent_atlas()._get_nodeIdentifiers().elements[self.index]

    def as_location(self):
        """
        Get the Location of this Node.
        """
        long_location = self.get_parent_atlas()._get_nodeLocations().elements[self.index]
        return geometry.location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this Node's tags.
        """
        node_tag_store = self.get_parent_atlas()._get_nodeTags()
        return node_tag_store.to_key_value_dict(self.index)

    def bounds(self):
        """
        Compute the bounding Rectangle of this Point.
        """
        return self.as_location().bounds()

    def intersects(self, polygon):
        """
        Check if this Node intersects some Polygon.
        """
        return self.as_location().intersects(polygon)

    def in_edges(self):
        """
        Get a list of incoming Edges to this Node. The list is sorted by the
        Edges' Atlas IDs.
        """
        result = []
        node_in_edges_indices = self.get_parent_atlas()._get_nodeInEdgesIndices()
        for index in node_in_edges_indices.arrays[self.index].elements:
            result.append(Edge(self.get_parent_atlas(), index))
        return sorted(result)

    def out_edges(self):
        """
        Get a list of outgoing Edges from this Node. The list is sorted by the
        Edges' Atlas IDs.
        """
        result = []
        node_out_edges_indices = self.get_parent_atlas()._get_nodeOutEdgesIndices()
        for index in node_out_edges_indices.arrays[self.index].elements:
            result.append(Edge(self.get_parent_atlas(), index))
        return sorted(result)

    def connected_edges(self):
        """
        Get a list of all Edges connected to this Node. The list is sorted by
        the Edges' Atlas IDs.
        """
        result = []
        for edge in self.in_edges():
            result.append(edge)
        for edge in self.out_edges():
            result.append(edge)
        return sorted(result)

    def get_absolute_valence(self):
        """
        Get the number of Edges connected to this node. Considers all Edges, not
        just master Edges.
        """
        return len(self.connected_edges())

    def get_valence(self):
        """
        Get the number of Edges connected to this node. Only considers the
        master Edges.
        """
        connected_edges = self.connected_edges()
        valence = 0
        for edge in connected_edges:
            if edge.is_master_edge():
                valence += 1
        return valence

    def relations(self):
        """
        Get the frozenset of Relations of which this Node is a member.
        Returns an empty set if this Node is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_nodeIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Overrides superclass get_type(). Always returns EntityType.NODE.
        """
        return EntityType.NODE


class Edge(AtlasEntity):
    """
    A unidirectional Atlas Edge. An Edge is like a Line, except it is part of a
    navigable structure that can be traversed using the Node and Edge API methods.

    Bidirectional OSM ways are represented with two opposing Edges, where one
    of them is the master Edge. The master Edge will have a positive identifier
    and the same traffic direction as OSM.
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
        string += str(self.start().get_identifier()) + ','
        result += ', start=[' + string + ']'

        string = ""
        string += str(self.end().get_identifier()) + ','
        result += ', end=[' + string + ']'

        result += ', geom=' + str(self.as_polyline())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.relations():
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

    def bounds(self):
        """
        Compute the bounding Rectangle of this Edge.
        """
        return self.as_polyline().bounds()

    def intersects(self, polygon):
        """
        Check if this Edge intersects some Polygon.
        """
        return self.as_polyline().intersects(polygon)

    def relations(self):
        """
        Get the frozenset of Relations of which this Edge is a member.
        Returns an empty set if this Edge is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_edgeIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def connected_nodes(self):
        """
        Get a frozenset of the Nodes connected to this Edge.
        """
        result = set()
        result.add(self.start())
        result.add(self.end())
        return frozenset(result)

    def connected_edges(self):
        """
        Get a frozenset of the Edges connected at the ends of the Nodes of this
        Edge. The set will not contain the Edge this method is called on, but
        will contain the reversed Edge if this Edge is part of a two-way road.
        """
        result = set()
        for edge in self.end().connected_edges():
            if self != edge:
                result.add(edge)
        for edge in self.start().connected_edges():
            if self != edge:
                result.add(edge)
        return result

    def start(self):
        """
        Get the starting Node of this Edge.
        """
        edge_start_node_index = self.get_parent_atlas()._get_edgeStartNodeIndex()
        index = edge_start_node_index.elements[self.index]
        return Node(self.get_parent_atlas(), index)

    def end(self):
        """
        Get the ending Node of this Edge.
        """
        edge_end_node_index = self.get_parent_atlas()._get_edgeEndNodeIndex()
        index = edge_end_node_index.elements[self.index]
        return Node(self.get_parent_atlas(), index)

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
        if candidate.get_type() != EntityType.EDGE:
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
            if entity.get_type() == EntityType.NODE:
                if self.end() == entity:
                    return True
            if entity.get_type() == EntityType.EDGE:
                if self.end() == entity.start():
                    return True
        return False

    def is_connected_at_start_to(self, candidates):
        """
        Given a set of AtlasEntity candidates, test if this edge is directly
        connected at its start to at least one of the candidates.
        """
        for entity in candidates:
            if entity.get_type() == EntityType.NODE:
                if self.start() == entity:
                    return True
            if entity.get_type() == EntityType.EDGE:
                if self.start() == entity.end():
                    return True
        return False

    def is_way_sectioned(self):
        """
        Determine if this Edge is a way-sectioned road.
        """
        return identifier_converters.get_way_section_index(self.get_identifier()) != 0

    def get_type(self):
        """
        Overrides superclass get_type(). Always returns EntityType.EDGE.
        """
        return EntityType.EDGE


class Relation(AtlasEntity):
    """
    An Atlas Relation. Aggregates AtlasEntities in a logical relationship.
    Can contain other Relations as members.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Relation. This should not be called directly.
        """
        super(Relation, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Relation.
        """
        result = '[ '
        result += 'Relation: id=' + str(self.get_identifier())

        string = ''
        for member in self.get_members():
            string += str(member) + ','
        result += ', members=[' + string + ']'

        string = ''
        for relation in self.relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ', tags=' + str(self.get_tags())
        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Relation.
        """
        return self.get_parent_atlas()._get_relationIdentifiers().elements[self.index]

    def get_members(self):
        """
        Get a sorted list of this Relation's members. The members are in
        RelationMember form.
        """
        result = []
        relation_identifiers = self.get_parent_atlas()._get_relationIdentifiers()
        relation_member_types = self.get_parent_atlas()._get_relationMemberTypes()
        relation_member_indices = self.get_parent_atlas()._get_relationMemberIndices()
        relation_member_roles = self.get_parent_atlas()._get_relationMemberRoles()
        dictionary = self.get_parent_atlas()._get_dictionary()

        array_index = 0
        # the relationMemberTypes field is a byte array, so the Python treats
        # it as a string. We need to convert it to a true byte array.
        for type_value in bytearray(relation_member_types.arrays[self.index].elements):
            member_index = relation_member_indices.arrays[self.index].elements[array_index]
            role = dictionary.word(relation_member_roles.arrays[self.index].elements[array_index])

            if type_value == EntityType.NODE:
                entity = Node(self.get_parent_atlas(), member_index)
            elif type_value == EntityType.EDGE:
                entity = Edge(self.get_parent_atlas(), member_index)
            elif type_value == EntityType.AREA:
                entity = Area(self.get_parent_atlas(), member_index)
            elif type_value == EntityType.LINE:
                entity = Line(self.get_parent_atlas(), member_index)
            elif type_value == EntityType.POINT:
                entity = Point(self.get_parent_atlas(), member_index)
            elif type_value == EntityType.RELATION:
                entity = Relation(self.get_parent_atlas(), member_index)
            else:
                raise ValueError('invalid EntityType value ' + str(type_value))
            result.append(RelationMember(role, entity, relation_identifiers.elements[self.index]))
            array_index += 1

        return sorted(result)

    def get_tags(self):
        """
        Get a dictionary of this Relation's tags.
        """
        relation_tag_store = self.get_parent_atlas()._get_relationTags()
        return relation_tag_store.to_key_value_dict(self.index)

    def bounds(self):
        """
        Compute the bounding Rectangle of this Relation's members.
        """
        # FIXME this fails if Relations have self-referencing members
        # this will never happen in a PackedAtlas so it should be OK for now
        # if pyatlas ever supports MultiAtlas then this will be a concern

        members = self.get_members()
        if len(members) == 0:
            return geometry.Rectangle(0, 0)

        entities_to_consider = []
        for member in self.get_members():
            entity = member.get_entity()
            if entity is None:
                raise ValueError('entity was None, how did this happen?')
            entities_to_consider.append(entity)

        return geometry.bounds_atlasentities(entities_to_consider)

    def intersects(self, polygon):
        """
        Check if any member of this Relation intersects some Polygon.
        """
        # FIXME this fails if Relations have self-referencing members
        # this will never happen in a PackedAtlas so it should be OK for now
        # if pyatlas ever supports MultiAtlas then this will be a concern

        for member in self.get_members():
            entity = member.get_entity()
            if entity.intersects(polygon):
                return True

        return False

    def relations(self):
        """
        Get the frozenset of Relations of which this Relation is a member.
        Returns an empty set if this Relation is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_relationIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Overrides superclass get_type(). Always returns EntityType.RELATION.
        """
        return EntityType.RELATION


class RelationMember(object):
    """
    A container type for Relation members. A RelationMember has a role as well
    as a reference to its AtlasEntity.
    """

    def __init__(self, role, entity, identifier):
        """
        Create a new RelationMember.
        """
        self.role = role
        self.entity = entity
        self.identifier = identifier

    def __lt__(self, other):
        """
        Define an ordering for RelationMembers. Compare EntityTypes, then
        identifiers, then roles.
        """
        if self.entity.get_type() < other.entity.get_type():
            return True
        elif self.entity.get_type() > other.entity.get_type():
            return False
        else:
            if self.identifier < other.identifier:
                return True
            elif self.identifier > other.identifier:
                return False
            else:
                if self.role < other.role:
                    return True
                else:
                    return False

    def __str__(self):
        """
        Get a string representation of this RelationMember.
        """
        result = '[ '
        result += 'id=' + str(self.get_entity().get_identifier())
        result += ', type=' + entity_type_to_str(self.entity.get_type())
        result += ', role=' + str(self.get_role())
        result += ' ]'
        return result

    def get_entity(self):
        """
        Get this RelationMember's AtlasEntity.
        """
        return self.entity

    def get_relation_identifier(self):
        """
        Get the identifier of the Relation of which this RelationMember is a member.
        """
        return self.identifier

    def get_role(self):
        """
        Get the role of this RelationMember.
        """
        return self.role


def entity_type_to_str(value):
    """
    Convert an EntityType enum to a string representation.
    """
    return EntityType._strs[value]
