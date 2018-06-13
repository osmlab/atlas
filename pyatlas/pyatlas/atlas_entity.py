import identifier_converters
import boundable


class AtlasEntity(boundable.Boundable):
    """
    A tagged, located entity in an Atlas. An AtlasEntity should not be
    instantiated directly. Use one of the appropriate sub-classes.
    """

    # FIXME this class overrides __eq__ and __ne__, but not __hash__.

    def __init__(self, parent_atlas):
        """
        AtlasEntity should not be instantiated directly.
        """
        self.parent_atlas = parent_atlas

    def __eq__(self, other):
        """
        Determine if this AtlasEntity is equal to another. Two entities are
        considered equal if they have the same identifier and the same type.
        """
        return self.get_identifier() == other.get_identifier() and self.get_type(
        ) == other.get_type()

    def __ne__(self, other):
        """
        Determine if this AtlasEntity is NOT equal to another. Inverse of the
        comparison made by the __eq__() method.
        """
        return not self.__eq__(other)

    def __hash__(self):
        """
        Compute a hashcode for this AtlasEntity.
        """
        return self.get_identifier() * 31 + self.get_type()

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

    def get_bounds(self):
        """
        Get the bounding Rectangle of this AtlasEntity.
        """
        raise NotImplementedError('subclass must implement')

    def get_relations(self):
        """
        Get the set of Relations of which this AtlasEntity is a member.
        """
        raise NotImplementedError('subclass must implement')

    def get_type(self):
        """
        Get the EntityType of this AtlasEntity
        """
        raise NotImplementedError('subclass must implement')

    def get_osm_identifier(self):
        """
        Convenience wrapper for the same function in the identifier_conversion
        module.
        """
        atlas_id = self.get_identifier()
        return identifier_converters.get_osm_identifier(atlas_id)

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
            relation0 = relation.Relation(self.get_parent_atlas(), relation_index)
            relation_set.add(relation0)

        return frozenset(relation_set)
