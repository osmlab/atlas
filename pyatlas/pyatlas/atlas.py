"""The Atlas class definition"""

from atlas_serializer import AtlasSerializer


class Atlas:
    """
    Atlas class - the current implementation is not threadsafe.

    Note that the member field names do not follow PEP 8 naming conventions.
    This is so they match up with the name of their corresponding ZIP entry.
    These ZIP entry names come from from the PackedAtlas Java implementation.
    """

    def __init__(self, atlas_file, lazy_loading=True):
        self.serializer = AtlasSerializer(atlas_file, self)
        self.lazy_loading = lazy_loading
        self.metaData = None

        if not self.lazy_loading:
            self.load_all_fields()

    def get_metadata(self):
        if self.metaData == None:
            self.serializer.load_field(self.serializer._FIELD_METADATA)
        return self.metaData

    def load_all_fields(self):
        self.serializer.load_all_fields()
