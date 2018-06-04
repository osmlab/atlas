"""The Atlas serialization code"""

import zipfile

import autogen.ProtoAtlasMetaData_pb2
import autogen.ProtoLongArray_pb2
import autogen.ProtoIntegerStringDictionary_pb2
import autogen.ProtoPackedTagStore_pb2
import autogen.ProtoLongToLongMap_pb2
import atlas_metadata
import integer_dictionary
import packed_tag_store


class AtlasSerializer:
    """Atlas serializer"""

    _FIELD_METADATA = 'metaData'
    _FIELD_DICTIONARY = 'dictionary'
    _FIELD_POINT_IDENTIFIERS = 'pointIdentifiers'
    _FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX = 'pointIdentifierToPointArrayIndex'
    _FIELD_POINT_LOCATIONS = 'pointLocations'
    _FIELD_POINT_TAGS = 'pointTags'
    _FIELD_NODE_IDENTIFIERS = 'nodeIdentifiers'
    _FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX = 'nodeIdentifierToNodeArrayIndex'
    _FIELD_NODE_LOCATIONS = 'nodeLocations'
    _FIELD_NODE_TAGS = 'nodeTags'
    _FIELD_NAMES_TO_LOAD_METHODS = {
        _FIELD_METADATA:
        'load_metadata',
        _FIELD_DICTIONARY:
        'load_dictionary',
        _FIELD_POINT_IDENTIFIERS:
        'load_point_identifiers',
        _FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX:
        'load_point_identifier_to_point_array_index',
        _FIELD_POINT_LOCATIONS:
        'load_point_locations',
        _FIELD_POINT_TAGS:
        'load_point_tags',
        _FIELD_NODE_IDENTIFIERS:
        'load_node_identifiers',
        _FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX:
        'load_node_identifier_to_node_array_index',
        _FIELD_NODE_LOCATIONS:
        'load_node_locations',
        _FIELD_NODE_TAGS:
        'load_node_tags'
    }

    def __init__(self, atlas_file, atlas):
        self.atlas_file = atlas_file
        self.atlas = atlas

    def load_all_fields(self):
        for field in self._FIELD_NAMES_TO_LOAD_METHODS:
            self.load_field(field)

    def load_metadata(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_METADATA)
        proto_metadata = autogen.ProtoAtlasMetaData_pb2.ProtoAtlasMetaData()
        proto_metadata.ParseFromString(zip_entry_data)
        self.atlas.metaData = atlas_metadata.get_atlas_metadata_from_proto(
            proto_metadata)

    def load_dictionary(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_DICTIONARY)
        proto_dictionary = autogen.ProtoIntegerStringDictionary_pb2.ProtoIntegerStringDictionary(
        )
        proto_dictionary.ParseFromString(zip_entry_data)
        self.atlas.dictionary = integer_dictionary.get_integer_dictionary_from_proto(
            proto_dictionary)

    def load_point_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_IDENTIFIERS)
        self.atlas.pointIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.pointIdentifiers.ParseFromString(zip_entry_data)

    def load_point_identifier_to_point_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.pointIdentifierToPointArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def load_point_locations(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_LOCATIONS)
        self.atlas.pointLocations = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.pointLocations.ParseFromString(zip_entry_data)

    def load_point_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_POINT_TAGS)
        proto_point_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore(
        )
        proto_point_tags.ParseFromString(zip_entry_data)
        self.atlas.pointTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_point_tags)

    def load_node_identifiers(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_IDENTIFIERS)
        self.atlas.nodeIdentifiers = autogen.ProtoLongArray_pb2.ProtoLongArray(
        )
        self.atlas.nodeIdentifiers.ParseFromString(zip_entry_data)

    def load_node_identifier_to_node_array_index(self):
        zip_entry_data = _read_zipentry(
            self.atlas_file, self._FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
        proto_map = autogen.ProtoLongToLongMap_pb2.ProtoLongToLongMap()
        proto_map.ParseFromString(zip_entry_data)

        self.atlas.nodeIdentifierToNodeArrayIndex = _convert_protolongtolongmap(
            proto_map)

    def load_node_locations(self):
        zip_entry_data = _read_zipentry(self.atlas_file,
                                        self._FIELD_NODE_LOCATIONS)
        self.atlas.nodeLocations = autogen.ProtoLongArray_pb2.ProtoLongArray()
        self.atlas.nodeLocations.ParseFromString(zip_entry_data)

    def load_node_tags(self):
        zip_entry_data = _read_zipentry(self.atlas_file, self._FIELD_NODE_TAGS)
        proto_node_tags = autogen.ProtoPackedTagStore_pb2.ProtoPackedTagStore()
        proto_node_tags.ParseFromString(zip_entry_data)
        self.atlas.nodeTags = packed_tag_store.get_packed_tag_store_from_proto(
            proto_node_tags)

    def load_field(self, field_name):
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
