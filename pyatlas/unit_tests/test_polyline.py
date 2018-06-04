import unittest

from pyatlas import polyline
from pyatlas.location import Location

class PolyLineTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_polyline_compression(self):
        loclist = [Location(1, 1), Location(2, 2), Location(5, 5)]
        correct_polyline = polyline.PolyLine(loclist, deep=True)
        test_polyline = polyline.decompress_polyline(correct_polyline.compress())
        self.assertEqual(correct_polyline, test_polyline)

        loclist = [Location(382117269, -1193153616),
                   Location(382117927, -1193152951),
                   Location(382116912, -1193151049),
                   Location(382116546, -1193151382),
                   Location(382116134, -1193150734),
                   Location(382115440, -1193151494)]
        correct_polyline = polyline.PolyLine(loclist, deep=True)
        test_polyline = polyline.decompress_polyline(correct_polyline.compress())
        self.assertEqual(correct_polyline, test_polyline)



if __name__ == "__main__":
    unittest.main()
