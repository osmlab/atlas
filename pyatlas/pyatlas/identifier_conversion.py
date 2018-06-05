_IDENTIFIER_SCALE = 1000


def get_osm_identifier(full_atlas_identifier):
    """
     Get the OSM identifier from the full Atlas identifier by removing the
     country code and way sectioning identifiers

     Example:
     Atlas ID: 222222001003 would return OSM ID: 222222
    """
    return abs(full_atlas_identifier / (_IDENTIFIER_SCALE * _IDENTIFIER_SCALE))
