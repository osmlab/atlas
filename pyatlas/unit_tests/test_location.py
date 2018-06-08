import unittest

from pyatlas import location
from pyatlas import rectangle


class LocationTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_location_packing(self):
        testlocation = location.Location(1, 1)
        self.assertEqual(testlocation,
                         location.get_location_from_packed_int(testlocation.get_as_packed_int()))

        testlocation = location.Location(1, -3)
        self.assertEqual(testlocation,
                         location.get_location_from_packed_int(testlocation.get_as_packed_int()))

        testlocation = location.Location(-3, -3)
        self.assertEqual(testlocation,
                         location.get_location_from_packed_int(testlocation.get_as_packed_int()))

        testlocation = location.Location(-900000000, 450000000)
        self.assertEqual(testlocation,
                         location.get_location_from_packed_int(testlocation.get_as_packed_int()))

        testlocation = location.Location(900000000, -1800000000)
        self.assertEqual(testlocation,
                         location.get_location_from_packed_int(testlocation.get_as_packed_int()))

        testlocation = location.Location(900000000, 1800000000 - 1)
        self.assertEqual(testlocation,
                         location.get_location_from_packed_int(testlocation.get_as_packed_int()))

    def test_location_conversion(self):
        loc_deg = 45.0
        loc_dm7 = 450000000
        self.assertEqual(loc_deg, location.dm7_as_degree(loc_dm7))
        self.assertEqual(loc_dm7, location.degree_as_dm7(loc_deg))

        loc_deg = 90
        loc_dm7 = 900000000
        self.assertEqual(loc_deg, location.dm7_as_degree(loc_dm7))
        self.assertEqual(loc_dm7, location.degree_as_dm7(loc_deg))

        loc_deg = -30
        loc_dm7 = -300000000
        self.assertEqual(loc_deg, location.dm7_as_degree(loc_dm7))
        self.assertEqual(loc_dm7, location.degree_as_dm7(loc_deg))

        loc_deg = -180
        loc_dm7 = -1800000000
        self.assertEqual(loc_deg, location.dm7_as_degree(loc_dm7))
        self.assertEqual(loc_dm7, location.degree_as_dm7(loc_deg))

        loc_deg = 179.9999
        loc_dm7 = 1799999000
        self.assertEqual(loc_deg, location.dm7_as_degree(loc_dm7))
        self.assertEqual(loc_dm7, location.degree_as_dm7(loc_deg))

    def test_location_bounds(self):
        testlocation = location.Location(450000000, 450000000)
        testlocationbounds = rectangle.Rectangle(testlocation, testlocation)
        bound = testlocation.get_bounds()
        self.assertEqual(bound, testlocationbounds)


if __name__ == "__main__":
    unittest.main()
