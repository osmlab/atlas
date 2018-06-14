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
