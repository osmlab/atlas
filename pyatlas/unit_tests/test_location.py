import unittest

from pyatlas import location

class LocationTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_location_packing(self):
        testlocation1 = location.Location(1, -3)
        self.assertEqual(testlocation1, location.get_location_from_packed_int(testlocation1.get_as_packed_int()))


if __name__ == "__main__":
    unittest.main()
