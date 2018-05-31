import unittest

from pyatlas import location

class LocationTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_location_packing(self):
        testlocation1 = location.Location(1, 2)
        self.assertEqual(testlocation1, testlocation1)
        testlocation1_packed = testlocation1.get_as_packed_int()
        testlocation1_match = location.get_location_from_packed_int(testlocation1_packed)
        self.assertEqual(testlocation1, testlocation1_match)


if __name__ == "__main__":
    unittest.main()
