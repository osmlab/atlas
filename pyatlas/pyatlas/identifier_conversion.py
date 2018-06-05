"""
Helpful functions to extract information from Atlas identifiers.
"""

# Country code and way sectioned identifiers are 3 decimal digits
_IDENTIFIER_SCALE = 1000


def get_osm_identifier(full_atlas_identifier):
    """
     Get the OSM identifier from the full Atlas identifier by removing the
     country code and way sectioned identifiers.

     Example:
     Atlas ID: 222222001003 would return OSM ID: 222222
    """
    full_atlas_identifier = abs(full_atlas_identifier)
    return full_atlas_identifier / (_IDENTIFIER_SCALE * _IDENTIFIER_SCALE)
