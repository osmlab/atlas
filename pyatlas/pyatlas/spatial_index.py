"""
This module defines the spatial index class for use by the Atlas.
"""

import ctypes
import shapely.geometry
from shapely.geos import lgeos as _lgeos

import geometry


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
        this index, as well as the EntityType of the AtlasEntities it will store.
        Can optionally accept an iterable of AtlasEntities with which to initialize
        the index.

        In order to start using the index, one must specify which backing tree
        implementation to use.
        """
        self.tree = None
        self.atlas = parent_atlas
        self.entity_type = entity_type
        self.initial_entities = initial_entities

    def initialize_rtree(self):
        """
        Initialize an R-tree to back this SpatialIndex. The underlying R-tree
        implementation is immutable, so repeated calls to add() will degrade
        performance. For more information, see the documentation in the _RTree class.
        """
        self.tree = _RTree(self.initial_entities)

    def initialize_quadtree(self):
        """
        Intitialize a quadtree to back this SpatialIndex.
        CURRENTLY UNIMPLEMENTED
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
        Get a frozenset of AtlasEntities that are within or intersecting some bounds.
        Can optionally accept a matching predicate function.
        """
        if self.tree is None:
            raise ValueError('must select a tree implementation before using')

        results = []
        for item_index in self.tree.get(bounds):
            result = self.atlas.entity(item_index, self.entity_type)
            if predicate(result):
                results.append(result)

        return frozenset(results)


class _RTree(object):
    """
    A wrapper class for the _CustomSTRtree implementation, which calls into the
    native GEOS library using machinery from Shapely.
    
    This class stores raw AtlasEntity identifiers without any
    EntityType information. For this reason, users of _RTree should avoid adding
    AtlasEntities with different EntityTypes to the same tree.
    
    Note also that the underlying tree implementation (GEOS R-tree) this class uses
    is immutable. _RTree simulates mutability by maintaining a parallel list of
    elements, and rebuilding the underlying tree on each add. For this reason,
    extensive use of the add() method is not recommended. For more information on
    the immutability of the GEOS R-tree, check out this class in the GEOS codebase:
    https://github.com/OSGeo/geos/blob/master/src/index/strtree/AbstractSTRtree.cpp
    """

    def __init__(self, initial_entities=None):
        """
        Create a new _RTree, optionally with an iterable of initial AtlasEntities.
        """
        self.contents = []
        self.tree = None

        if initial_entities is not None:
            for entity in initial_entities:
                self.contents.append(entity)
            self._construct_tree_from_contents()

    def _construct_tree_from_contents(self):
        """
        Use the tree's contents list (of AtlasEntities) to rebuild the backing
        _CustomSTRtree.
        """
        contents_shapely_format = [
            geometry.boundable_to_shapely_box(entity) for entity in self.contents
        ]

        # pack the arguments in format expected by the _CustomSTRtree
        hacktree_arguments = []
        for entity, cont in zip(self.contents, contents_shapely_format):
            hacktree_arguments.append((entity.get_identifier(), cont))

        self.tree = _CustomSTRtree(hacktree_arguments)

    def add(self, entity):
        """
        Insert an AtlasEntity into the _RTree. The underlying _CustomSTRtree
        (which trivially wraps the GEOS STRtree) is immutable once "built", so
        this method forces a rebuild of the entire tree. The STRtree is "built"
        once any type of query has been made on it.
        """
        self.contents.append(entity)
        self._construct_tree_from_contents()

    def get(self, boundable):
        """
        Given a Boundable object, get a list of the identifiers of all
        AtlasEntities within the bounds of the Boundable.
        """
        if self.tree is not None:
            boundable = geometry.boundable_to_shapely_box(boundable)
            return self.tree.get(boundable)
        else:
            raise ValueError('R-tree is empty')


class _CustomSTRtree(object):
    """
    Hack re-implementation of the shapely STRtree. Changes the behaviour of the
    STRtree to allow for insertion of the entity atlas identifier (type long).
    """

    def __init__(self, items):
        """
        Parameter items is a list of tuples, where each tuple lookes like:
        (entity_id: long, entity_geometry: shapely.geometry.polygon)
        """
        self._n_geoms = len(items)

        self._tree_handle = shapely.geos.lgeos.GEOSSTRtree_create(max(4, len(items)))
        for item in items:
            _lgeos.GEOSSTRtree_insert(self._tree_handle, item[1]._geom,
                                      ctypes.py_object(long(item[0])))

        geoms = [item[1] for item in items]
        self._geoms = list(geoms)

    def __del__(self):
        if self._tree_handle is not None:
            _lgeos.GEOSSTRtree_destroy(self._tree_handle)
            self._tree_handle = None

    def get(self, geom):
        """
        Get a list of identifiers of AtlasEntities whose bounds intersect the
        bounds defined by the geom parameter.
        """
        if self._n_geoms == 0:
            return []

        result = []

        def callback(item, userdata):
            identifier = ctypes.cast(item, ctypes.py_object).value
            result.append(identifier)

        _lgeos.GEOSSTRtree_query(self._tree_handle, geom._geom, _lgeos.GEOSQueryCallback(callback),
                                 None)

        return result
