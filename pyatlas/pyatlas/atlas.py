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
        self.pointIdentifierToPointArrayIndex = None
        self.pointLocations = None
        self.dictionary = None
        self.pointTags = None

        if not self.lazy_loading:
            self.load_all_fields()

    def get_metadata(self):
        """
        Get the metadata associated with this Atlas. See class AtlasMetaData
        for more information.
        :return: the metadata
        """
        if self.metaData is None:
            self.serializer.load_field(self.serializer._FIELD_METADATA)
        return self.metaData

    def points(self):
        """
        Get a generator for all the points in this Atlas
        :return: the Atlas point generator
        """
        for i, element in enumerate(self._get_pointIdentifiers().elements):
            yield Point(self, i)

    def point(self, identifier):
        """
        Get a point with a given Atlas identifier. Returns None if there is no
        point with the given identifier.
        :param identifier: the identifier
        :return: the point with this identifier, None if it does not exist
        """
        identifier_to_index = self._get_pointIdentifierToPointArrayIndex()
        if identifier in identifier_to_index:
            return Point(self, identifier_to_index[identifier])
        return None

    def load_all_fields(self):
        """
        Force this Atlas to load all its fields from its backing store.
        """
        self.serializer.load_all_fields()

    def _get_pointIdentifiers(self):
        if self.pointIdentifiers is None:
            self.serializer.load_field(
                self.serializer._FIELD_POINT_IDENTIFIERS)
        return self.pointIdentifiers

    def _get_pointIdentifierToPointArrayIndex(self):
        if self.pointIdentifierToPointArrayIndex is None:
            self.serializer.load_field(
                self.serializer._FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
        return self.pointIdentifierToPointArrayIndex

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
