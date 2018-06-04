_LATITUDE_MIN_DM7 = -900000000
_LATITUDE_MAX_DM7 = 900000000
_LONGITUDE_MIN_DM7 = -1800000000
_LONGITUDE_MAX_DM7 = 1800000000 - 1

_DM7_PER_DEGREE = 10000000
"""There are 10 million dm7 in a degree"""


class Location:
    """
    A latitude-longitude location. Uses the dm7 type to represent coordinates.
    dm7 is an integral representation of a decimal degree fixed to 7 places of
    precision. 7 places are enough to specify any location on Earth with
    submeter accuracy.

    Examples:
    45.01 degrees -> 450_100_000
    -90 degrees -> -900_000_000
    150.5 degrees -> 1_505_000_000
    """

    def __init__(self, latitude, longitude):
        latitude = int(latitude)
        longitude = int(longitude)
        if latitude > _LATITUDE_MAX_DM7 or latitude < _LATITUDE_MIN_DM7:
            raise ValueError('latitude {} out of range'.format(str(latitude)))
        if longitude > _LONGITUDE_MAX_DM7 or longitude < _LONGITUDE_MIN_DM7:
            raise ValueError('longitude {} out of range'.format(
                str(longitude)))
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


def degree_as_dm7(degree):
    """
    Given a degree, return the equivalent dm7. Does not perform range validation.
    Performs integer conversion of the result.
    """
    return int(round(_DM7_PER_DEGREE * degree))


def dm7_as_degree(dm7):
    """
    Given a dm7, return the equivalent degree. Does not perform range validation.
    """
    return float(dm7) / _DM7_PER_DEGREE


def get_location_from_packed_int(packed_location):
    """
    Decode a Location object from a 64 bit integer with the latitude and
    longitude packed in.
    :param packed_location: a packed 64 bit integer
    :return: a new Location with unpacked latitude and longitude
    """
    longitude = packed_location & 0xFFFFFFFF
    if longitude & 0x80000000 > 0:
        longitude = longitude - (1 << 32)
    # alternate way to test
    #if longitude < _LONGITUDE_MIN_DM7 or longitude > _LONGITUDE_MAX_DM7:
    #    longitude = longitude - (1 << 32)

    latitude = (packed_location >> 32) & 0xFFFFFFFF
    if latitude & 0x80000000 > 0:
        latitude = latitude - (1 << 32)
    # alternate way to test
    #if latitude < _LATITUDE_MIN_DM7 or latitude > _LATITUDE_MAX_DM7:
    #    latitude = latitude - (1 << 32)

    return Location(latitude, longitude)
