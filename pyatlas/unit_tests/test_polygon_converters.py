import unittest

from pyatlas import geometry
from pyatlas.geometry import Location
from pyatlas.geometry import Polygon
from pyatlas.geometry import PolyLine
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
        bounds = Polygon(loclist).bounds()

        shapely_box = geometry.boundable_to_shapely_box(bounds)
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

        shapely_poly = geometry.polygon_to_shapely_polygon(polygon)
        test_against = shapely.geometry.LineString([(0, 0), (400000000, 0), (350000000, 300000000),
                                                    (450000000, 450000000), (1000, 450000000)])
        test_against = shapely.geometry.Polygon(test_against)

        self.assertTrue(shapely_poly, test_against)

    def test_location_to_shapely_point(self):
        l1 = Location(0, 0)
        l2 = Location(1000, 2000)
        l3 = Location(50000, -1000000)

        p1 = geometry.location_to_shapely_point(l1)
        p2 = geometry.location_to_shapely_point(l2)
        p3 = geometry.location_to_shapely_point(l3)

        self.assertEquals(shapely.geometry.Point(0, 0), p1)
        self.assertEquals(shapely.geometry.Point(1000, 2000), p2)
        self.assertEquals(shapely.geometry.Point(50000, -1000000), p3)

    def test_polyline_to_shapely_linestring(self):
        polyline1 = PolyLine([Location(-1000, -1000), Location(0, 0), Location(5000, 8000)])
        linestring1 = geometry.polyline_to_shapely_linestring(polyline1)
        test_against = shapely.geometry.LineString([(-1000, -1000), (0, 0), (5000, 8000)])
        self.assertEquals(linestring1, test_against)
