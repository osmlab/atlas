import shapely.strtree
import shapely.geometry
from shapely.geos import lgeos
import ctypes

# TODO this class does not handle any edgecases, ie. initial_boundables is empty, etc.


class RTree(object):
    """
    A wrapper class for the Shapely STRtree implementation. The underlying
    STRtree is immutable, but this class simulates mutability by maintaining a
    parallel list of elements, and rebuilding the STRtree on each add. For this
    reason, extensive use of the add() method is not recommended.

    Note also, this class stores raw AtlasEntity identifiers without any
    EntityType information. For this reason, users of RTree should avoid adding
    AtlasEntities with different EntityTypes in the same tree.
    """

    def __init__(self, initial_boundables):
        """
        Create a new RTree with a list of initial items.
        """
        self.contents = []
        for boundable in initial_boundables:
            self.contents.append(boundable)
        self.contents_shapely_format = [
            _convert_to_shapely(boundable) for boundable in self.contents
        ]

        # pack the arguments in format expected by the _HackSTRtree
        self.hacktree_arguments = []
        for boundable, cont in zip(self.contents, self.contents_shapely_format):
            self.hacktree_arguments.append((boundable.get_identifier(), cont))

        self.internal_tree = _HackSTRtree(self.hacktree_arguments)

    def add(self, bounds, identifier):
        """
        Insert an AtlasEntity with a given identifier into the RTree with a
        spatial extent given by the bounds parameter.
        """
        # TODO implement
        raise NotImplementedError

    def get(self, boundable):
        """
        Given a Boundable object, get the identifiers of all AtlasEntities within
        the bounds of the Boundable.
        """
        boundable = _convert_to_shapely(boundable)
        return self.internal_tree.query(boundable)


class _HackSTRtree(shapely.strtree.STRtree):
    """
    Hack subclass of the shapely STRtree. Overrides the behaviour of strtree to
    allow for insertion of the entity atlas identifier (of type long).
    """

    def __init__(self, items):
        """
        Parameter items is a list of tuples, where each tuple lookes like:
        (entity_id: long, entity_geometry: shapely.geometry.polygon)
        """
        self._n_geoms = len(items)

        self._tree_handle = shapely.geos.lgeos.GEOSSTRtree_create(max(2, len(items)))
        for item in items:
            lgeos.GEOSSTRtree_insert(self._tree_handle, item[1]._geom,
                                     ctypes.py_object(long(item[0])))

        geoms = [item[1] for item in items]
        self._geoms = list(geoms)

    def query(self, geom):
        """
        A query with a given geometry returns an identifier. This identifier is
        then used by the pyatlas RTree to look up the entity using the atlas
        idToArrayIndex maps.

        """
        if self._n_geoms == 0:
            return []

        result = []

        def callback(item, userdata):
            identifier = ctypes.cast(item, ctypes.py_object).value
            result.append(identifier)

        lgeos.GEOSSTRtree_query(self._tree_handle, geom._geom, lgeos.GEOSQueryCallback(callback),
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
