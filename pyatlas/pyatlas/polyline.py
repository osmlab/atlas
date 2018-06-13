import location
import rectangle
import boundable

# Do not change the precision. It matches the default in the Java implementation.
_PRECISION = 7
_ENCODING_OFFSET_MINUS_ONE = 63
_FIVE_BIT_MASK = 0x1f
_SIXTH_BIT_MASK = 0x20
_BIT_SHIFT = 5
_MAXIMUM_DELTA_LONGITUDE = 180 * pow(10, _PRECISION)


class PolyLine(boundable.Boundable):
    """
    A PolyLine is a set of Locations in a specific order.
    """

    def __init__(self, location_list, deep=False):
        """
        Create a new PolyLine given a Location list. By default, it will perform
        a reference copy of the Location list. Can optionally perform a deep copy
        of the list instead.
        """
        if len(location_list) == 0:
            raise ValueError('cannot have an empty PolyLine')
        if deep:
            self.location_list = []
            for point in location_list:
                new_point = location.Location(point.get_latitude(), point.get_longitude())
                self.location_list.append(new_point)
        else:
            self.location_list = location_list

    def __str__(self):
        """
        Get a string representation of this PolyLine.
        """
        result = "["
        for point in self.locations():
            result += str(point) + ", "
        result += "]"
        return result

    def __eq__(self, other):
        """
        Check if this PolyLine is the same as another PolyLine. Compares their
        internal Location list.
        """
        if len(self.location_list) != len(other.location_list):
            return False
        for point, other_point in zip(self.locations(), other.locations()):
            if not point == other_point:
                return False
        return True

    def compress(self):
        """
        Transform this PolyLine into its compressed representation. The
        compression is based on the MapQuest compressed lat/lon encoding
        found here:
        https://developer.mapquest.com/documentation/common/encode-decode/
        """
        old_latitude = 0
        old_longitude = 0
        encoded = ""
        precision = pow(10, _PRECISION)
        last = location.Location(0, 0)

        for point in self.locations():
            latitude = int(round(location.dm7_as_degree(point.latitude) * precision))
            longitude = int(round(location.dm7_as_degree(point.longitude) * precision))

            encoded += _encode_number(latitude - old_latitude)
            delta_longitude = longitude - old_longitude
            if delta_longitude > _MAXIMUM_DELTA_LONGITUDE:
                raise ValueError(
                    'unable to compress polyline, consecutive points {} and {} too far apart', last,
                    point)
            encoded += _encode_number(delta_longitude)

            old_latitude = latitude
            old_longitude = longitude
            last = point

        return encoded

    def get_bounds(self):
        """
        Get the bounding Rectangle of this PolyLine.
        """
        return rectangle.bounds_locations(self.locations())

    def get_locations_list(self):
        """
        Get the underlying Location list for this PolyLine.
        """
        return self.location_list

    def locations(self):
        """
        Get a generator for the Locations in this PolyLine.
        """
        for point in self.location_list:
            yield point


def decompress_polyline(bytestring):
    """
    Given a PolyLine bytestring compressed using PolyLine.compress(),
    decompress it and return it as a PolyLine.
    """
    locations = _decompress_bytestring(bytestring)
    return PolyLine(locations)


def _encode_number(number):
    # encode a number as a unicode character
    number = number << 1
    if number < 0:
        number = ~number
    encoded = ""
    while number >= _SIXTH_BIT_MASK:
        code_point = (_SIXTH_BIT_MASK | number & _FIVE_BIT_MASK) + _ENCODING_OFFSET_MINUS_ONE
        encoded += unichr(code_point)
        number = _urshift32(number, _BIT_SHIFT)
    encoded += unichr(number + _ENCODING_OFFSET_MINUS_ONE)
    return encoded


def _decompress_bytestring(bytestring):
    # reverse the compression algorithm in PolyLine.compress()
    precision = pow(10, -1 * _PRECISION)
    length = len(bytestring)
    index = 0
    latitude = 0
    longitude = 0
    locations = []

    while index < length:
        shift = 0
        result = 0
        while True:
            byte_encoded = ord(bytestring[index]) - _ENCODING_OFFSET_MINUS_ONE
            result |= (byte_encoded & _FIVE_BIT_MASK) << shift
            shift += _BIT_SHIFT
            index += 1
            if byte_encoded < _SIXTH_BIT_MASK:
                break

        if result & 1 > 0:
            delta_latitude = ~(_urshift32(result, 1))
        else:
            delta_latitude = _urshift32(result, 1)
        latitude += delta_latitude

        shift = 0
        result = 0
        while True:
            byte_encoded = ord(bytestring[index]) - _ENCODING_OFFSET_MINUS_ONE
            result |= (byte_encoded & _FIVE_BIT_MASK) << shift
            shift += _BIT_SHIFT
            index += 1
            if byte_encoded < _SIXTH_BIT_MASK:
                break

        if result & 1 > 0:
            delta_longitude = ~(_urshift32(result, 1))
        else:
            delta_longitude = _urshift32(result, 1)
        longitude += delta_longitude

        latitude = latitude * precision
        longitude = longitude * precision

        # convert lat/lon to dm7
        latitude = location.degree_as_dm7(latitude)
        longitude = location.degree_as_dm7(longitude)

        locations.append(location.Location(latitude, longitude))

    return locations


def _urshift32(to_shift, shift_amount):
    # Perform a 32 bit unsigned right shift (drag in leading 0s)
    return (to_shift % 0x100000000) >> shift_amount
