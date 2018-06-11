import rtree


class SpatialIndex(object):
    """
    An optimized container class for making spatial queries on AtlasEntities.
    The Atlas will automatically construct the SpatialIndices it needs as it
    receives queries, so it is unlikely you will ever need to create instances
    of this class manually.
    """

    def __init__(self, parent_atlas, entity_type, initial_boundables=None):
        """
        Create a new SpatialIndex. Requires a reference to the parent Atlas of
        this index, as well as the EntityType of the entities it will store. Can
        optionally accept a list of Boundables to initialize the tree with.

        In order to start using the tree, one must specify which backing
        implementation to use.
        """
        # TODO have a required enum parameter that forces the user to choose a backing tree
        self.tree = None
        self.atlas = parent_atlas
        self.entity_type = entity_type
        self.initial_boundables = initial_boundables

    def use_rtree(self):
        self.tree = rtree.RTree(self.initial_boundables)

    def add(self, entity):
        """
        Insert an AtlasEntity into the index.
        """
        bounds = entity.bounds()
        if bounds is not None:
            self.tree.add(bounds, entity.get_identifier())
        else:
            raise ValueError('bounds cannot be None')

    def get(self, bound, predicate=lambda e: True):
        """
        Get a list of the features that are within or intersecting some bounds.
        Can optionally accept a matching predicate function.
        """
        results = []
        for item_index in self.tree.get(bound):
            result = self.atlas.entity(item_index, self.entity_type)
            if predicate(result):
                results.append(result)
