"""
Helpful functions to convert between pyatlas types and Shapely polygons.
"""

import shapely.geometry


def boundable_to_shapely_box(boundable):
    """
    Convert a pyatlas Boundable type to its Shapely Polygon representation.
    The Shapely Polygon will always be a rectangle.
    """
    # TODO make this implementation look like polygon_to_shapely_polygon
    bounds = boundable.get_bounds()
    lower_left = bounds.get_lower_left()
    upper_right = bounds.get_upper_right()
    shapely_box = shapely.geometry.box(
        lower_left.get_longitude(),
        lower_left.get_latitude(),
        upper_right.get_longitude(),
        upper_right.get_latitude(),
        ccw=False)

    return shapely.geometry.polygon.orient(shapely_box)


def polygon_to_shapely_polygon(polygon):
    """
    Convert a pyatlas Polygon to its Shapely Polygon representation.
    """
    shapely_points = []
    for point in polygon.locations():
        shapely_points.append(shapely.geometry.Point(point.get_latitude(), point.get_longitude()))

    return shapely.geometry.Polygon(shapely.geometry.LineString(shapely_points))
