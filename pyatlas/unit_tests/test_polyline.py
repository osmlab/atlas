import unittest

from pyatlas import polyline
from pyatlas.location import Location

class PolyLineTest(unittest.TestCase):
    def setUp(self):
        pass

    def test_polyline_compression(self):
        # loclist1 = [Location(382117269, -1193153616),
        #             Location(382117927, -1193152951),
        #             Location(382116912, -1193151049),
        #             Location(382116546, -1193151382),
        #             Location(382116134, -1193150734),
        #             Location(382115440, -1193151494),
        #             Location(382115964, -1193152234),
        #             Location(382116293, -1193151948)]
        # polystr1 = 'kxqywU~ciwbfAch@qh@l~@{uBzUxSvXog@jj@nn@w_@fm@qS{P'
        # #polystr2 = 'ydxjyUl_ly`fAa}@m]fPww@b}@n]'
        # #polystr3 = 'got_qUh}bbseAraBFCps@saBI'
        #
        # for correctloc, testloc in zip(loclist1, polyline.as_polyline(polystr1).points):
        #    self.assertEqual(correctloc, testloc)

        loclist = [Location(1, 1), Location(2, 2)]
        correct_polyline = polyline.PolyLine(loclist, deep=True)
        test_polyline = polyline.as_polyline(correct_polyline.compress())
        print(correct_polyline)
        print(test_polyline)
        self.assertEqual(correct_polyline, test_polyline)
        loclist = [Location(382117269, -1193153616),
                   Location(382117927, -1193152951),
                   Location(382116912, -1193151049)]
        correct_polyline = polyline.PolyLine(loclist, deep=True)
        test_polyline = polyline.as_polyline(correct_polyline.compress())
        self.assertEqual(correct_polyline, test_polyline)



if __name__ == "__main__":
    unittest.main()
