import unittest

from pyatlas.atlas import Atlas
from pyatlas.rectangle import Rectangle
from pyatlas import location


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

        lower_left = location.with_degrees(37, -118.02)
        upper_right = location.with_degrees(39, -118)

        test_results = atlas.points_within(Rectangle(lower_left, upper_right))
        self.assertEquals({atlas.point(1), atlas.point(2), atlas.point(3)}, test_results)

        test_results = atlas.points_within(
            Rectangle(lower_left, upper_right), lambda p: p.get_identifier() % 2 != 0)
        self.assertEquals({atlas.point(1), atlas.point(3)}, test_results)

        test_results = atlas.points_at(location.with_degrees(38, -118))
        self.assertEquals({atlas.point(1)}, test_results)

    def test_node_spatial_index(self):
        atlas = Atlas("resources/test.atlas")

        lower_left = location.with_degrees(39, -119.04)
        upper_right = location.with_degrees(39.05, -119)

        test_results = atlas.nodes_within(Rectangle(lower_left, upper_right))
        self.assertEquals({atlas.node(2), atlas.node(4)}, test_results)

        test_results = atlas.nodes_within(
            Rectangle(lower_left, upper_right), lambda n: n.get_identifier() == 3)
        self.assertEquals(frozenset(), test_results)

        test_results = atlas.nodes_at(location.with_degrees(39, -119.05))
        self.assertEquals({atlas.node(3)}, test_results)


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
