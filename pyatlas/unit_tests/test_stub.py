import unittest


class StubTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_stub(self):
        self.assertEqual(True, True)


if __name__ == "__main__":
    unittest.main()
