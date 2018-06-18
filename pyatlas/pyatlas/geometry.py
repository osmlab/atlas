"""
This module defines the pyatlas geometry primitives as well as various helping
functions for manipulating the geometry. These primitives are built using
lat-long locations on the Earth.
"""

import shapely.geometry

# --- Location definition constants ---
_LATITUDE_MIN_DM7 = -900000000
_LATITUDE_MAX_DM7 = 900000000
_LONGITUDE_MIN_DM7 = -1800000000
_LONGITUDE_MAX_DM7 = 1800000000 - 1
# There are 10 million dm7 in a degree
_DM7_PER_DEGREE = 10000000

# --- PolyLine encoding constants ---
# Do not change the precision. It matches the default in the Java implementation.
_PRECISION = 7
_ENCODING_OFFSET_MINUS_ONE = 63
_FIVE_BIT_MASK = 0x1f
_SIXTH_BIT_MASK = 0x20
_BIT_SHIFT = 5
_MAXIMUM_DELTA_LONGITUDE = 180 * pow(10, _PRECISION)


class Boundable(object):
    """
    A Boundable is any geometric object that can be bounded by Rectangle.
    """

    def __init__(self):
        raise NotImplementedError('Boundable should not be instantiated')

    def bounds(self):
        """
        Get the bounding Rectangle of this object.
        """
        raise NotImplementedError('subclass must implement')

    def intersects(self, polygon):
        """
        Check if this Boundable intersects some Polygon.
        """
        raise NotImplementedError('subclass must implement')


class Location(Boundable):
    """
    A latitude-longitude location. This is the building block of the other
    geometric types.

    Uses the dm7 type to represent coordinates.
    dm7 is an integral representation of a decimal degree fixed to 7 places of
    precision. 7 places are enough to specify any location on Earth with
    submeter accuracy.

    Examples:
    45.01 degrees -> 450_100_000 dm7
    -90 degrees -> -900_000_000 dm7
    150.5 degrees -> 1_505_000_000 dm7
    """

    def __init__(self, latitude, longitude):
        """
        Create a new Location with a dm7 latitude and longitude. To create a
        Location with degrees, use the geometry.location_with_degrees() module
        function.
        """
        if not isinstance(latitude, (int, long)):
            raise TypeError('latitude must be an integer')
        if not isinstance(longitude, (int, long)):
            raise TypeError('longitude must be an integer')

        if latitude > _LATITUDE_MAX_DM7 or latitude < _LATITUDE_MIN_DM7:
            raise ValueError('latitude {} out of range'.format(str(latitude)))
        if longitude > _LONGITUDE_MAX_DM7 or longitude < _LONGITUDE_MIN_DM7:
            raise ValueError('longitude {} out of range'.format(str(longitude)))

        self.latitude = latitude
        self.longitude = longitude

    def __str__(self):
        """
        Get the wkt string representation of this Location. The pair ordering
        of Locations is always (LATITUDE, LONGITUDE)
        """
        shapely_point = location_to_shapely_point(self)
        return shapely_point.wkt

    def __eq__(self, other):
        """
        Check if two Locations are equal. Equivalent Locations have equal
        latitude and equal longitude.
        """
        return self.latitude == other.latitude and self.longitude == other.longitude

    def get_as_packed_int(self):
        """
        Pack this Location into a 64 bit integer. The higher order 32 bits are
        the latitude, the lower order 32 bits are the longitude.
        """
        packed = self.latitude
        packed = packed << 32
        packed = packed | (self.longitude & 0xFFFFFFFF)
        return packed

    def bounds(self):
        """
        Get the bounding Rectangle of this Location.
        """
        return Rectangle(self, self)

    def intersects(self, polygon):
        """
        Check if this Location intersects some Polygon.
        """
        return polygon.fully_geometrically_encloses_location(self)

    def get_latitude(self):
        """
        Get the latitude of this Location as a dm7.
        """
        return self.latitude

    def get_latitude_deg(self):
        """
        Get the latitude of this Location as a degree.
        """
        return dm7_as_degree(self.get_latitude())

    def get_longitude(self):
        """
        Get the longitude of this Location as a dm7.
        """
        return self.longitude

    def get_longitude_deg(self):
        """
        Get the latitude of this Location as a degree.
        """
        return dm7_as_degree(self.get_longitude())


