"""
Module for a special type of Polygon: the Rectangle.
"""

import polygon
import location


class Rectangle(polygon.Polygon):
    """
    A rectangle on the surface of earth. It cannot span the date change
    line (longitude -180).
    """

    def __init__(self, lower_left, upper_right):
        """
        Create a new Rectangle using a lower left corner Location and an
        upper right corner Location.
        """
        upper_left = location.Location(upper_right.get_latitude(), lower_left.get_longitude())
        lower_right = location.Location(lower_left.get_latitude(), upper_right.get_longitude())
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


def bounds_locations(locations):
    """
    Build a Rectangle that bounds an iterable of Locations.
    """
    yielded_at_least_one = False
    lower_lat = None
    upper_lat = None
    left_lon = None
    right_lon = None

    for location0 in locations:
        yielded_at_least_one = True
        latitude = location0.get_latitude()
        longitude = location0.get_longitude()
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

    return Rectangle(
        location.Location(lower_lat, left_lon), location.Location(upper_lat, right_lon))


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
        for location0 in entity.get_bounds().locations():
            latitude = location0.get_latitude()
            longitude = location0.get_longitude()
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

    return Rectangle(
        location.Location(lower_lat, left_lon), location.Location(upper_lat, right_lon))
