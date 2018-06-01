"""The Atlas class definition"""

from atlas_serializer import AtlasSerializer
from point import Point


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
        self.pointIdentifiers = None
        self.pointLocations = None
        self.dictionary = None
        self.pointTags = None

        if not self.lazy_loading:
            self.load_all_fields()

    def get_metadata(self):
        if self.metaData is None:
            self.serializer.load_field(self.serializer._FIELD_METADATA)
        return self.metaData

    def points(self):
        """
        Get a generator for all the Points in this Atlas
        :return: the Atlas Point generator
        """
        i = 0
        while i < len(self._get_pointIdentifiers().elements):
            yield Point(self, i)
            i += 1

    def point(self, identifier):
        raise NotImplementedError

    def load_all_fields(self):
        self.serializer.load_all_fields()

    def _get_pointIdentifiers(self):
        if self.pointIdentifiers is None:
            self.serializer.load_field(
                self.serializer._FIELD_POINT_IDENTIFIERS)
        return self.pointIdentifiers

    def _get_pointLocations(self):
        if self.pointLocations is None:
            self.serializer.load_field(self.serializer._FIELD_POINT_LOCATIONS)
        return self.pointLocations

    def _get_dictionary(self):
        if self.dictionary is None:
            self.serializer.load_field(self.serializer._FIELD_DICTIONARY)
        return self.dictionary

    def _get_pointTags(self):
        if self.pointTags is None:
            self.serializer.load_field(self.serializer._FIELD_POINT_TAGS)
        self.pointTags.set_dictionary(self._get_dictionary())
        return self.pointTags
