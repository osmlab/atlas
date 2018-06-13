"""
Helpful functions to convert between pyatlas types and Shapely polygons.
"""

import shapely.geometry


def boundable_to_shapely_box(boundable):
    """
    Convert a pyatlas Boundable type to its Shapely Polygon representation.
    The Shapely Polygon will always be a rectangle.
    """
    return polygon_to_shapely_polygon(boundable.get_bounds())


def polygon_to_shapely_polygon(polygon):
    """
    Convert a pyatlas Polygon to its Shapely Polygon representation.
    """
    shapely_points = []
    for point in polygon.locations():
        shapely_points.append(shapely.geometry.Point(point.get_latitude(), point.get_longitude()))

    return shapely.geometry.Polygon(shapely.geometry.LineString(shapely_points))
