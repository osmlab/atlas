import location
import atlas_entity
import entity_type


class Point(atlas_entity.AtlasEntity):
    """
    An Atlas Point. Effectively a simple Location with some tags.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Point. This should not be called directly.
        """
        super(Point, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Point.
        """
        result = '[ '
        result += 'Point: id=' + str(self.get_identifier())
        result += ', location_latlon=' + str(self.get_location())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this point.
        """
        return self.get_parent_atlas()._get_pointIdentifiers().elements[
            self.index]

    def get_location(self):
        """
        Get the Location of this point. Check the Location class for more
        information.
        """
        long_location = self.get_parent_atlas()._get_pointLocations().elements[
            self.index]
        return location.get_location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this Point's tags.
        """
        point_tag_store = self.get_parent_atlas()._get_pointTags()
        return point_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the frozenset of Relations of which this Point is a member.
        Returns an empty set if this Point is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas(
        )._get_pointIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Implement superclass get_type(). Always returns POINT.
        """
        return entity_type.EntityType.POINT
