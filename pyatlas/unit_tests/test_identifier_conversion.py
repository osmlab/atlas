import unittest

from pyatlas import identifier_conversion


class IdentifierConversionTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_osm_conversion(self):
        atlas_id = 222222000000
        osm_id = 222222
        self.assertEqual(osm_id,
                         identifier_conversion.get_osm_identifier(atlas_id))

        atlas_id = 123001002
        osm_id = 123
        self.assertEqual(osm_id,
                         identifier_conversion.get_osm_identifier(atlas_id))

        atlas_id = 3101220
        osm_id = 3
        self.assertEqual(osm_id,
                         identifier_conversion.get_osm_identifier(atlas_id))

        atlas_id = -222222000001
        osm_id = 222222
        self.assertEqual(osm_id,
                         identifier_conversion.get_osm_identifier(atlas_id))