class PolyLine(Boundable):
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
                new_point = Location(point.get_latitude(), point.get_longitude())
                self.location_list.append(new_point)
        else:
            self.location_list = location_list

    def __str__(self):
        """
        Get the wkt string representation of this PolyLine.
        """
        shapely_poly = polyline_to_shapely_linestring(self)
        return shapely_poly.wkt

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
        last = Location(0, 0)

        for point in self.locations():
            latitude = int(round(dm7_as_degree(point.latitude) * precision))
            longitude = int(round(dm7_as_degree(point.longitude) * precision))

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

    def bounds(self):
        """
        Get the bounding Rectangle of this PolyLine.
        """
        return bounds_locations(self.locations())

    def intersects(self, polygon):
        """
        Check if this PolyLine intersects some Polygon.
        """
        return polygon.overlaps_polyline(self)

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


class Polygon(PolyLine):
    """
    A special case of PolyLine that has an extra segment between the last and
    first point - effectively a closed PolyLine. The Polygon does not actually
    re-store the last (first) Location. Instead, the API simulates its presence.
    """

    def __init__(self, location_list, deep=False):
        """
        Create a new Polygon given a Location list. By default, it will perform
        a shallow copy of the parameter list. Can optionally perform a deep copy
        of the list.
        """
        super(Polygon, self).__init__(location_list, deep)

    def __str__(self):
        """
        Get the wkt string representation of this Polygon.
        """
        shapely_poly = polygon_to_shapely_polygon(self)
        return shapely_poly.wkt

    def fully_geometrically_encloses_location(self, location):
        """
        Test if this Polygon fully geometrically encloses a given Location. Will
        return False if the Location lies perfectly on the Polygon's boundary.
        """
        shapely_point = location_to_shapely_point(location)
        shapely_poly_self = polygon_to_shapely_polygon(self)
        return shapely_poly_self.contains(shapely_point)

    def _overlaps_polygon(self, polygon):
        """
        Test if this Polygon overlaps a given Polygon at any point.
        """
        # TODO Shapely differentiates between overlaps and intersects
        # Shapely intersects() allows one to contain the other
        # Shapely overlaps() means they intersect, but neither contains the other
        # which is the right choice here?
        shapely_polyg_self = polygon_to_shapely_polygon(self)
        shapely_polyg_other = polygon_to_shapely_polygon(polygon)
        return shapely_polyg_self.intersects(shapely_polyg_other)

    def overlaps_polyline(self, polyline):
        """
        Test if this Polygon overlaps a given PolyLine at any point.
        """
        # TODO Shapely differentiates between overlaps and intersects
        # Shapely intersects() allows one to contain the other
        # Shapely overlaps() means they intersect, but neither contains the other
        # which is the right choice here?
        shapely_polyline = polyline_to_shapely_linestring(polyline)
        shapely_poly_self = polygon_to_shapely_polygon(self)
        return shapely_poly_self.intersects(shapely_polyline)

    def closed_loop(self):
        """
        Get a generator for the Locations in this Polygon. Will generate the
        first item again at the end, simulating the closedness of the Polygon.
        """
        for point in self.locations():
            yield point
        yield self.location_list[0]

    def intersects(self, polygon):
        """
        Check if this Polygon intersects some other Polygon (ie. overlaps it
        at any point).
        """
        return self._overlaps_polygon(polygon)


class Rectangle(Polygon):
    """
    A special case of Polygon.
    """

    def __init__(self, lower_left, upper_right):
        """
        Create a new Rectangle using a lower left corner Location and an
        upper right corner Location.
        """
        upper_left = Location(upper_right.get_latitude(), lower_left.get_longitude())
        lower_right = Location(lower_left.get_latitude(), upper_right.get_longitude())
        locations = [lower_left, upper_left, upper_right, lower_right]
        super(Rectangle, self).__init__(locations, deep=True)
        self.lower_left = lower_left
        self.upper_right = upper_right

    def get_lower_left(self):
        """
        Get the lower left corner Location of this Rectangle.
        """
        return self.lower_left

    def get_upper_right(self):
        """
        Get the upper right corner Location of this Rectangle.
        """
        return self.upper_right


def location_with_degrees(latitude, longitude):
    """
    Get a new Location with a latitude and longitude specified in degree values.
    """
    latitude = degree_as_dm7(latitude)
    longitude = degree_as_dm7(longitude)
    return Location(latitude, longitude)


