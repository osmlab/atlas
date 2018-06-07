"""The pyatlas Atlas implementation"""
import zipfile

import point
import line
import area
import node
import edge
import relation

import autogen.ProtoAtlasMetaData_pb2
import autogen.ProtoLongArray_pb2
import autogen.ProtoLongArrayOfArrays_pb2
import autogen.ProtoIntegerStringDictionary_pb2
import autogen.ProtoPackedTagStore_pb2
import autogen.ProtoLongToLongMap_pb2
import autogen.ProtoLongToLongMultiMap_pb2
import autogen.ProtoPolyLineArray_pb2
import autogen.ProtoPolygonArray_pb2
import autogen.ProtoByteArrayOfArrays_pb2
import autogen.ProtoIntegerArrayOfArrays_pb2
import atlas_metadata
import integer_dictionary
import packed_tag_store
import polyline


class Atlas(object):
    """
    The Atlas - current implementation is not threadsafe.

    The field names match up with the name of their corresponding ZIP entry.
    These ZIP entry names come from from the PackedAtlas Java implementation.
    """

    def __init__(self, atlas_file, lazy_loading=True):
        """
        Create a new Atlas backed by a specified atlas file.

        Args:
            atlas_file (str): The path to the atlas file.
            lazy_loading (bool, optional): Specify if this Atlas should use
                lazy deserialization. Defaults to True. Setting this to False
                causes all the deserialization to happen upfront.
        """
        self.serializer = _AtlasSerializer(atlas_file, self)
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
        self.lineIndexToRelationIndices = None

        self.areaIdentifiers = None
        self.areaIdentifierToAreaArrayIndex = None
        self.areaPolygons = None
        self.areaTags = None
        self.areaIndexToRelationIndices = None

        self.nodeIdentifiers = None
        self.nodeIdentifierToNodeArrayIndex = None
        self.nodeLocations = None
        self.nodeTags = None
        self.nodeInEdgesIndices = None
        self.nodeOutEdgesIndices = None
        self.nodeIndexToRelationIndices = None

        self.edgeIdentifiers = None
        self.edgeIdentifierToEdgeArrayIndex = None
        self.edgeStartNodeIndex = None
        self.edgeEndNodeIndex = None
        self.edgePolyLines = None
        self.edgeTags = None
        self.edgeIndexToRelationIndices = None

        self.relationIdentifiers = None
        self.relationIdentifierToRelationArrayIndex = None
        self.relationMemberTypes = None
        self.relationMemberIndices = None
        self.relationMemberRoles = None
        self.relationTags = None
        self.relationIndexToRelationIndices = None

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

    def points(self, predicate=lambda p: True):
        """
        Get a generator for Points in this Atlas. Can optionally also accept a
        predicate to filter the generated Points.
        """
        for i, element in enumerate(self._get_pointIdentifiers().elements):
            point0 = point.Point(self, i)
            if predicate(point0):
                yield point0

    def point(self, identifier):
        """
        Get a Point with a given Atlas identifier. Returns None if there is no
        Point with the given identifier.
        """
        identifier_to_index = self._get_pointIdentifierToPointArrayIndex()
        if identifier in identifier_to_index:
            return point.Point(self, identifier_to_index[identifier])
        return None

    def lines(self, predicate=lambda l: True):
        """
        Get a generator for Lines in this Atlas. Can optionally also accept a
        predicate to filter the generated Lines.
        """
        for i, element in enumerate(self._get_lineIdentifiers().elements):
            line0 = line.Line(self, i)
            if predicate(line0):
                yield line0

    def line(self, identifier):
        """
        Get a Line with a given Atlas identifier. Returns None if there is no
        Line with the given identifier.
        """
        identifier_to_index = self._get_lineIdentifierToLineArrayIndex()
        if identifier in identifier_to_index:
            return line.Line(self, identifier_to_index[identifier])
        return None

    def areas(self, predicate=lambda a: True):
        """
        Get a generator for Areas in this Atlas. Can optionally also accept a
        predicate to filter the generated Areas.
        """
        for i, element in enumerate(self._get_areaIdentifiers().elements):
            area0 = area.Area(self, i)
            if predicate(area0):
                yield area0

    def area(self, identifier):
        """
        Get an Area with a given Atlas identifier. Returns None if there is no
        Area with the given identifier.
        """
        identifier_to_index = self._get_areaIdentifierToAreaArrayIndex()
        if identifier in identifier_to_index:
            return area.Area(self, identifier_to_index[identifier])
        return None

    def nodes(self, predicate=lambda n: True):
        """
        Get a generator for Nodes in this Atlas. Can optionally also accept a
        predicate to filter the generated Nodes.
        """
        for i, element in enumerate(self._get_nodeIdentifiers().elements):
            node0 = node.Node(self, i)
            if predicate(node0):
                yield node0

    def node(self, identifier):
        """
        Get a Node with a given Atlas identifier. Returns None if there is no
        Node with the given identifier.
        """
        identifier_to_index = self._get_nodeIdentifierToNodeArrayIndex()
        if identifier in identifier_to_index:
            return node.Node(self, identifier_to_index[identifier])
        return None

    def edges(self, predicate=lambda e: True):
        """
        Get a generator for Edges in this Atlas. Can optionally also accept a
        predicate to filter the generated Edges.
        """
        for i, element in enumerate(self._get_edgeIdentifiers().elements):
            edge0 = edge.Edge(self, i)
            if predicate(edge0):
                yield edge0

    def edge(self, identifier):
        """
        Get an Edge with a given Atlas identifier. Returns None if there is no
        Edge with the given identifier.
        """
        identifier_to_index = self._get_edgeIdentifierToEdgeArrayIndex()
        if identifier in identifier_to_index:
            return edge.Edge(self, identifier_to_index[identifier])
        return None

    def relations(self, predicate=lambda r: True):
        """
        Get a generator for Relations in this Atlas. Can optionally also accept a
        predicate to filter the generated Relations.
        """
        for i, element in enumerate(self._get_relationIdentifiers().elements):
            relation0 = relation.Relation(self, i)
            if predicate(relation0):
                yield relation0

    def relation(self, identifier):
        """
        Get a Relation with a given Atlas identifier. Returns None if there is no
        Relation with the given identifier.
        """
        identifier_to_index = self._get_relationIdentifierToRelationArrayIndex()
        if identifier in identifier_to_index:
            return relation.Relation(self, identifier_to_index[identifier])
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
            self.serializer._load_field(self.serializer._FIELD_POINT_IDENTIFIERS)
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
            self.serializer._load_field(self.serializer._FIELD_POINT_INDEX_TO_RELATION_INDICES)
        return self.pointIndexToRelationIndices

    def _get_lineIdentifiers(self):
        if self.lineIdentifiers is None:
            self.serializer._load_field(self.serializer._FIELD_LINE_IDENTIFIERS)
        return self.lineIdentifiers

    def _get_lineIdentifierToLineArrayIndex(self):
        if self.lineIdentifierToLineArrayIndex is None:
            self.serializer._load_field(self.serializer._FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX)
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

    def _get_lineIndexToRelationIndices(self):
        if self.lineIndexToRelationIndices is None:
            self.serializer._load_field(self.serializer._FIELD_LINE_INDEX_TO_RELATION_INDICES)
        return self.lineIndexToRelationIndices

    def _get_areaIdentifiers(self):
        if self.areaIdentifiers is None:
            self.serializer._load_field(self.serializer._FIELD_AREA_IDENTIFIERS)
        return self.areaIdentifiers

    def _get_areaIdentifierToAreaArrayIndex(self):
        if self.areaIdentifierToAreaArrayIndex is None:
            self.serializer._load_field(self.serializer._FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX)
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

    def _get_areaIndexToRelationIndices(self):
        if self.areaIndexToRelationIndices is None:
            self.serializer._load_field(self.serializer._FIELD_AREA_INDEX_TO_RELATION_INDICES)
        return self.areaIndexToRelationIndices

    def _get_nodeIdentifiers(self):
        if self.nodeIdentifiers is None:
            self.serializer._load_field(self.serializer._FIELD_NODE_IDENTIFIERS)
        return self.nodeIdentifiers

    def _get_nodeIdentifierToNodeArrayIndex(self):
        if self.nodeIdentifierToNodeArrayIndex is None:
            self.serializer._load_field(self.serializer._FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
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
            self.serializer._load_field(self.serializer._FIELD_NODE_IN_EDGES_INDICES)
        return self.nodeInEdgesIndices

    def _get_nodeOutEdgesIndices(self):
        if self.nodeOutEdgesIndices is None:
            self.serializer._load_field(self.serializer._FIELD_NODE_OUT_EDGES_INDICES)
        return self.nodeOutEdgesIndices

    def _get_nodeIndexToRelationIndices(self):
        if self.nodeIndexToRelationIndices is None:
            self.serializer._load_field(self.serializer._FIELD_NODE_INDEX_TO_RELATION_INDICES)
        return self.nodeIndexToRelationIndices

    def _get_edgeIdentifiers(self):
        if self.edgeIdentifiers is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_IDENTIFIERS)
        return self.edgeIdentifiers

    def _get_edgeIdentifierToEdgeArrayIndex(self):
        if self.edgeIdentifierToEdgeArrayIndex is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX)
        return self.edgeIdentifierToEdgeArrayIndex

    def _get_edgeStartNodeIndex(self):
        if self.edgeStartNodeIndex is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_START_NODE_INDEX)
        return self.edgeStartNodeIndex

    def _get_edgeEndNodeIndex(self):
        if self.edgeEndNodeIndex is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_END_NODE_INDEX)
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

    def _get_edgeIndexToRelationIndices(self):
        if self.edgeIndexToRelationIndices is None:
            self.serializer._load_field(self.serializer._FIELD_EDGE_INDEX_TO_RELATION_INDICES)
        return self.edgeIndexToRelationIndices

    def _get_relationIdentifiers(self):
        if self.relationIdentifiers is None:
            self.serializer._load_field(self.serializer._FIELD_RELATION_IDENTIFIERS)
        return self.relationIdentifiers

    def _get_relationIdentifierToRelationArrayIndex(self):
        if self.relationIdentifierToRelationArrayIndex is None:
            self.serializer._load_field(
                self.serializer._FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX)
        return self.relationIdentifierToRelationArrayIndex

    def _get_relationMemberTypes(self):
        if self.relationMemberTypes is None:
            self.serializer._load_field(self.serializer._FIELD_RELATION_MEMBER_TYPES)
        return self.relationMemberTypes

    def _get_relationMemberIndices(self):
        if self.relationMemberIndices is None:
            self.serializer._load_field(self.serializer._FIELD_RELATION_MEMBER_INDICES)
        return self.relationMemberIndices

    def _get_relationMemberRoles(self):
        if self.relationMemberRoles is None:
            self.serializer._load_field(self.serializer._FIELD_RELATION_MEMBER_ROLES)
        return self.relationMemberRoles

    def _get_relationTags(self):
        if self.relationTags is None:
            self.serializer._load_field(self.serializer._FIELD_RELATION_TAGS)
        self.relationTags.set_dictionary(self._get_dictionary())
        return self.relationTags

    def _get_relationIndexToRelationIndices(self):
        if self.relationIndexToRelationIndices is None:
            self.serializer._load_field(self.serializer._FIELD_RELATION_INDEX_TO_RELATION_INDICES)
        return self.relationIndexToRelationIndices


