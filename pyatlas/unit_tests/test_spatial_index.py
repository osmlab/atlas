import unittest

from pyatlas import geometry
from pyatlas.geometry import Rectangle
from pyatlas.atlas import Atlas
from pyatlas.spatial_index import SpatialIndex
from pyatlas.spatial_index import _RTree
from pyatlas.atlas_entities import EntityType


class SpatialIndexTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_rtree(self):
        # The bounding box defined in this test should only encompass
        # test Points 1, 2, and 3 from the test atlas

        atlas = Atlas("resources/test.atlas")
        tree = _RTree(atlas.points())

        lower_left = geometry.location_with_degrees(37, -118.02)
        upper_right = geometry.location_with_degrees(39, -118)

        test_results = []
        for element in tree.get(Rectangle(lower_left, upper_right)):
            test_results.append(element)

        self.assertEquals({1, 2, 3}, set(test_results))

    def test_basic_spatial_index_operations(self):
        atlas = Atlas("resources/test.atlas")

        index = SpatialIndex(atlas, EntityType.POINT, atlas.points())
        index.initialize_rtree()

        lower_left = geometry.location_with_degrees(37, -118.02)
        upper_right = geometry.location_with_degrees(39, -118)

        test_results = index.get(Rectangle(lower_left, upper_right))
        self.assertEquals({atlas.point(2), atlas.point(3), atlas.point(1)}, test_results)

        test_results = index.get(
            Rectangle(lower_left, upper_right), lambda p: p.get_identifier() == 2)
        self.assertEquals({atlas.point(2)}, test_results)
