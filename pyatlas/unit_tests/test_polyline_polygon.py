import unittest

from pyatlas import geometry
from pyatlas.geometry import Location, PolyLine, Polygon, Rectangle


class PolyLinePolygonTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_polyline_compression(self):
        loclist = [Location(1, 1), Location(2, 2), Location(5, 5)]
        correct_polyline = PolyLine(loclist, deep=True)
        test_polyline = geometry.decompress_polyline(correct_polyline.compress())
        self.assertEqual(correct_polyline, test_polyline)

        loclist = [
            Location(382117269, -1193153616),
            Location(382117927, -1193152951),
            Location(382116912, -1193151049),
            Location(382116546, -1193151382),
            Location(382116134, -1193150734),
            Location(382115440, -1193151494)
        ]
        correct_polyline = PolyLine(loclist, deep=True)
        test_polyline = geometry.decompress_polyline(correct_polyline.compress())
        self.assertEqual(correct_polyline, test_polyline)

    def test_polygon_closedness(self):
        loclist = [
            Location(382117269, -1193153616),
            Location(382117927, -1193152951),
            Location(382116912, -1193151049),
            Location(382116546, -1193151382),
            Location(382116134, -1193150734),
            Location(382115440, -1193151494)
        ]
        correct_polygon = Polygon(loclist, deep=True)
        closed_list = []
        for point in correct_polygon.closed_loop():
            closed_list.append(point)
        self.assertEqual(closed_list[0], closed_list[len(closed_list) - 1])

    def test_poly_bounds(self):
        # create a lopsided PolyLine to test bounding box
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        expected_rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        computed_rect = PolyLine(loclist).bounds()
        self.assertEqual(expected_rect, computed_rect)

        # now test again but with a Polygon
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        expected_rect = Rectangle(Location(0, 0), Location(450000000, 450000000))
        computed_rect = Polygon(loclist).bounds()
        self.assertEqual(expected_rect, computed_rect)

    def test_fully_geometrically_encloses_location(self):
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        polygon = Polygon(loclist)
        point = Location(1200, 1500)
        self.assertTrue(polygon.fully_geometrically_encloses_location(point))

        point = Location(0, 0)
        self.assertFalse(polygon.fully_geometrically_encloses_location(point))

        point = Location(-34, -1)
        self.assertFalse(polygon.fully_geometrically_encloses_location(point))

    def test_overlaps_polyline(self):
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        loclist2 = [Location(1, 1), Location(2000, 3000)]
        polygon = Polygon(loclist)
        polyline0 = PolyLine(loclist2)
        self.assertTrue(polygon.overlaps_polyline(polyline0))

    def test_intersects_polygon(self):
        loclist = [
            Location(0, 0),
            Location(400000000, 0),
            Location(350000000, 300000000),
            Location(450000000, 450000000),
            Location(1000, 450000000)
        ]
        loclist2 = [Location(1, 1), Location(10, 10), Location(2000, 3000)]
        polygon = Polygon(loclist)
        polygon2 = Polygon(loclist2)
        self.assertTrue(polygon.intersects(polygon2))


if __name__ == "__main__":
    unittest.main()
