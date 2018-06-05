import location


class Point:
    """
    An Atlas Point.
    """

    def __init__(self, parent_atlas, index):
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        result = '['
        result += 'Point: id=' + str(self.get_identifier())
        result += ', location_latlon=' + str(self.get_location())
        result += ', tags=' + str(self.get_tags())
        result += ']'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this point.
        :return: the Atlas id
        """
        return self.get_parent_atlas()._get_pointIdentifiers().elements[
            self.index]

    def get_location(self):
        """
        Get the Location of this point. Check the Location class for more
        information.
        :return: the location
        """
        long_location = self.get_parent_atlas()._get_pointLocations().elements[
            self.index]
        return location.get_location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this point's tags.
        :return: the dictionary
        """
        point_tag_store = self.get_parent_atlas()._get_pointTags()
        return point_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the set of relations of which this point is a member.
        :return: the set of relations
        """
        # TODO implement
        raise NotImplementedError

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this point.
        :return: the parent atlas
        """
        return self.parent_atlas
