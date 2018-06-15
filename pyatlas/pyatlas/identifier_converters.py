"""
This module defines helpful functions to extract information from Atlas identifiers.
"""

# Country code and way sectioned identifiers are 3 decimal digits
_IDENTIFIER_SCALE = 1000


def get_osm_identifier(full_atlas_identifier):
    """
     Get the OSM identifier from the full Atlas identifier by removing the
     country code and way section index.

     Example:
     Atlas ID: 222222001003 would return OSM ID: 222222
    """
    full_atlas_identifier = abs(full_atlas_identifier)
    return full_atlas_identifier / (_IDENTIFIER_SCALE * _IDENTIFIER_SCALE)


def get_country_code(full_atlas_identifier):
    """
    Get the country code from the full Atlas identifier.

    Example:
    Atlas ID: 222222001003 would return country code: 1
    """
    full_atlas_identifier = abs(full_atlas_identifier)
    return (full_atlas_identifier / _IDENTIFIER_SCALE) % _IDENTIFIER_SCALE


def get_way_section_index(full_atlas_identifier):
    """
    Get the way section index from the full Atlas identifier.

    Example:
    Atlas ID: 222222001003 would return index: 3
    """
    full_atlas_identifier = abs(full_atlas_identifier)
    return full_atlas_identifier % _IDENTIFIER_SCALE
