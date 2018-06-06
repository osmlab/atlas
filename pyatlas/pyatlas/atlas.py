"""The pyatlas Atlas implementation"""

from atlas_serializer import AtlasSerializer
from point import Point
from line import Line
from area import Area
from node import Node
from edge import Edge
from relation import Relation


class Atlas(object):
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
        self.pointIndexToRelationIndices = None

        self.lineIdentifiers = None
        self.lineIdentifierToLineArrayIndex = None
        self.linePolyLines = None
        self.lineTags = None

        self.areaIdentifiers = None
        self.areaIdentifierToAreaArrayIndex = None
        self.areaPolygons = None
        self.areaTags = None

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

        self.relationIdentifiers = None
        self.relationIdentifierToRelationArrayIndex = None

        if not self.lazy_loading:
            self.load_all_fields()

    def get_metadata(self):
        """
        Get the metadata associated with this Atlas. See class AtlasMetaData
        for more information on the metadata format.
        """
        if self.metaData is None:
            self.serializer._load_field(self.serializer._FIELD_METADATA)
        return self.metaData

    def points(self, predicate=lambda point: True):
        """
        Get a generator for Points in this Atlas. Can optionally also accept a
        predicate to filter the generated Points.
        """
        for i, element in enumerate(self._get_pointIdentifiers().elements):
            point = Point(self, i)
            if predicate(point):
                yield point

    def point(self, identifier):
        """
        Get a Point with a given Atlas identifier. Returns None if there is no
        Point with the given identifier.
        """
        identifier_to_index = self._get_pointIdentifierToPointArrayIndex()
        if identifier in identifier_to_index:
            return Point(self, identifier_to_index[identifier])
        return None

    def lines(self, predicate=lambda line: True):
        """
        Get a generator for Lines in this Atlas. Can optionally also accept a
        predicate to filter the generated Lines.
        """
        for i, element in enumerate(self._get_lineIdentifiers().elements):
            line = Line(self, i)
            if predicate(line):
                yield line

    def line(self, identifier):
        """
        Get a Line with a given Atlas identifier. Returns None if there is no
        Line with the given identifier.
        """
        identifier_to_index = self._get_lineIdentifierToLineArrayIndex()
        if identifier in identifier_to_index:
            return Line(self, identifier_to_index[identifier])
        return None

    def areas(self, predicate=lambda area: True):
        """
        Get a generator for Areas in this Atlas. Can optionally also accept a
        predicate to filter the generated Areas.
        """
        for i, element in enumerate(self._get_areaIdentifiers().elements):
            area = Area(self, i)
            if predicate(area):
                yield area

    def area(self, identifier):
        """
        Get an Area with a given Atlas identifier. Returns None if there is no
        Area with the given identifier.
        """
        identifier_to_index = self._get_areaIdentifierToAreaArrayIndex()
        if identifier in identifier_to_index:
            return Area(self, identifier_to_index[identifier])
        return None

    def nodes(self, predicate=lambda node: True):
        """
        Get a generator for Nodes in this Atlas. Can optionally also accept a
        predicate to filter the generated Nodes.
        """
        for i, element in enumerate(self._get_nodeIdentifiers().elements):
            node = Node(self, i)
            if predicate(node):
                yield node

    def node(self, identifier):
        """
        Get a Node with a given Atlas identifier. Returns None if there is no
        Node with the given identifier.
        """
        identifier_to_index = self._get_nodeIdentifierToNodeArrayIndex()
        if identifier in identifier_to_index:
            return Node(self, identifier_to_index[identifier])
        return None

    def edges(self, predicate=lambda edge: True):
        """
        Get a generator for Edges in this Atlas. Can optionally also accept a
        predicate to filter the generated Edges.
        """
        for i, element in enumerate(self._get_edgeIdentifiers().elements):
            edge = Edge(self, i)
            if predicate(edge):
                yield edge

    def edge(self, identifier):
        """
        Get an Edge with a given Atlas identifier. Returns None if there is no
        Edge with the given identifier.
        """
        identifier_to_index = self._get_edgeIdentifierToEdgeArrayIndex()
        if identifier in identifier_to_index:
            return Edge(self, identifier_to_index[identifier])
        return None

    def relations(self, predicate=lambda relation: True):
        """
        Get a generator for Relations in this Atlas. Can optionally also accept a
        predicate to filter the generated Relations.
        """
        for i, element in enumerate(self._get_relationIdentifiers().elements):
            relation = Relation(self, i)
            if predicate(relation):
                yield relation

    def relation(self, identifier):
        """
        Get a Relation with a given Atlas identifier. Returns None if there is no
        Relation with the given identifier.
        """
        identifier_to_index = self._get_relationIdentifierToRelationArrayIndex(
        )
        if identifier in identifier_to_index:
            return Relation(self, identifier_to_index[identifier])
        return None

    def load_all_fields(self):
        """
        Force this Atlas to load all its fields from its backing store.
        """
        self.serializer._load_all_fields()

    def _get_dictionary(self):
        if self.dictionary is None:
            self.serializer._load_field(self.serializer._FIELD_DICTIONARY)
        return self.dictionary

    def _get_pointIdentifiers(self):
        if self.pointIdentifiers is None:
            self.serializer._load_field(
                self.serializer._FIELD_POINT_IDENTIFIERS)
        return self.pointIdentifiers

    def _get_pointIdentifierToPointArrayIndex(self):
        if self.pointIdentifierToPointArrayIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
        return self.pointIdentifierToPointArrayIndex

    def _get_pointLocations(self):
        if self.pointLocations is None:
            self.serializer._load_field(self.serializer._FIELD_POINT_LOCATIONS)
        return self.pointLocations

    def _get_pointTags(self):
        if self.pointTags is None:
            self.serializer._load_field(self.serializer._FIELD_POINT_TAGS)
        self.pointTags.set_dictionary(self._get_dictionary())
        return self.pointTags

    def _get_pointIndexToRelationIndices(self):
        if self.pointIndexToRelationIndices is None:
            self.serializer._load_field(
                self.serializer._FIELD_POINT_INDEX_TO_RELATION_INDICES)
        return self.pointIndexToRelationIndices

    def _get_lineIdentifiers(self):
        if self.lineIdentifiers is None:
            self.serializer._load_field(
                self.serializer._FIELD_LINE_IDENTIFIERS)
        return self.lineIdentifiers

    def _get_lineIdentifierToLineArrayIndex(self):
        if self.lineIdentifierToLineArrayIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX)
        return self.lineIdentifierToLineArrayIndex

    def _get_linePolyLines(self):
        if self.linePolyLines is None:
            self.serializer._load_field(self.serializer._FIELD_LINE_POLYLINES)
        return self.linePolyLines

    def _get_lineTags(self):
        if self.lineTags is None:
            self.serializer._load_field(self.serializer._FIELD_LINE_TAGS)
        self.lineTags.set_dictionary(self._get_dictionary())
        return self.lineTags

    def _get_areaIdentifiers(self):
        if self.areaIdentifiers is None:
            self.serializer._load_field(
                self.serializer._FIELD_AREA_IDENTIFIERS)
        return self.areaIdentifiers

    def _get_areaIdentifierToAreaArrayIndex(self):
        if self.areaIdentifierToAreaArrayIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX)
        return self.areaIdentifierToAreaArrayIndex

    def _get_areaPolygons(self):
        if self.areaPolygons is None:
            self.serializer._load_field(self.serializer._FIELD_AREA_POLYGONS)
        return self.areaPolygons

    def _get_areaTags(self):
        if self.areaTags is None:
            self.serializer._load_field(self.serializer._FIELD_AREA_TAGS)
        self.areaTags.set_dictionary(self._get_dictionary())
        return self.areaTags

    def _get_nodeIdentifiers(self):
        if self.nodeIdentifiers is None:
            self.serializer._load_field(
                self.serializer._FIELD_NODE_IDENTIFIERS)
        return self.nodeIdentifiers

    def _get_nodeIdentifierToNodeArrayIndex(self):
        if self.nodeIdentifierToNodeArrayIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
        return self.nodeIdentifierToNodeArrayIndex

    def _get_nodeLocations(self):
        if self.nodeLocations is None:
            self.serializer._load_field(self.serializer._FIELD_NODE_LOCATIONS)
        return self.nodeLocations

    def _get_nodeTags(self):
        if self.nodeTags is None:
            self.serializer._load_field(self.serializer._FIELD_NODE_TAGS)
        self.nodeTags.set_dictionary(self._get_dictionary())
        return self.nodeTags

    def _get_nodeInEdgesIndices(self):
        if self.nodeInEdgesIndices is None:
            self.serializer._load_field(
                self.serializer._FIELD_NODE_IN_EDGES_INDICES)
        return self.nodeInEdgesIndices

    def _get_nodeOutEdgesIndices(self):
        if self.nodeOutEdgesIndices is None:
            self.serializer._load_field(
                self.serializer._FIELD_NODE_OUT_EDGES_INDICES)
        return self.nodeOutEdgesIndices

    def _get_edgeIdentifiers(self):
        if self.edgeIdentifiers is None:
            self.serializer._load_field(
                self.serializer._FIELD_EDGE_IDENTIFIERS)
        return self.edgeIdentifiers

    def _get_edgeIdentifierToEdgeArrayIndex(self):
        if self.edgeIdentifierToEdgeArrayIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX)
        return self.edgeIdentifierToEdgeArrayIndex

    def _get_edgeStartNodeIndex(self):
        if self.edgeStartNodeIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_EDGE_START_NODE_INDEX)
        return self.edgeStartNodeIndex

    def _get_edgeEndNodeIndex(self):
        if self.edgeEndNodeIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_EDGE_END_NODE_INDEX)
        return self.edgeEndNodeIndex

    def _get_edgePolyLines(self):
        if self.edgePolyLines is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_POLYLINES)
        return self.edgePolyLines

    def _get_edgeTags(self):
        if self.edgeTags is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_TAGS)
        self.edgeTags.set_dictionary(self._get_dictionary())
        return self.edgeTags

    def _get_relationIdentifiers(self):
        if self.relationIdentifiers is None:
            self.serializer._load_field(
                self.serializer._FIELD_RELATION_IDENTIFIERS)
        return self.relationIdentifiers

    def _get_relationIdentifierToRelationArrayIndex(self):
        if self.relationIdentifierToRelationArrayIndex is None:
            self.serializer._load_field(
                self.serializer.
                _FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX)
        return self.relationIdentifierToRelationArrayIndex
