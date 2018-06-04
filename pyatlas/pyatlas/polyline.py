"""
PolyLine module.
"""
"""
Do not change the precision. It matches the default in the Java implementation.
"""
_PRECISION = 7
_ENCODING_OFFSET_MINUS_ONE = 63
_FIVE_BIT_MASK = 0x1f
_SIXTH_BIT_MASK = 0x20
_BIT_SHIFT = 5


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
    index = 0
    latitude = 0
    longitude = 0
    locations = []

    while index < length:
        pass

    return PolyLine(locations)
