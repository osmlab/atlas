import atlas_entity


class Line(atlas_entity.AtlasEntity):
    """
    An Atlas Line.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Line. This should not be called directly.
        """
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        """
        Transform this Line into its string representation.
        """
        result = '[ '
        result += 'Line: id=' + str(self.get_identifier())
        result += ', geom=' + str(self.as_polyline())
        result += ', tags=' + str(self.get_tags())
        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Line.
        """
        return self.get_parent_atlas()._get_lineIdentifiers().elements[
            self.index]

    def as_polyline(self):
        """
        Get the PolyLine geometry of this Line.
        """
        return self.get_parent_atlas()._get_linePolyLines()[self.index]

    def get_tags(self):
        """
        Get a dictionary of this Line's tags.
        """
        line_tag_store = self.get_parent_atlas()._get_lineTags()
        return line_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the set of relations of which this Line is a member.
        """
        # TODO implement
        raise NotImplementedError

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this Line.
        """
        return self.parent_atlas
