import identifier_conversion


class AtlasEntity(object):
    """
    A tagged, located entity in an Atlas. An AtlasEntity should not be
    instantiated directly. Use one of the appropriate sub-classes.
    """

    def __init__(self, parent_atlas):
        """
        AtlasEntity should not be instantiated directly.
        """
        self.parent_atlas = parent_atlas

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

    def get_relations(self):
        """
        Get the set of Relations of which this AtlasEntity is a member.
        """
        raise NotImplementedError('subclass must implement')

    def get_osm_identifier(self):
        """
        Convenience wrapper for the same function in the identifier_conversion
        module.
        """
        atlas_id = self.get_identifier()
        return identifier_conversion.get_osm_identifier(atlas_id)

    def get_parent_atlas(self):
        """
        Get the Atlas that contains this AtlasEntity.
        """
        return self.parent_atlas

    def _get_relations_helper(self, relation_map, index):
        # subclasses of AtlasEntity can use this helper function to
        # avoid code duplication in their get_relations() implementations
        import relation
        relation_set = set()

        if index not in relation_map:
            return relation_set

        for relation_index in relation_map[index]:
            relation0 = relation.Relation(self.get_parent_atlas(),
                                          relation_index)
            relation_set.add(relation0)

        return frozenset(relation_set)
