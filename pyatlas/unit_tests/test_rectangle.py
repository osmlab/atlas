import unittest

from pyatlas.location import Location
from pyatlas import rectangle
from pyatlas.rectangle import Rectangle


class RectangleTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_rectangle_construction(self):
        rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        loop = []
        for point in rect.closed_loop():
            loop.append(point)
        # first and last points are the same
        self.assertEqual(loop[0], loop[len(loop) - 1])
        # test consistency
        self.assertEqual(loop[0], Location(0, 0))
        self.assertEqual(loop[1], Location(450000000, 0))
        self.assertEqual(loop[2], Location(450000000, 450000000))
        self.assertEqual(loop[3], Location(0, 450000000))
        self.assertEqual(loop[4], Location(0, 0))

    def test_bounds_for_locations(self):
        loclist = [
            Location(0, 0),
            Location(450000000, 0),
            Location(450000000, 450000000),
            Location(0, 450000000)
        ]
        rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        rect2 = rectangle.bounds_locations(loclist)
        self.assertEqual(rect, rect2)


if __name__ == "__main__":
    unittest.main()
