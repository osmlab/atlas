class Edge:
    """
    An Atlas Edge. Logically corresponds to the PackedEdge class from the
    Java implementation.
    """

    def __init__(self, parent_atlas, index):
        self.parent_atlas = parent_atlas
        self.index = index

    def __str__(self):
        result = '['
        result += 'Edge: id=' + str(self.get_identifier())
        result += ', tags=' + str(self.get_tags())
        result += ']'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Edge.
        :return: the Atlas id
        """
        return self.get_parent_atlas()._get_edgeIdentifiers().elements[
            self.index]

    def get_tags(self):
        """
        Get a dictionary of this Edge's tags.
        :return: the dictionary
        """
        edge_tag_store = self.get_parent_atlas()._get_edgeTags()
        return edge_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the set of relations of which this Edge is a member.
        :return: the set of relations
        """
        # TODO implement
        raise NotImplementedError

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this Edge.
        :return: the parent atlas
        """
        return self.parent_atlas
