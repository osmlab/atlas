import atlas_entity
import entity_type


class Line(atlas_entity.AtlasEntity):
    """
    An Atlas Line. Effectively a PolyLine with some tags.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Line. This should not be called directly.
        """
        super(Line, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Line.
        """
        result = '[ '
        result += 'Line: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_polyline())
        result += ', tags=' + str(self.get_tags())

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def as_polyline(self):
        """
        Get the PolyLine geometry of this Line.
        """
        return self.get_parent_atlas()._get_linePolyLines()[self.index]

    def get_identifier(self):
        """
        Get the Atlas identifier of this Line.
        """
        return self.get_parent_atlas()._get_lineIdentifiers().elements[self.index]

    def get_tags(self):
        """
        Get a dictionary of this Line's tags.
        """
        line_tag_store = self.get_parent_atlas()._get_lineTags()
        return line_tag_store.to_key_value_dict(self.index)

    def get_bounds(self):
        """
        Get the bounding Rectangle of this Line.
        """
        return self.as_polyline().get_bounds()

    def get_relations(self):
        """
        Get the frozenset of Relations of which this Line is a member.
        Returns an empty set if this Line is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas()._get_lineIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)

    def get_type(self):
        """
        Implement superclass get_type(). Always returns EntityType.LINE.
        """
        return entity_type.EntityType.LINE
