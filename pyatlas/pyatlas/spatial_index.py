import rtree


class SpatialIndex(object):
    """
    An optimized container class for making spatial queries on AtlasEntities.
    The Atlas will automatically construct the SpatialIndices it needs as it
    receives queries, so it is unlikely you will ever need to create instances
    of this class manually.
    """

    def __init__(self, parent_atlas, entity_type, initial_entities=None):
        """
        Create a new SpatialIndex. Requires a reference to the parent Atlas of
        this index, as well as the EntityType of the entities it will store. Can
        optionally accept an iterable of AtlasEntities with which to initialize
        the index.

        In order to start using the tree, one must specify which backing
        implementation to use.
        """
        self.tree = None
        self.atlas = parent_atlas
        self.entity_type = entity_type
        self.initial_entities = initial_entities

    def initialize_rtree(self):
        """
        Initialize an rtree to back this SpatialIndex.
        """
        self.tree = rtree.RTree(self.initial_entities)

    def initialize_quadtree(self):
        """
        Intitialize a quadtree to back this SpatialIndex.
        """
        # TODO implement the quadtree
        raise NotImplementedError('quadtree currently not implemented')

    def add(self, entity):
        """
        Insert an AtlasEntity into the index.
        """
        if self.tree is None:
            raise ValueError('must select a tree implementation before using')

        if entity.bounds() is not None:
            self.tree.add(entity)
        else:
            raise ValueError('bounds cannot be None')

    def get(self, bounds, predicate=lambda e: True):
        """
        Get a list of AtlasEntities that are within or intersecting some bounds.
        Can optionally accept a matching predicate function.
        """
        if self.tree is None:
            raise ValueError('must select a tree implementation before using')

        results = []
        for item_index in self.tree.get(bounds):
            result = self.atlas.entity(item_index, self.entity_type)
            if predicate(result):
                results.append(result)

        return results
