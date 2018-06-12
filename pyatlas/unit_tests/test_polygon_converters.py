import unittest

from pyatlas.location import Location
from pyatlas.polygon import Polygon
from pyatlas import polygon_converters
import shapely.geometry


class PolygonConvertersTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_boundable_to_shapely_box(self):
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        bounds = Polygon(loclist).get_bounds()

        shapely_box = polygon_converters.boundable_to_shapely_box(bounds)
        test_against = shapely.geometry.LineString([(0, 0), (450000000, 0), (450000000, 450000000),
                                                    (0, 450000000)])
        test_against = shapely.geometry.Polygon(test_against)

        self.assertTrue(shapely_box, test_against)

    def test_polygon_to_shapely_polygon(self):
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        polygon = Polygon(loclist)

        shapely_poly = polygon_converters.polygon_to_shapely_polygon(polygon)
        test_against = shapely.geometry.LineString([(0, 0), (400000000, 0), (350000000, 300000000),
                                                    (450000000, 450000000), (1000, 450000000)])
        test_against = shapely.geometry.Polygon(test_against)

        self.assertTrue(shapely_poly, test_against)
