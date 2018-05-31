"""The Atlas serialization code"""

import zipfile

import autogen.ProtoAtlasMetaData_pb2
import atlas_metadata


class AtlasSerializer:
    """Atlas serializer"""

    _FIELD_METADATA = 'metaData'
    _FIELD_NAMES_TO_LOAD_METHODS = {_FIELD_METADATA: 'load_metadata'}

    def __init__(self, atlas_file, atlas):
        self.atlas_file = atlas_file
        self.atlas = atlas

    def load_all_fields(self):
        for field in self._FIELD_NAMES_TO_LOAD_METHODS:
            self.load_field(field)

    def load_metadata(self):
        zip_entry_data = self._read_zipentry(self.atlas_file,
                                             self._FIELD_METADATA)
        proto_metadata = autogen.ProtoAtlasMetaData_pb2.ProtoAtlasMetaData()
        proto_metadata.ParseFromString(zip_entry_data)
        self.atlas.metaData = atlas_metadata.get_metadata_from_proto(
            proto_metadata)

    def load_field(self, field_name):
        if field_name not in self._FIELD_NAMES_TO_LOAD_METHODS:
            raise KeyError('Unrecognized field {}'.format(field_name))

        # reflection code to get the appropriate load method for the field we are loading
        method_name = self._FIELD_NAMES_TO_LOAD_METHODS[field_name]
        load_method_to_call = getattr(self, method_name)
        load_method_to_call()

    def _read_zipentry(self, protofile, entry):
        with zipfile.ZipFile(protofile, 'r') as myzip:
            return myzip.read(entry)
