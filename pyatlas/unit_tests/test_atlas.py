import unittest

from pyatlas.atlas import Atlas


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
