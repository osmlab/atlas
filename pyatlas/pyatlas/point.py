import location


class Point:
    """
    An Atlas Point. Logically corresponds to the PackedPoint class from the
    Java implementation. This is a flyweight class, and does not contain any
    actual data. It simply references into the data arrays of the parent Atlas.
    """

    def __init__(self, parent_atlas, index):
        self.parent_atlas = parent_atlas
        self.index = index

    def get_identifier(self):
        return self.get_parent_atlas()._get_pointIdentifiers().elements[
            self.index]

    def get_location(self):
        long_location = self.get_parent_atlas()._get_pointLocations().elements[
            self.index]
        return location.get_location_from_packed_int(long_location)

    def get_tags(self):
        raise NotImplementedError

    def get_relations(self):
        raise NotImplementedError

    def get_parent_atlas(self):
        return self.parent_atlas
