import unittest

from pyatlas.atlas import Atlas


class AtlasTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_lazy_loading(self):
        atlas = Atlas("resources/test.atlas")
        _touch_all_atlas_features(atlas)

    def test_upfront_loading(self):
        atlas = Atlas("resources/test.atlas", lazy_loading=False)
        _touch_all_atlas_features(atlas)


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


if __name__ == "__main__":
    unittest.main()
