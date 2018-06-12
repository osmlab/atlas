import unittest

from pyatlas import identifier_converters


class IdentifierConvertersTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_osm_conversion(self):
        atlas_id = 222222000000
        osm_id = 222222
        self.assertEqual(osm_id, identifier_converters.get_osm_identifier(atlas_id))

        atlas_id = 123001002
        osm_id = 123
        self.assertEqual(osm_id, identifier_converters.get_osm_identifier(atlas_id))

        atlas_id = 3101220
        osm_id = 3
        self.assertEqual(osm_id, identifier_converters.get_osm_identifier(atlas_id))

        atlas_id = -222222000001
        osm_id = 222222
        self.assertEqual(osm_id, identifier_converters.get_osm_identifier(atlas_id))

    def test_country_code_conversion(self):
        atlas_id = 222222000000
        country_code = 0
        self.assertEqual(country_code, identifier_converters.get_country_code(atlas_id))

        atlas_id = 123001002
        country_code = 1
        self.assertEqual(country_code, identifier_converters.get_country_code(atlas_id))

        atlas_id = 3101220
        country_code = 101
        self.assertEqual(country_code, identifier_converters.get_country_code(atlas_id))

        atlas_id = -222222002001
        country_code = 2
        self.assertEqual(country_code, identifier_converters.get_country_code(atlas_id))

    def test_way_section_conversion(self):
        atlas_id = 222222000000
        way_section = 0
        self.assertEqual(way_section, identifier_converters.get_way_section_index(atlas_id))

        atlas_id = 123001002
        way_section = 2
        self.assertEqual(way_section, identifier_converters.get_way_section_index(atlas_id))

        atlas_id = 3101220
        way_section = 220
        self.assertEqual(way_section, identifier_converters.get_way_section_index(atlas_id))

        atlas_id = -222222002001
        way_section = 1
        self.assertEqual(way_section, identifier_converters.get_way_section_index(atlas_id))
