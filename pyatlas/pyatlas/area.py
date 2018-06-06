import location
import atlas_entity


class Area(atlas_entity.AtlasEntity):
    """
    An Atlas Area.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Area. This should not be called directly.
        """
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        """
        Transform this Area into its string representation.
        """
        result = '[ '
        result += 'Area: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_polygon())
        result += ', tags=' + str(self.get_tags())
        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Area.
        """
        return self.get_parent_atlas()._get_areaIdentifiers().elements[
            self.index]

    def as_polygon(self):
        """
        Get the Polygon geometry of this Area.
        """
        return self.get_parent_atlas()._get_areaPolygons()[self.index]

    def get_tags(self):
        """
        Get a dictionary of this Area's tags.
        """
        area_tag_store = self.get_parent_atlas()._get_areaTags()
        return area_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the set of relations of which this Area is a member.
        """
        # TODO implement
        raise NotImplementedError

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this Area.
        """
        return self.parent_atlas
