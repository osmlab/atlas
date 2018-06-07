import atlas_entity
import entity_type
import node
import edge
import area
import line
import point
import relation


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
        for member in self.get_members():
            string += str(member) + ','
        result += ', members=[' + string + ']'

        string = ''
        for relation in self.get_relations():
            string += str(relation.get_identifier()) + ','
        result += ', relations=[' + string + ']'

        result += ', tags=' + str(self.get_tags())
        result += ' ]'
        return result

    def get_identifier(self):
        """
        Get the Atlas identifier of this Relation.
        """
        return self.get_parent_atlas()._get_relationIdentifiers().elements[
            self.index]

    def get_members(self):
        """
        Get a sorted list of this Relation's members. The members are in
        RelationMember form.
        """
        result = []
        relation_identifiers = self.get_parent_atlas(
        )._get_relationIdentifiers()
        relation_member_types = self.get_parent_atlas(
        )._get_relationMemberTypes()
        relation_member_indices = self.get_parent_atlas(
        )._get_relationMemberIndices()
        relation_member_roles = self.get_parent_atlas(
        )._get_relationMemberRoles()
        dictionary = self.get_parent_atlas()._get_dictionary()

        array_index = 0
        # the relationMemberTypes field is a byte array, so the Python treats
        # it as a string. We need to convert it to a true byte array.
        for type_value in bytearray(
                relation_member_types.arrays[self.index].elements):
            member_index = relation_member_indices.arrays[self.index].elements[
                array_index]
            role = dictionary.word(
                relation_member_roles.arrays[self.index].elements[array_index])

            if type_value == entity_type.EntityType.NODE:
                entity = node.Node(self.get_parent_atlas(), member_index)
            elif type_value == entity_type.EntityType.EDGE:
                entity = edge.Edge(self.get_parent_atlas(), member_index)
            elif type_value == entity_type.EntityType.AREA:
                entity = area.Area(self.get_parent_atlas(), member_index)
            elif type_value == entity_type.EntityType.LINE:
                entity = line.Line(self.get_parent_atlas(), member_index)
            elif type_value == entity_type.EntityType.POINT:
                entity = point.Point(self.get_parent_atlas(), member_index)
            elif type_value == entity_type.EntityType.RELATION:
                entity = relation.Relation(self.get_parent_atlas(),
                                           member_index)
            else:
                raise ValueError('invalid EntityType value ' + str(type_value))
            result.append(
                RelationMember(role, entity,
                               relation_identifiers.elements[self.index]))
            array_index += 1

        return sorted(result)

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

    def get_type(self):
        """
        Implement superclass get_type(). Always returns RELATION.
        """
        return entity_type.EntityType.RELATION


class RelationMember(object):
    """
    A container type for Relation members. A RelationMember has a role as well
    as a reference to its AtlasEntity.
    """

    def __init__(self, role, entity, identifier):
        """
        Create a new RelationMember.
        """
        self.role = role
        self.entity = entity
        self.identifier = identifier

    def __lt__(self, other):
        """
        Define an ordering for RelationMembers. Compare EntityTypes, then
        identifiers, then roles.
        """
        if self.entity.get_type() < other.entity.get_type():
            return True
        elif self.entity.get_type() > other.entity.get_type():
            return False
        else:
            if self.identifier < other.identifier:
                return True
            elif self.identifier > other.identifier:
                return False
            else:
                if self.role < other.role:
                    return True
                else:
                    return False

    def __str__(self):
        """
        Get a string representation of this RelationMember.
        """
        result = '[ '
        result += 'id=' + str(self.get_entity().get_identifier())
        result += ', type=' + entity_type.to_str(self.entity.get_type())
        result += ', role=' + str(self.get_role())
        result += ' ]'
        return result

    def get_entity(self):
        """
        Get this RelationMember's AtlasEntity.
        """
        return self.entity

    def get_relation_identifier(self):
        """
        Get the identifier of the Relation of which this RelationMember is a member.
        """
        return self.identifier

    def get_role(self):
        """
        Get the role of this RelationMember.
        """
        return self.role
