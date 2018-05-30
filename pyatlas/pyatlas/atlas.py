"""The Atlas class definition"""

from atlas_serializer import AtlasSerializer

class Atlas:
    """Atlas class, NOT THREADSAFE"""
    def __init__(self, atlas_file, lazy_loading=True):
        self.serializer = AtlasSerializer(atlas_file, self)
        self.lazy_loading = lazy_loading
        self.metaData = None

        if not self.lazy_loading:
            self.load_all_fields()

    def get_meta_data(self):
        if self.metaData == None:
            self.serializer.load_field('metaData')
        return self.metaData

    def load_all_fields(self):
        self.serializer.load_all_fields()
