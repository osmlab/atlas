import identifier_conversion


class AtlasEntity:
    """
    A tagged, located entity in an Atlas.
    """

    def __init__(self):
        pass

    def get_identifier(self):
        raise NotImplementedError('subclass must implement')

    def get_tags(self):
        raise NotImplementedError('subclass must implement')

    def get_osm_identifier(self):
        """
        Get the OSM identifier from the full Atlas identifier by removing the
        country code and way sectioned identifiers.

        Example:
        Atlas ID: 222222001003 would return OSM ID: 222222
        """
        atlas_id = self.get_identifier()
        return identifier_conversion.get_osm_identifier(atlas_id)
