import location


class Node:
    """
    An Atlas Node. Logically corresponds to the PackedNode class from the
    Java implementation.
    """

    def __init__(self, parent_atlas, index):
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        result = '['
        result += 'Node: id=' + str(self.get_identifier())
        result += ', location_latlon=' + str(self.get_location())
        result += ', tags=' + str(self.get_tags())
        result += ']'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this node.
        :return: the Atlas id
        """
        return self.get_parent_atlas()._get_nodeIdentifiers().elements[
            self.index]

    def get_location(self):
        """
        Get the Location of this Node. Check the Location class for more
        information.
        :return: the location
        """
        long_location = self.get_parent_atlas()._get_nodeLocations().elements[
            self.index]
        return location.get_location_from_packed_int(long_location)

    def get_tags(self):
        """
        Get a dictionary of this Node's tags.
        :return: the dictionary
        """
        node_tag_store = self.get_parent_atlas()._get_nodeTags()
        return node_tag_store.to_key_value_dict(self.index)

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this node.
        :return: the parent atlas
        """
        return self.parent_atlas
