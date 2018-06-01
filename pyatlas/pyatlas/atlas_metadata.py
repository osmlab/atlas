import autogen.Tag_pb2


class AtlasMetaData:
    """Atlas metadata"""

    def __init__(self):
        self.number_of_edges = 0
        self.number_of_nodes = 0
        self.number_of_areas = 0
        self.number_of_lines = 0
        self.number_of_points = 0
        self.number_of_relations = 0
        self.original = False
        self.code_version = ""
        self.data_version = ""
        self.country = ""
        self.shard_name = ""
        self.tags = {}


def get_atlas_metadata_from_proto(proto_atlas_metadata):
    """
    Takes a decoded ProtoAtlasMetaData object and turns
    it into a more user friendly AtlasMetaData object.
    """
    new_atlas_metadata = AtlasMetaData()
    new_atlas_metadata.number_of_edges = proto_atlas_metadata.edgeNumber
    new_atlas_metadata.number_of_nodes = proto_atlas_metadata.nodeNumber
    new_atlas_metadata.number_of_areas = proto_atlas_metadata.areaNumber
    new_atlas_metadata.number_of_lines = proto_atlas_metadata.lineNumber
    new_atlas_metadata.number_of_points = proto_atlas_metadata.pointNumber
    new_atlas_metadata.number_of_relations = proto_atlas_metadata.relationNumber
    new_atlas_metadata.original = proto_atlas_metadata.original
    new_atlas_metadata.code_version = proto_atlas_metadata.codeVersion
    new_atlas_metadata.data_version = proto_atlas_metadata.dataVersion
    new_atlas_metadata.country = proto_atlas_metadata.country
    new_atlas_metadata.shard_name = proto_atlas_metadata.shardName

    # convert prototags and fill the tag dict
    for proto_tag in proto_atlas_metadata.tags:
        new_atlas_metadata.tags[proto_tag.key] = proto_tag.value

    return new_atlas_metadata
