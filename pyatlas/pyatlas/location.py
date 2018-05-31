class Location:
    """
    A latitude-longitude location.
    """

    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude

    def __str__(self):
        return "[" + str(self.latitude) + ", " + str(self.longitude) + "]"

    def __eq__(self, other):
        return self.latitude == other.latitude and self.longitude == other.longitude

    def get_as_packed_int(self):
        """
        Pack this Location into a 64 bit integer. The higher order 32 bits are
        the latitude, the lower order bits are the longitude.
        :return: the packed Location
        """
        packed = self.latitude
        packed = packed << 32
        packed = packed | (self.longitude & 0xFFFFFFFF)
        return packed


def get_location_from_packed_int(packed_location):
    """
    Decode a Location object from a 64 bit integer with the latitude and
    longitude packed in.
    :param packed_location: a packed 64 bit integer
    :return: a new Location with unpacked latitude and longitude
    """
    longitude = packed_location & 0xFFFFFFFF
    latitude = (packed_location >> 32) & 0xFFFFFFFF
    return Location(latitude, longitude)
