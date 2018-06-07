import atlas_entity


class Relation(atlas_entity.AtlasEntity):
    """
    An Atlas Relation. Aggregates AtlasEntities in a logical relationship.
    Can contain other Relations as members.
    """

    def __init__(self, parent_atlas, index):
        """
        Constuct a new Relation. This should not be called directly.
        """
        super(Relation, self).__init__(parent_atlas)
        self.index = index

    def __str__(self):
        """
        Get a string representation of this Relation.
        """
        result = '[ '
        result += 'Relation: id=' + str(self.get_identifier())

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Relation.
        """
        return self.get_parent_atlas()._get_relationIdentifiers().elements[
            self.index]

    def get_tags(self):
        """
        Get a dictionary of this Relation's tags.
        """
        relation_tag_store = self.get_parent_atlas()._get_relationTags()
        return relation_tag_store.to_key_value_dict(self.index)

    def get_relations(self):
        """
        Get the frozenset of Relations of which this Relation is a member.
        Returns an empty set if this Relation is not a member of any Relations.
        """
        relation_map = self.get_parent_atlas(
        )._get_relationIndexToRelationIndices()
        return self._get_relations_helper(relation_map, self.index)
