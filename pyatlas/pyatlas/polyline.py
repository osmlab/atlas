# Hardcode this to 7 to match the Java Atlas implementation
_PRECISION = 7


class PolyLine:
    """
    A PolyLine is a set of Locations in a specific order.
    """

    def __init__(self, location_list, deep=False):
        """
        Create a new PolyLine given a Location list. By default, it will perform
        a shallow copy of the parameter list. Can optionally perform a deep copy
        of the list.
        :param location_list: the points for this PolyLine
        :param deep: decide to perform a deep copy
        """
        if deep:
            self.points = []
            for location in location_list:
                self.points.append(location)
        else:
            self.points = location_list


def as_polyline(bytestring):
    precision = pow(10, -1 * _PRECISION)
    length = len(bytestring)
