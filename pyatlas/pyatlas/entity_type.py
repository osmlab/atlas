class EntityType(object):
    """
    An enum for AtlasEntity types.
    """

    def __init__(self):
        raise NotImplementedError

    # these MUST match the Java implementation for serialization compatibility
    NODE = 0
    EDGE = 1
    AREA = 2
    LINE = 3
    POINT = 4
    RELATION = 5

    _strs = {
        NODE: "NODE",
        EDGE: "EDGE",
        AREA: "AREA",
        LINE: "LINE",
        POINT: "POINT",
        RELATION: "RELATION",
    }


def to_str(value):
    """
    Convert an EntityType enum to a string representation.
    """
    return EntityType._strs[value]
