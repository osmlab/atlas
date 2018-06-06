import zipfile

import autogen.ProtoAtlasMetaData_pb2
import autogen.ProtoLongArray_pb2
import autogen.ProtoLongArrayOfArrays_pb2
import autogen.ProtoIntegerStringDictionary_pb2
import autogen.ProtoPackedTagStore_pb2
import autogen.ProtoLongToLongMap_pb2
import autogen.ProtoPolyLineArray_pb2
import autogen.ProtoPolygonArray_pb2
import atlas_metadata
import integer_dictionary
import packed_tag_store
import polyline


class AtlasSerializer(object):
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

    _FIELD_LINE_IDENTIFIERS = 'lineIdentifiers'
    _FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX = 'lineIdentifierToLineArrayIndex'
    _FIELD_LINE_POLYLINES = 'linePolyLines'
    _FIELD_LINE_TAGS = 'lineTags'

    _FIELD_AREA_IDENTIFIERS = 'areaIdentifiers'
    _FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX = 'areaIdentifierToAreaArrayIndex'
    _FIELD_AREA_POLYGONS = 'areaPolygons'
    _FIELD_AREA_TAGS = 'areaTags'

    _FIELD_NODE_IDENTIFIERS = 'nodeIdentifiers'
    _FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX = 'nodeIdentifierToNodeArrayIndex'
    _FIELD_NODE_LOCATIONS = 'nodeLocations'
    _FIELD_NODE_TAGS = 'nodeTags'
    _FIELD_NODE_IN_EDGES_INDICES = 'nodeInEdgesIndices'
    _FIELD_NODE_OUT_EDGES_INDICES = 'nodeOutEdgesIndices'

    _FIELD_EDGE_IDENTIFIERS = 'edgeIdentifiers'
    _FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX = 'edgeIdentifierToEdgeArrayIndex'
    _FIELD_EDGE_START_NODE_INDEX = 'edgeStartNodeIndex'
    _FIELD_EDGE_END_NODE_INDEX = 'edgeEndNodeIndex'
    _FIELD_EDGE_POLYLINES = 'edgePolyLines'
    _FIELD_EDGE_TAGS = 'edgeTags'

    # yapf: disable
    _FIELD_NAMES_TO_LOAD_METHODS = {
        _FIELD_METADATA:
        '_load_metadata',
        _FIELD_DICTIONARY:
        '_load_dictionary',

        _FIELD_POINT_IDENTIFIERS:
        '_load_point_identifiers',
        _FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX:
        '_load_point_identifier_to_point_array_index',
        _FIELD_POINT_LOCATIONS:
        '_load_point_locations',
        _FIELD_POINT_TAGS:
        '_load_point_tags',

        _FIELD_LINE_IDENTIFIERS:
        '_load_line_identifiers',
        _FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX:
        '_load_line_identifier_to_line_array_index',
        _FIELD_LINE_POLYLINES:
        '_load_line_polylines',
        _FIELD_LINE_TAGS:
        '_load_line_tags',

        _FIELD_AREA_IDENTIFIERS:
        '_load_area_identifiers',
        _FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX:
        '_load_area_identifier_to_area_array_index',
        _FIELD_AREA_POLYGONS:
        '_load_area_polygons',
        _FIELD_AREA_TAGS:
        '_load_area_tags',

        _FIELD_NODE_IDENTIFIERS:
        '_load_node_identifiers',
        _FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX:
        '_load_node_identifier_to_node_array_index',
        _FIELD_NODE_LOCATIONS:
        '_load_node_locations',
        _FIELD_NODE_TAGS:
        '_load_node_tags',
        _FIELD_NODE_IN_EDGES_INDICES:
        '_load_node_in_edges_indices',
        _FIELD_NODE_OUT_EDGES_INDICES:
        '_load_node_out_edges_indices',

        _FIELD_EDGE_IDENTIFIERS:
        '_load_edge_identifiers',
        _FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX:
        '_load_edge_identifier_to_edge_array_index',
        _FIELD_EDGE_START_NODE_INDEX:
        '_load_edge_start_node_index',
        _FIELD_EDGE_END_NODE_INDEX:
        '_load_edge_end_node_index',
        _FIELD_EDGE_POLYLINES:
        '_load_edge_polylines',
        _FIELD_EDGE_TAGS:
        '_load_edge_tags'
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
        self.atlas.metaData = atlas_metadata.get_atlas_metadata_from_proto(
            proto_metadata)

    def _load_dictionary(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_DICTIONARY)
        proto_dictionary = autogen.ProtoIntegerStringDictionary_pb2.ProtoIntegerStringDictionary(
        )
        proto_dictionary.ParseFromString(zip_entry_data)
        self.atlas.dictionary = integer_dictionary.get_integer_dictionary_from_proto(
            proto_dictionary)

    def _load_point_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_IDENTIFIERS)
        self.atlas.pointIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.pointIdentifiers.ParseFromString(zip_entry_data)

    def _load_point_identifier_to_point_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.pointIdentifierToPointArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def _load_point_locations(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_LOCATIONS)
        self.atlas.pointLocations = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.pointLocations.ParseFromString(zip_entry_data)

    def _load_point_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_TAGS)
        proto_point_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore(
        )
        proto_point_tags.ParseFromString(zip_entry_data)
        self.atlas.pointTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_point_tags)

    def _load_line_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_LINE_IDENTIFIERS)
        self.atlas.lineIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.lineIdentifiers.ParseFromString(zip_entry_data)

    def _load_line_identifier_to_line_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.lineIdentifierToLineArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def _load_line_polylines(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_LINE_POLYLINES)
        proto_array = autogen.ProtoPolyLineArray_pb2.ProtoPolyLineArray()
        proto_array.ParseFromString(zip_entry_data)
        result = []
        for encoding in proto_array.encodings:
            poly_line = polyline.decompress_polyline(encoding)
            result.append(poly_line)
        self.atlas.linePolyLines = result

    def _load_line_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_LINE_TAGS)
        proto_line_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_line_tags.ParseFromString(zip_entry_data)
        self.atlas.lineTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_line_tags)

    def _load_area_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_AREA_IDENTIFIERS)
        self.atlas.areaIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.areaIdentifiers.ParseFromString(zip_entry_data)

    def _load_area_identifier_to_area_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.areaIdentifierToAreaArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def _load_area_polygons(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_AREA_POLYGONS)
        proto_array = autogen.ProtoPolygonArray_pb2.ProtoPolygonArray()
        proto_array.ParseFromString(zip_entry_data)
        result = []
        for encoding in proto_array.encodings:
            polygon = polyline.decompress_polygon(encoding)
            result.append(polygon)
        self.atlas.areaPolygons = result

    def _load_area_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_AREA_TAGS)
        proto_area_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_area_tags.ParseFromString(zip_entry_data)
        self.atlas.areaTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_area_tags)

    def _load_node_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_IDENTIFIERS)
        self.atlas.nodeIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.nodeIdentifiers.ParseFromString(zip_entry_data)

    def _load_node_identifier_to_node_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.nodeIdentifierToNodeArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def _load_node_locations(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_LOCATIONS)
        self.atlas.nodeLocations = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.nodeLocations.ParseFromString(zip_entry_data)

    def _load_node_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_TAGS)
        proto_node_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_node_tags.ParseFromString(zip_entry_data)
        self.atlas.nodeTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_node_tags)

    def _load_edge_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_EDGE_IDENTIFIERS)
        self.atlas.edgeIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.edgeIdentifiers.ParseFromString(zip_entry_data)

    def _load_edge_identifier_to_edge_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.edgeIdentifierToEdgeArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def _load_edge_start_node_index(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_EDGE_START_NODE_INDEX)
        self.atlas.edgeStartNodeIndex = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.edgeStartNodeIndex.ParseFromString(zip_entry_data)

    def _load_edge_end_node_index(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_EDGE_END_NODE_INDEX)
        self.atlas.edgeEndNodeIndex = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.edgeEndNodeIndex.ParseFromString(zip_entry_data)

    def _load_node_in_edges_indices(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_IN_EDGES_INDICES)
        self.atlas.nodeInEdgesIndices = autogen.ProtoLongArrayOfArrays_pb2.ProtoLongArrayOfArrays(
        )
        self.atlas.nodeInEdgesIndices.ParseFromString(zip_entry_data)

    def _load_node_out_edges_indices(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_OUT_EDGES_INDICES)
        self.atlas.nodeOutEdgesIndices = autogen.ProtoLongArrayOfArrays_pb2.ProtoLongArrayOfArrays(
        )
        self.atlas.nodeOutEdgesIndices.ParseFromString(zip_entry_data)

    def _load_edge_polylines(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_EDGE_POLYLINES)
        proto_array = autogen.ProtoPolyLineArray_pb2.ProtoPolyLineArray()
        proto_array.ParseFromString(zip_entry_data)
        result = []
        for encoding in proto_array.encodings:
            poly_line = polyline.decompress_polyline(encoding)
            result.append(poly_line)
        self.atlas.edgePolyLines = result

    def _load_edge_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_EDGE_TAGS)
        proto_edge_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_edge_tags.ParseFromString(zip_entry_data)
        self.atlas.edgeTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_edge_tags)

    def _load_field(self, field_name):
        if field_name not in self._FIELD_NAMES_TO_LOAD_METHODS:
            raise KeyError('unrecognized field {}'.format(field_name))

        # reflection code to get the appropriate load method for the field we are loading
        method_name = self._FIELD_NAMES_TO_LOAD_METHODS[field_name]
        load_method_to_call = getattr(self, method_name)
        load_method_to_call()


def _read_zipentry(protofile, entry):
    with zipfile.ZipFile(protofile, 'r') as myzip:
        return myzip.read(entry)


def _convert_protolongtolongmap(proto_map):
    if len(proto_map.keys.elements) != len(proto_map.values.elements):
        raise ValueError('array length mismatch')
    new_dict = {}
    for key, value in zip(proto_map.keys.elements, proto_map.values.elements):
        new_dict[key] = value
    return new_dict