class _AtlasSerializer(object):
    """
    The Atlas serializer. Used by Atlas to read ZIP entries from the
    backing store. This class should not be used directly.
    """

    _FIELD_METADATA = 'metaData'
    _FIELD_DICTIONARY = 'dictionary'

    _FIELD_POINT_IDENTIFIERS = 'pointIdentifiers'
    _FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX = 'pointIdentifierToPointArrayIndex'
    _FIELD_POINT_LOCATIONS = 'pointLocations'
    _FIELD_POINT_TAGS = 'pointTags'
    _FIELD_POINT_INDEX_TO_RELATION_INDICES = 'pointIndexToRelationIndices'

    _FIELD_LINE_IDENTIFIERS = 'lineIdentifiers'
    _FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX = 'lineIdentifierToLineArrayIndex'
    _FIELD_LINE_POLYLINES = 'linePolyLines'
    _FIELD_LINE_TAGS = 'lineTags'
    _FIELD_LINE_INDEX_TO_RELATION_INDICES = 'lineIndexToRelationIndices'

    _FIELD_AREA_IDENTIFIERS = 'areaIdentifiers'
    _FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX = 'areaIdentifierToAreaArrayIndex'
    _FIELD_AREA_POLYGONS = 'areaPolygons'
    _FIELD_AREA_TAGS = 'areaTags'
    _FIELD_AREA_INDEX_TO_RELATION_INDICES = 'areaIndexToRelationIndices'

    _FIELD_NODE_IDENTIFIERS = 'nodeIdentifiers'
    _FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX = 'nodeIdentifierToNodeArrayIndex'
    _FIELD_NODE_LOCATIONS = 'nodeLocations'
    _FIELD_NODE_TAGS = 'nodeTags'
    _FIELD_NODE_IN_EDGES_INDICES = 'nodeInEdgesIndices'
    _FIELD_NODE_OUT_EDGES_INDICES = 'nodeOutEdgesIndices'
    _FIELD_NODE_INDEX_TO_RELATION_INDICES = 'nodeIndexToRelationIndices'

    _FIELD_EDGE_IDENTIFIERS = 'edgeIdentifiers'
    _FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX = 'edgeIdentifierToEdgeArrayIndex'
    _FIELD_EDGE_START_NODE_INDEX = 'edgeStartNodeIndex'
    _FIELD_EDGE_END_NODE_INDEX = 'edgeEndNodeIndex'
    _FIELD_EDGE_POLYLINES = 'edgePolyLines'
    _FIELD_EDGE_TAGS = 'edgeTags'
    _FIELD_EDGE_INDEX_TO_RELATION_INDICES = 'edgeIndexToRelationIndices'

    _FIELD_RELATION_IDENTIFIERS = 'relationIdentifiers'
    _FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX = 'relationIdentifierToRelationArrayIndex'
    _FIELD_RELATION_MEMBER_TYPES = 'relationMemberTypes'
    _FIELD_RELATION_MEMBER_INDICES = 'relationMemberIndices'
    _FIELD_RELATION_MEMBER_ROLES = 'relationMemberRoles'
    _FIELD_RELATION_TAGS = 'relationTags'
    _FIELD_RELATION_INDEX_TO_RELATION_INDICES = 'relationIndexToRelationIndices'

    # yapf: disable
    _FIELD_NAMES_TO_LOAD_METHODS = {
        _FIELD_METADATA:
        '_load_metadata',
        _FIELD_DICTIONARY:
        '_load_dictionary',

        _FIELD_POINT_IDENTIFIERS:
        '_load_pointIdentifiers',
        _FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX:
        '_load_pointIdentifierToPointArrayIndex',
        _FIELD_POINT_LOCATIONS:
        '_load_pointLocations',
        _FIELD_POINT_TAGS:
        '_load_pointTags',
        _FIELD_POINT_INDEX_TO_RELATION_INDICES:
        '_load_pointIndexToRelationIndices',

        _FIELD_LINE_IDENTIFIERS:
        '_load_lineIdentifiers',
        _FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX:
        '_load_lineIdentifierToLineArrayIndex',
        _FIELD_LINE_POLYLINES:
        '_load_linePolylines',
        _FIELD_LINE_TAGS:
        '_load_lineTags',
        _FIELD_LINE_INDEX_TO_RELATION_INDICES:
        '_load_lineIndexToRelationIndices',

        _FIELD_AREA_IDENTIFIERS:
        '_load_areaIdentifiers',
        _FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX:
        '_load_areaIdentifierToAreaArrayIndex',
        _FIELD_AREA_POLYGONS:
        '_load_areaPolygons',
        _FIELD_AREA_TAGS:
        '_load_areaTags',
        _FIELD_AREA_INDEX_TO_RELATION_INDICES:
        '_load_areaIndexToRelationIndices',

        _FIELD_NODE_IDENTIFIERS:
        '_load_nodeIdentifiers',
        _FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX:
        '_load_nodeIdentifierToNodeArrayIndex',
        _FIELD_NODE_LOCATIONS:
        '_load_nodeLocations',
        _FIELD_NODE_TAGS:
        '_load_nodeTags',
        _FIELD_NODE_IN_EDGES_INDICES:
        '_load_nodeInEdgesIndices',
        _FIELD_NODE_OUT_EDGES_INDICES:
        '_load_nodeOutEdgesIndices',
        _FIELD_NODE_INDEX_TO_RELATION_INDICES:
        '_load_nodeIndexToRelationIndices',

        _FIELD_EDGE_IDENTIFIERS:
        '_load_edgeIdentifiers',
        _FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX:
        '_load_edgeIdentifierToEdgeArrayIndex',
        _FIELD_EDGE_START_NODE_INDEX:
        '_load_edgeStartNodeIndex',
        _FIELD_EDGE_END_NODE_INDEX:
        '_load_edgeEndNodeIndex',
        _FIELD_EDGE_POLYLINES:
        '_load_edgePolylines',
        _FIELD_EDGE_TAGS:
        '_load_edgeTags',
        _FIELD_EDGE_INDEX_TO_RELATION_INDICES:
        '_load_edgeIndexToRelationIndices',

        _FIELD_RELATION_IDENTIFIERS:
        '_load_relationIdentifiers',
        _FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX:
        '_load_relationIdentifierToRelationArrayIndex',
        _FIELD_RELATION_MEMBER_TYPES:
        '_load_relationMemberTypes',
        _FIELD_RELATION_MEMBER_INDICES:
        '_load_relationMemberIndices',
        _FIELD_RELATION_MEMBER_ROLES:
        '_load_relationMemberRoles',
        _FIELD_RELATION_TAGS:
        '_load_relationTags',
        _FIELD_RELATION_INDEX_TO_RELATION_INDICES:
        '_load_relationIndexToRelationIndices'
    }
    # yapf: enable

    def __init__(self, atlas_file, atlas):
        self.atlas_file = atlas_file
        self.atlas = atlas

    def _load_all_fields(self):
        for field in self._FIELD_NAMES_TO_LOAD_METHODS:
            self._load_field(field)

    def _load_metadata(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_METADATA)
        proto_metadata = autogen.ProtoAtlasMetaData_pb2.ProtoAtlasMetaData()
        proto_metadata.ParseFromString(zip_entry_data)
        self.atlas.metaData = atlas_metadata.get_atlas_metadata_from_proto(proto_metadata)

    def _load_dictionary(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_DICTIONARY)
        proto_dictionary = autogen.ProtoIntegerStringDictionary_pb2.ProtoIntegerStringDictionary()
        proto_dictionary.ParseFromString(zip_entry_data)
        self.atlas.dictionary = integer_dictionary.get_integer_dictionary_from_proto(
            proto_dictionary)

    def _load_pointIdentifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_POINT_IDENTIFIERS)
        self.atlas.pointIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.pointIdentifiers.ParseFromString(zip_entry_data)

    def _load_pointIdentifierToPointArrayIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)
        self.atlas.pointIdentifierToPointArrayIndex = _convert_protolongtolongmap(proto_map)

    def _load_pointLocations(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_POINT_LOCATIONS)
        self.atlas.pointLocations = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.pointLocations.ParseFromString(zip_entry_data)

    def _load_pointTags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_POINT_TAGS)
        proto_point_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_point_tags.ParseFromString(zip_entry_data)
        self.atlas.pointTags = packed_tag_store.get_packed_tag_store_from_proto(proto_point_tags)

    def _load_pointIndexToRelationIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_INDEX_TO_RELATION_INDICES)
        proto_multimap = autogen.ProtoLongToLongMultiMap_pb2.ProtoLongToLongMultiMap()
        proto_multimap.ParseFromString(zip_entry_data)
        self.atlas.pointIndexToRelationIndices = _convert_protolongtolongmultimap(proto_multimap)

    def _load_lineIdentifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_LINE_IDENTIFIERS)
        self.atlas.lineIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.lineIdentifiers.ParseFromString(zip_entry_data)

    def _load_lineIdentifierToLineArrayIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)
        self.atlas.lineIdentifierToLineArrayIndex = _convert_protolongtolongmap(proto_map)

    def _load_linePolylines(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_LINE_POLYLINES)
        proto_array = autogen.ProtoPolyLineArray_pb2.ProtoPolyLineArray()
        proto_array.ParseFromString(zip_entry_data)
        result = []
        for encoding in proto_array.encodings:
            poly_line = polyline.decompress_polyline(encoding)
            result.append(poly_line)
        self.atlas.linePolyLines = result

    def _load_lineTags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_LINE_TAGS)
        proto_line_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_line_tags.ParseFromString(zip_entry_data)
        self.atlas.lineTags = packed_tag_store.get_packed_tag_store_from_proto(proto_line_tags)

    def _load_lineIndexToRelationIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_LINE_INDEX_TO_RELATION_INDICES)
        proto_multimap = autogen.ProtoLongToLongMultiMap_pb2.ProtoLongToLongMultiMap()
        proto_multimap.ParseFromString(zip_entry_data)
        self.atlas.lineIndexToRelationIndices = _convert_protolongtolongmultimap(proto_multimap)

    def _load_areaIdentifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_AREA_IDENTIFIERS)
        self.atlas.areaIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.areaIdentifiers.ParseFromString(zip_entry_data)

    def _load_areaIdentifierToAreaArrayIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)
        self.atlas.areaIdentifierToAreaArrayIndex = _convert_protolongtolongmap(proto_map)

    def _load_areaPolygons(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_AREA_POLYGONS)
        proto_array = autogen.ProtoPolygonArray_pb2.ProtoPolygonArray()
        proto_array.ParseFromString(zip_entry_data)
        result = []
        for encoding in proto_array.encodings:
            polygon = polyline.decompress_polygon(encoding)
            result.append(polygon)
        self.atlas.areaPolygons = result

    def _load_areaTags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_AREA_TAGS)
        proto_area_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_area_tags.ParseFromString(zip_entry_data)
        self.atlas.areaTags = packed_tag_store.get_packed_tag_store_from_proto(proto_area_tags)

    def _load_areaIndexToRelationIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_AREA_INDEX_TO_RELATION_INDICES)
        proto_multimap = autogen.ProtoLongToLongMultiMap_pb2.ProtoLongToLongMultiMap()
        proto_multimap.ParseFromString(zip_entry_data)
        self.atlas.areaIndexToRelationIndices = _convert_protolongtolongmultimap(proto_multimap)

    def _load_nodeIdentifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_IDENTIFIERS)
        self.atlas.nodeIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.nodeIdentifiers.ParseFromString(zip_entry_data)

    def _load_nodeIdentifierToNodeArrayIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)
        self.atlas.nodeIdentifierToNodeArrayIndex = _convert_protolongtolongmap(proto_map)

    def _load_nodeLocations(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_LOCATIONS)
        self.atlas.nodeLocations = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.nodeLocations.ParseFromString(zip_entry_data)

    def _load_nodeTags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_TAGS)
        proto_node_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_node_tags.ParseFromString(zip_entry_data)
        self.atlas.nodeTags = packed_tag_store.get_packed_tag_store_from_proto(proto_node_tags)

    def _load_nodeInEdgesIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_IN_EDGES_INDICES)
        self.atlas.nodeInEdgesIndices = autogen.ProtoLongArrayOfArrays_pb2.ProtoLongArrayOfArrays()
        self.atlas.nodeInEdgesIndices.ParseFromString(zip_entry_data)

    def _load_nodeOutEdgesIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_OUT_EDGES_INDICES)
        self.atlas.nodeOutEdgesIndices = autogen.ProtoLongArrayOfArrays_pb2.ProtoLongArrayOfArrays()
        self.atlas.nodeOutEdgesIndices.ParseFromString(zip_entry_data)

    def _load_nodeIndexToRelationIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_INDEX_TO_RELATION_INDICES)
        proto_multimap = autogen.ProtoLongToLongMultiMap_pb2.ProtoLongToLongMultiMap()
        proto_multimap.ParseFromString(zip_entry_data)
        self.atlas.nodeIndexToRelationIndices = _convert_protolongtolongmultimap(proto_multimap)

    def _load_edgeIdentifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_IDENTIFIERS)
        self.atlas.edgeIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.edgeIdentifiers.ParseFromString(zip_entry_data)

    def _load_edgeIdentifierToEdgeArrayIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)
        self.atlas.edgeIdentifierToEdgeArrayIndex = _convert_protolongtolongmap(proto_map)

    def _load_edgeStartNodeIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_START_NODE_INDEX)
        self.atlas.edgeStartNodeIndex = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.edgeStartNodeIndex.ParseFromString(zip_entry_data)

    def _load_edgeEndNodeIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_END_NODE_INDEX)
        self.atlas.edgeEndNodeIndex = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.edgeEndNodeIndex.ParseFromString(zip_entry_data)

    def _load_edgePolylines(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_POLYLINES)
        proto_array = autogen.ProtoPolyLineArray_pb2.ProtoPolyLineArray()
        proto_array.ParseFromString(zip_entry_data)
        result = []
        for encoding in proto_array.encodings:
            poly_line = polyline.decompress_polyline(encoding)
            result.append(poly_line)
        self.atlas.edgePolyLines = result

    def _load_edgeTags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_TAGS)
        proto_edge_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_edge_tags.ParseFromString(zip_entry_data)
        self.atlas.edgeTags = packed_tag_store.get_packed_tag_store_from_proto(proto_edge_tags)

    def _load_edgeIndexToRelationIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_INDEX_TO_RELATION_INDICES)
        proto_multimap = autogen.ProtoLongToLongMultiMap_pb2.ProtoLongToLongMultiMap()
        proto_multimap.ParseFromString(zip_entry_data)
        self.atlas.edgeIndexToRelationIndices = _convert_protolongtolongmultimap(proto_multimap)

    def _load_relationIdentifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_RELATION_IDENTIFIERS)
        self.atlas.relationIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.relationIdentifiers.ParseFromString(zip_entry_data)

    def _load_relationIdentifierToRelationArrayIndex(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)
        self.atlas.relationIdentifierToRelationArrayIndex = _convert_protolongtolongmap(proto_map)

    def _load_relationMemberTypes(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_RELATION_MEMBER_TYPES)
        self.atlas.relationMemberTypes = autogen.ProtoByteArrayOfArrays_pb2.ProtoByteArrayOfArrays()
        self.atlas.relationMemberTypes.ParseFromString(zip_entry_data)

    def _load_relationMemberIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_RELATION_MEMBER_INDICES)
        self.atlas.relationMemberIndices = autogen.ProtoLongArrayOfArrays_pb2.ProtoLongArrayOfArrays(
        )
        self.atlas.relationMemberIndices.ParseFromString(zip_entry_data)

    def _load_relationMemberRoles(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_RELATION_MEMBER_ROLES)
        self.atlas.relationMemberRoles = autogen.ProtoIntegerArrayOfArrays_pb2.ProtoIntegerArrayOfArrays(
        )
        self.atlas.relationMemberRoles.ParseFromString(zip_entry_data)

    def _load_relationTags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_RELATION_TAGS)
        proto_relation_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_relation_tags.ParseFromString(zip_entry_data)
        self.atlas.relationTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_relation_tags)

    def _load_relationIndexToRelationIndices(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_RELATION_INDEX_TO_RELATION_INDICES)
        proto_multimap = autogen.ProtoLongToLongMultiMap_pb2.ProtoLongToLongMultiMap()
        proto_multimap.ParseFromString(zip_entry_data)
        self.atlas.relationIndexToRelationIndices = _convert_protolongtolongmultimap(proto_multimap)

    def _load_field(self, field_name):
        if field_name not in self._FIELD_NAMES_TO_LOAD_METHODS:
            raise KeyError('unrecognized field {}'.format(field_name))

        # reflection code to get the appropriate load method for the field we are loading
        method_name = self._FIELD_NAMES_TO_LOAD_METHODS[field_name]
        load_method_to_call = getattr(self, method_name)
        load_method_to_call()


## some private static utility functions ##


def _read_zipentry(zip_file, entry):
    # read a zip entry named 'entry' from a given 'zip_file'
    with zipfile.ZipFile(zip_file, 'r') as myzip:
        return myzip.read(entry)


def _convert_protolongtolongmap(proto_map):
    # convert the ProtoLongToLongMap_pb2 type to a simple dict
    if len(proto_map.keys.elements) != len(proto_map.values.elements):
        raise ValueError('array length mismatch')
    new_dict = {}
    for key, value in zip(proto_map.keys.elements, proto_map.values.elements):
        new_dict[key] = value
    return new_dict


def _convert_protolongtolongmultimap(proto_map):
    # convert the ProtoLongToLongMultiMap_pb2 type to a simple dict
    if len(proto_map.keys.elements) != len(proto_map.values):
        raise ValueError('array length mismatch')
    new_dict = {}
    for key, array_value in zip(proto_map.keys.elements, proto_map.values):
        value_list = []
        for value in array_value.elements:
            value_list.append(value)
        new_dict[key] = value_list
    return new_dict
