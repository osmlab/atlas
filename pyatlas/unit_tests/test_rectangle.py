import unittest

from pyatlas.atlas import Atlas
from pyatlas import geometry
from pyatlas.geometry import Location, Rectangle


class RectangleTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_rectangle_construction(self):
        rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        loop = []
        for point in rect.closed_loop():
            loop.append(point)
        # first and last points should be the same
        self.assertEqual(loop[0], loop[len(loop) - 1])
        # test consistency
        self.assertEqual(loop[0], Location(0, 0))
        self.assertEqual(loop[1], Location(450000000, 0))
        self.assertEqual(loop[2], Location(450000000, 450000000))
        self.assertEqual(loop[3], Location(0, 450000000))
        self.assertEqual(loop[4], Location(0, 0))

    def test_location_bounding_calculation(self):
        loclist = [
            Location(0, 0),
            Location(450000000, 0),
            Location(450000000, 450000000),
            Location(0, 450000000)
        ]
        expected_rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        computed_rect = geometry.bounds_locations(loclist)
        self.assertEqual(expected_rect, computed_rect)

        # create a lopsided polygon to test bounding box
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        expected_rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        computed_rect = geometry.bounds_locations(loclist)
        self.assertEqual(expected_rect, computed_rect)

    def test_entity_bounding_calculation_on_relations(self):
        atlas = Atlas("resources/test.atlas")

        relation = atlas.relation(1)
        expected_rect = Rectangle(
            Location(390000000, -1190300000), Location(390500000, -1180000000))
        computed_rect = geometry.bounds_atlasentities([relation])
        self.assertEqual(expected_rect, computed_rect)

        relation = atlas.relation(2)
        expected_rect = Rectangle(
            Location(380000000, -1180100000), Location(380100000, -1180000000))
        computed_rect = geometry.bounds_atlasentities([relation])
        self.assertEqual(expected_rect, computed_rect)


if __name__ == "__main__":
    unittest.main()
