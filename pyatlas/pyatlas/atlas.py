"""The Atlas class definition"""

from atlas_serializer import AtlasSerializer
from point import Point
from node import Node
from edge import Edge


class Atlas:
    """
    The Atlas - current implementation is not threadsafe.

    The field names match up with the name of their corresponding ZIP entry.
    These ZIP entry names come from from the PackedAtlas Java implementation.
    """

    def __init__(self, atlas_file, lazy_loading=True):
        self.serializer = AtlasSerializer(atlas_file, self)
        self.lazy_loading = lazy_loading

        ### PackedAtlas fields ###
        self.metaData = None
        self.dictionary = None

        self.pointIdentifiers = None
        self.pointIdentifierToPointArrayIndex = None
        self.pointLocations = None
        self.pointTags = None

        self.nodeIdentifiers = None
        self.nodeIdentifierToNodeArrayIndex = None
        self.nodeLocations = None
        self.nodeTags = None
        self.nodeInEdgesIndices = None
        self.nodeOutEdgesIndices = None

        self.edgeIdentifiers = None
        self.edgeIdentifierToEdgeArrayIndex = None
        self.edgeStartNodeIndex = None
        self.edgeEndNodeIndex = None
        self.edgePolyLines = None
        self.edgeTags = None

        if not self.lazy_loading:
            self.load_all_fields()

    def get_metadata(self):
        """
        Get the metadata associated with this Atlas. See class AtlasMetaData
        for more information.
        :return: the metadata
        """
        if self.metaData is None:
            self.serializer.load_field(self.serializer._FIELD_METADATA)
        return self.metaData

    def points(self, predicate=lambda point: True):
        """
        Get a generator for Points in this Atlas. Can optionally also accept a
        predicate to filter the generated Points.
        :param predicate: a Point filter predicate
        :return: the Point generator
        """
        for i, element in enumerate(self._get_pointIdentifiers().elements):
            point = Point(self, i)
            if predicate(point):
                yield point

    def point(self, identifier):
        """
        Get a Point with a given Atlas identifier. Returns None if there is no
        Point with the given identifier.
        :param identifier: the identifier
        :return: the point with this identifier, None if it does not exist
        """
        identifier_to_index = self._get_pointIdentifierToPointArrayIndex()
        if identifier in identifier_to_index:
            return Point(self, identifier_to_index[identifier])
        return None

    def nodes(self, predicate=lambda point: True):
        """
        Get a generator for Nodes in this Atlas. Can optionally also accept a
        predicate to filter the generated Nodes.
        :param predicate: a Node filter predicate
        :return: the Node generator
        """
        for i, element in enumerate(self._get_nodeIdentifiers().elements):
            node = Node(self, i)
            if predicate(node):
                yield node

    def node(self, identifier):
        """
        Get a Node with a given Atlas identifier. Returns None if there is no
        Node with the given identifier.
        :param identifier: the identifier
        :return: the Node with this identifier, None if it does not exist
        """
        identifier_to_index = self._get_nodeIdentifierToNodeArrayIndex()
        if identifier in identifier_to_index:
            return Node(self, identifier_to_index[identifier])
        return None

    def edges(self, predicate=lambda point: True):
        """
        Get a generator for Edges in this Atlas. Can optionally also accept a
        predicate to filter the generated Edges.
        :param predicate: a Edges filter predicate
        :return: the Edge generator
        """
        for i, element in enumerate(self._get_edgeIdentifiers().elements):
            edge = Edge(self, i)
            if predicate(edge):
                yield edge

    def edge(self, identifier):
        """
        Get an Edge with a given Atlas identifier. Returns None if there is no
        Edge with the given identifier.
        :param identifier: the identifier
        :return: the Edge with this identifier, None if it does not exist
        """
        identifier_to_index = self._get_edgeIdentifierToNodeArrayIndex()
        if identifier in identifier_to_index:
            return Edge(self, identifier_to_index[identifier])
        return None

    def load_all_fields(self):
        """
        Force this Atlas to load all its fields from its backing store.
        """
        self.serializer.load_all_fields()

    def _get_pointIdentifiers(self):
        if self.pointIdentifiers is None:
            self.serializer.load_field(
                self.serializer._FIELD_POINT_IDENTIFIERS)
        return self.pointIdentifiers

    def _get_pointIdentifierToPointArrayIndex(self):
        if self.pointIdentifierToPointArrayIndex is None:
            self.serializer.load_field(
                self.serializer._FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
        return self.pointIdentifierToPointArrayIndex

    def _get_pointLocations(self):
        if self.pointLocations is None:
            self.serializer.load_field(self.serializer._FIELD_POINT_LOCATIONS)
        return self.pointLocations

    def _get_pointTags(self):
        if self.pointTags is None:
            self.serializer.load_field(self.serializer._FIELD_POINT_TAGS)
        self.pointTags.set_dictionary(self._get_dictionary())
        return self.pointTags

    def _get_nodeIdentifiers(self):
        if self.nodeIdentifiers is None:
            self.serializer.load_field(self.serializer._FIELD_NODE_IDENTIFIERS)
        return self.nodeIdentifiers

    def _get_nodeIdentifierToNodeArrayIndex(self):
        if self.nodeIdentifierToNodeArrayIndex is None:
            self.serializer.load_field(
                self.serializer._FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
        return self.nodeIdentifierToNodeArrayIndex

    def _get_nodeLocations(self):
        if self.nodeLocations is None:
            self.serializer.load_field(self.serializer._FIELD_NODE_LOCATIONS)
        return self.nodeLocations

    def _get_nodeTags(self):
        if self.nodeTags is None:
            self.serializer.load_field(self.serializer._FIELD_NODE_TAGS)
        self.nodeTags.set_dictionary(self._get_dictionary())
        return self.nodeTags

    def _get_nodeInEdgesIndices(self):
        if self.nodeInEdgesIndices is None:
            self.serializer.load_field(
                self.serializer._FIELD_NODE_IN_EDGES_INDICES)
        return self.nodeInEdgesIndices

    def _get_nodeOutEdgesIndices(self):
        if self.nodeOutEdgesIndices is None:
            self.serializer.load_field(
                self.serializer._FIELD_NODE_OUT_EDGES_INDICES)
        return self.nodeOutEdgesIndices

    def _get_edgeIdentifiers(self):
        if self.edgeIdentifiers is None:
            self.serializer.load_field(self.serializer._FIELD_EDGE_IDENTIFIERS)
        return self.edgeIdentifiers

    def _get_edgeIdentifierToNodeArrayIndex(self):
        if self.edgeIdentifierToEdgeArrayIndex is None:
            self.serializer.load_field(
                self.serializer._FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX)
        return self.edgeIdentifierToEdgeArrayIndex

    def _get_edgeStartNodeIndex(self):
        if self.edgeStartNodeIndex is None:
            self.serializer.load_field(
                self.serializer._FIELD_EDGE_START_NODE_INDEX)
        return self.edgeStartNodeIndex

    def _get_edgeEndNodeIndex(self):
        if self.edgeEndNodeIndex is None:
            self.serializer.load_field(
                self.serializer._FIELD_EDGE_END_NODE_INDEX)
        return self.edgeEndNodeIndex

    def _get_edgePolyLines(self):
        if self.edgePolyLines is None:
            self.serializer.load_field(self.serializer._FIELD_EDGE_POLYLINES)
        return self.edgePolyLines

    def _get_edgeTags(self):
        if self.edgeTags is None:
            self.serializer.load_field(self.serializer._FIELD_EDGE_TAGS)
        self.edgeTags.set_dictionary(self._get_dictionary())
        return self.edgeTags

    def _get_dictionary(self):
        if self.dictionary is None:
            self.serializer.load_field(self.serializer._FIELD_DICTIONARY)
        return self.dictionary
