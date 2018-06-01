import unittest

from pyatlas import location

class LocationTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_location_packing(self):
        testlocation1 = location.Location(1, 1)
        self.assertEqual(testlocation1, location.get_location_from_packed_int(testlocation1.get_as_packed_int()))

        testlocation2 = location.Location(1, -3)
        self.assertEqual(testlocation2, location.get_location_from_packed_int(testlocation2.get_as_packed_int()))

        testlocation3 = location.Location(-3, -3)
        self.assertEqual(testlocation3, location.get_location_from_packed_int(testlocation3.get_as_packed_int()))

        testlocation4 = location.Location(-900000000, 450000000)
        self.assertEqual(testlocation4, location.get_location_from_packed_int(testlocation4.get_as_packed_int()))

        testlocation5 = location.Location(900000000, -1800000000)
        self.assertEqual(testlocation5, location.get_location_from_packed_int(testlocation5.get_as_packed_int()))

        testlocation6 = location.Location(900000000, 1800000000 - 1)
        self.assertEqual(testlocation6, location.get_location_from_packed_int(testlocation6.get_as_packed_int()))

if __name__ == "__main__":
    unittest.main()
