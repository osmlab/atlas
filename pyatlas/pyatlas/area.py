import atlas_entity


class Area(atlas_entity.AtlasEntity):
    """
    An Atlas Area. Effectively a Polygon with some tags.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Area. This should not be called directly.
        """
        super(Area, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Area.
        """
        result = '[ '
        result += 'Area: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_polygon())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

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
        Get the frozenset of Relations of which this Area is a member.
        Returns an empty set if this Area is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_areaIndexToRelationIndices(
        )
        return self._get_relations_helper(relation_map, self.index)