def location_from_packed_int(packed_location):
    """
    Decode a Location object from a packed 64 bit integer. See
    Location.get_as_packed_int() for more information.
    """
    longitude = packed_location & 0xFFFFFFFF
    if longitude & 0x80000000 > 0:
        longitude = longitude - (1 << 32)

    latitude = (packed_location >> 32) & 0xFFFFFFFF
    if latitude & 0x80000000 > 0:
        latitude = latitude - (1 << 32)

    return Location(latitude, longitude)


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


def decompress_polyline(bytestring):
    """
    Given a PolyLine bytestring obtained using PolyLine.compress(),
    decompress it and return it as a PolyLine.
    """
    locations = _decompress_bytestring(bytestring)
    return PolyLine(locations)


def decompress_polygon(bytestring):
    """
    Given a PolyLine bytestring obtained using PolyLine.compress(),
    decompress it and return it as a Polygon.
    """
    locations = _decompress_bytestring(bytestring)
    return Polygon(locations)


def bounds_locations(locations):
    """
    Build a Rectangle that bounds an iterable of Locations.
    """
    yielded_at_least_one = False
    lower_lat = None
    upper_lat = None
    left_lon = None
    right_lon = None

    for location in locations:
        yielded_at_least_one = True
        latitude = location.get_latitude()
        longitude = location.get_longitude()
        if lower_lat is None or latitude < lower_lat:
            lower_lat = latitude
        if upper_lat is None or latitude > upper_lat:
            upper_lat = latitude
        if left_lon is None or longitude < left_lon:
            left_lon = longitude
        if right_lon is None or longitude > right_lon:
            right_lon = longitude

    if not yielded_at_least_one:
        raise ValueError('location iterable must yield at least one value')

    return Rectangle(Location(lower_lat, left_lon), Location(upper_lat, right_lon))


def bounds_atlasentities(entities):
    """
    Build a Rectangle that bounds an iterable of AtlasEntities.
    """
    yielded_at_least_one = False
    lower_lat = None
    upper_lat = None
    left_lon = None
    right_lon = None

    for entity in entities:
        yielded_at_least_one = True
        for location in entity.bounds().locations():
            latitude = location.get_latitude()
            longitude = location.get_longitude()
            if lower_lat is None or latitude < lower_lat:
                lower_lat = latitude
            if upper_lat is None or latitude > upper_lat:
                upper_lat = latitude
            if left_lon is None or longitude < left_lon:
                left_lon = longitude
            if right_lon is None or longitude > right_lon:
                right_lon = longitude

    if not yielded_at_least_one:
        raise ValueError('entity iterable must yield at least one value')

    return Rectangle(Location(lower_lat, left_lon), Location(upper_lat, right_lon))


def boundable_to_shapely_box(boundable):
    """
    Convert a pyatlas Boundable type to its Shapely Polygon representation.
    The Shapely Polygon will always be a rectangle.
    """
    return polygon_to_shapely_polygon(boundable.bounds())


def polygon_to_shapely_polygon(polygon):
    """
    Convert a pyatlas Polygon to its Shapely Polygon representation.
    """
    shapely_points = []
    for location in polygon.locations():
        shapely_points.append(location_to_shapely_point(location))

    return shapely.geometry.Polygon(shapely.geometry.LineString(shapely_points))


def location_to_shapely_point(location):
    """
    Convert a Location to its Shapely Point representation.
    """
    latitude = location.get_latitude()
    longitude = location.get_longitude()

    return shapely.geometry.Point(latitude, longitude)


def polyline_to_shapely_linestring(polyline):
    """
    Convert a PolyLine to its Shapely LineString representation.
    """
    shapely_points = []
    for location in polyline.locations():
        shapely_points.append(location_to_shapely_point(location))

    return shapely.geometry.LineString(shapely_points)


def _encode_number(number):
    """
    Encode a number as a unicode character.
    """
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
    """
    Reverse the compression algorithm in PolyLine.compress().
    """
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
        latitude = degree_as_dm7(latitude)
        longitude = degree_as_dm7(longitude)

        locations.append(Location(latitude, longitude))

    return locations


def _urshift32(to_shift, shift_amount):
    """
    Perform a 32 bit unsigned right shift (drag in leading 0s).
    """
    return (to_shift % 0x100000000) >> shift_amount
