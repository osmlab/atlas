import polyline
import polygon_converters


class Polygon(polyline.PolyLine):
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
        shapely_poly = polygon_converters.polygon_to_shapely_polygon(self)
        return shapely_poly.wkt

    def fully_geometrically_encloses_location(self, location0):
        """
        Test if this Polygon fully geometrically encloses a given Location. Will
        return False if the Location lies perfectly on the Polygon's boundary.
        """
        point = polygon_converters.location_to_shapely_point(location0)
        poly = polygon_converters.polygon_to_shapely_polygon(self)
        return poly.contains(point)

    def overlaps_polygon(self, polygon0):
        """
        Test if this Polygon overlaps a given Polygon at any point.
        """
        # TODO Shapely differentiates between overlaps and intersects
        # Shapely intersects() allows one to contain the other
        # Shapely overlaps() means they intersect, but neither contains the other
        # which is the right choice here?
        polyg = polygon_converters.polygon_to_shapely_polygon(self)
        polyg0 = polygon_converters.polygon_to_shapely_polygon(polygon0)
        return polyg.intersects(polyg0)

    def overlaps_polyline(self, polyline0):
        """
        Test if this Polygon overlaps a given PolyLine at any point.
        """
        # TODO Shapely differentiates between overlaps and intersects
        # Shapely intersects() allows one to contain the other
        # Shapely overlaps() means they intersect, but neither contains the other
        # which is the right choice here?
        polyl = polygon_converters.polyline_to_shapely_linestring(polyline0)
        polyg = polygon_converters.polygon_to_shapely_polygon(self)
        return polyg.intersects(polyl)

    def closed_loop(self):
        """
        Get a generator for the Locations in this Polygon. Will generate the
        first item again at the end, simulating the closedness of the Polygon.
        """
        for point in self.locations():
            yield point
        yield self.location_list[0]


def decompress_polygon(bytestring):
    """
    Given a PolyLine bytestring compressed using PolyLine.compress(),
    decompress it and return it as a Polygon.
    """
    locations = polyline._decompress_bytestring(bytestring)
    return Polygon(locations)
