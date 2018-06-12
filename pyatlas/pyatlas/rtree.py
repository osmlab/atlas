import shapely.strtree
import shapely.geometry
import ctypes
from shapely.geos import lgeos as _lgeos


class RTree(object):
    """
    A wrapper class for the Shapely STRtree implementation. The underlying
    _CustomSTRtree is immutable, but this class simulates mutability by
    maintaining a parallel list of elements, and rebuilding the STRtree on each
    add. For this reason, extensive use of the add() method is not recommended.

    TODO make the _CustomSTRtree implementation mutable

    Note also, this class stores raw AtlasEntity identifiers without any
    EntityType information. For this reason, users of RTree should avoid adding
    AtlasEntities with different EntityTypes in the same tree.
    """

    def __init__(self, initial_entities=None):
        """
        Create a new RTree, optionally with an iterable of initial AtlasEntities.
        """
        self.contents = []
        self.tree = None

        if initial_entities is not None:
            for entity in initial_entities:
                self.contents.append(entity)
            self._construct_tree_from_contents()

    def _construct_tree_from_contents(self):
        contents_shapely_format = [_convert_to_shapely(entity) for entity in self.contents]

        # pack the arguments in format expected by the _HackSTRtree
        hacktree_arguments = []
        for entity, cont in zip(self.contents, contents_shapely_format):
            hacktree_arguments.append((entity.get_identifier(), cont))

        self.tree = _CustomSTRtree(hacktree_arguments)

    def add(self, entity):
        """
        Insert an AtlasEntity into the RTree. The underlying STRtree is
        immutable, so this method forces a rebuild of the entire tree.
        """
        self.contents.append(entity)
        self._construct_tree_from_contents()

    def get(self, boundable):
        """
        Given a Boundable object, get a list of the identifiers of all
        AtlasEntities within the bounds of the Boundable.
        """
        if self.tree is not None:
            boundable = _convert_to_shapely(boundable)
            return self.tree.get(boundable)
        else:
            raise ValueError('RTree is empty')


class _CustomSTRtree(object):
    """
    Hack re-implementation of the shapely STRtree. Changes the behaviour of the
    strtree to allow for insertion of the entity atlas identifier (type long).
    """

    def __init__(self, items):
        """
        Parameter items is a list of tuples, where each tuple lookes like:
        (entity_id: long, entity_geometry: shapely.geometry.polygon)
        """
        self._n_geoms = len(items)

        self._tree_handle = shapely.geos.lgeos.GEOSSTRtree_create(max(2, len(items)))
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


def _convert_to_shapely(boundable):
    # convert a pyatlas Boundable type to its Shapely Polygon representation
    bounds = boundable.get_bounds()
    lower_left = bounds.get_lower_left()
    upper_right = bounds.get_upper_right()
    return shapely.geometry.box(
        lower_left.get_longitude(),
        lower_left.get_latitude(),
        upper_right.get_longitude(),
        upper_right.get_latitude(),
        ccw=False)
