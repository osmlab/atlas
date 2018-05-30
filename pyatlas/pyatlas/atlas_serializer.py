"""The Atlas serialization code"""

import zipfile

import autogen.ProtoAtlasMetaData_pb2

class AtlasSerializer:
    """Atlas serializer"""

    FIELD_NAMES_TO_LOAD_METHODS = {'metaData':'load_meta_data'}

    def __init__(self, atlas_file, atlas):
        self.atlas_file = atlas_file
        self.atlas = atlas

    def load_all_fields(self):
        for field in self.FIELD_NAMES_TO_LOAD_METHODS:
            self.load_field(field)

    def load_meta_data(self):
        zip_entry_data = self.read_zipentry(self.atlas_file, 'metaData')
        self.atlas.metaData = autogen.ProtoAtlasMetaData_pb2.ProtoAtlasMetaData()
        self.atlas.metaData.ParseFromString(zip_entry_data)

    def load_field(self, field_name):
        if field_name not in self.FIELD_NAMES_TO_LOAD_METHODS:
            raise KeyError('Unrecognized field {}'.format(field_name))

        # reflection code gets the appropriate load method for the field we are loading
        method_name = self.FIELD_NAMES_TO_LOAD_METHODS[field_name]
        load_method_to_call = getattr(self, method_name)
        load_method_to_call()

    def read_zipentry(self, protofile, entry):
        with zipfile.ZipFile(protofile, 'r') as myzip:
            return myzip.read(entry)
