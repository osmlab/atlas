import unittest

from pyatlas.atlas import Atlas
from pyatlas import geometry
from pyatlas.geometry import Rectangle


class AtlasTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_lazy_loading(self):
        atlas = Atlas("resources/test.atlas")
        _touch_all_atlas_features(atlas)
        self.assertEqual(atlas.number_of_points(), 5)
        self.assertEqual(atlas.number_of_lines(), 2)
        self.assertEqual(atlas.number_of_areas(), 2)
        self.assertEqual(atlas.number_of_nodes(), 4)
        self.assertEqual(atlas.number_of_edges(), 3)
        self.assertEqual(atlas.number_of_relations(), 2)

    def test_upfront_loading(self):
        atlas = Atlas("resources/test.atlas", lazy_loading=False)
        _touch_all_atlas_features(atlas)
        self.assertEqual(atlas.number_of_points(), 5)
        self.assertEqual(atlas.number_of_lines(), 2)
        self.assertEqual(atlas.number_of_areas(), 2)
        self.assertEqual(atlas.number_of_nodes(), 4)
        self.assertEqual(atlas.number_of_edges(), 3)
        self.assertEqual(atlas.number_of_relations(), 2)

    def test_point_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        lower_left = geometry.location_with_degrees(37, -118.02)
        upper_right = geometry.location_with_degrees(39, -118)

        # NOTE point 1 does not show up in the results because it lies on the polygon border
        test_results = atlas.points_within(Rectangle(lower_left, upper_right))
        self.assertEquals({atlas.point(2), atlas.point(3)}, test_results)

        test_results = atlas.points_within(
            Rectangle(lower_left, upper_right), lambda p: p.get_identifier() % 2 != 0)
        self.assertEquals({atlas.point(3)}, test_results)

        test_results = atlas.points_at(geometry.location_with_degrees(38, -118))
        self.assertEquals({atlas.point(1)}, test_results)

    def test_line_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        test_location = geometry.location_with_degrees(38.02, -118.02)
        test_results = atlas.lines_containing(test_location)
        self.assertEquals({atlas.line(1)}, test_results)

        poly = Rectangle(
            geometry.location_with_degrees(38, -118), geometry.location_with_degrees(39, -119))
        test_results = atlas.lines_intersecting(poly)
        self.assertEquals({atlas.line(1), atlas.line(2)}, test_results)

    def test_area_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        test_location = geometry.location_with_degrees(38.15, -118.03)
        test_results = atlas.areas_covering(test_location)
        self.assertEquals({atlas.area(2)}, test_results)

        test_results = atlas.areas_intersecting(atlas.area(2).as_polygon())
        self.assertEquals({atlas.area(1), atlas.area(2)}, test_results)

    def test_node_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        lower_left = geometry.location_with_degrees(39, -119.04)
        upper_right = geometry.location_with_degrees(39.05, -119)

        # NOTE node 4 does not show up in results because it lies on the the polygon border
        test_results = atlas.nodes_within(Rectangle(lower_left, upper_right))
        self.assertEquals({atlas.node(2)}, test_results)

        test_results = atlas.nodes_within(
            Rectangle(lower_left, upper_right), lambda n: n.get_identifier() == 3)
        self.assertEquals(frozenset(), test_results)

        test_results = atlas.nodes_at(geometry.location_with_degrees(39, -119.05))
        self.assertEquals({atlas.node(3)}, test_results)

    def test_edge_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        test_location = geometry.location_with_degrees(39, -119.05)
        test_results = atlas.edges_containing(test_location)
        self.assertEquals({atlas.edge(1), atlas.edge(3)}, test_results)

        poly = Rectangle(
            geometry.location_with_degrees(38, -120), geometry.location_with_degrees(40, -117))
        test_results = atlas.edges_intersecting(poly)
        self.assertEquals({atlas.edge(1), atlas.edge(2), atlas.edge(3)}, test_results)

    def test_relation_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        lower_left = geometry.location_with_degrees(37.999, -118.001)
        upper_right = geometry.location_with_degrees(38.001, -117.999)

        test_results = atlas.relations_with_entities_intersecting(
            Rectangle(lower_left, upper_right))
        self.assertEquals({atlas.relation(2)}, test_results)


def _touch_all_atlas_features(atlas):
    for point in atlas.points():
        string = point.__str__()

    for line in atlas.lines():
        string = line.__str__()

    for area in atlas.areas():
        string = area.__str__()

    for node in atlas.nodes():
        string = node.__str__()

    for edge in atlas.edges():
        string = edge.__str__()

    for relation in atlas.relations():
        string = relation.__str__()

    string = str(atlas.point(1))
    string = str(atlas.line(1))
    string = str(atlas.area(1))
    string = str(atlas.node(1))
    string = str(atlas.edge(1))
    string = str(atlas.relation(1))


if __name__ == "__main__":
    unittest.main()
