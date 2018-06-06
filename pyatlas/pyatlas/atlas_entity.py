import identifier_conversion


class AtlasEntity:
    """
    A tagged, located entity in an Atlas. An AtlasEntity should not be
    instantiated directly. Use one of the appropriate sub-classes.
    """

    def __init__(self):
        """
        AtlasEntity should not be instantiated directly.
        """
        pass

    def get_identifier(self):
        """
        Get the Atlas identifier of this entity.
        """
        raise NotImplementedError('subclass must implement')

    def get_tags(self):
        """
        Get a dictionary of this entity's tags.
        """
        raise NotImplementedError('subclass must implement')

    def get_osm_identifier(self):
        """
        Convenience wrapper for the same function in the identifier_conversion
        module.
        """
        atlas_id = self.get_identifier()
        return identifier_conversion.get_osm_identifier(atlas_id)
