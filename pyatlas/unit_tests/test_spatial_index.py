import unittest

from pyatlas.rtree import RTree
from pyatlas import location
from pyatlas.rectangle import Rectangle
from pyatlas.atlas import Atlas


class SpatialIndexTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_rtree(self):
        # The bounding box defined in this test should only encompass
        # test Points 1, 2, and 3 from the test atlas

        atlas = Atlas("resources/test.atlas")
        tree = RTree(atlas.points())

        lower_left = location.with_degrees(37, -118.02)
        upper_right = location.with_degrees(39, -118)

        test_results = []
        for element in tree.get(Rectangle(lower_left, upper_right)):
            test_results.append(element)

        self.assertEquals([1, 2, 3], test_results)
