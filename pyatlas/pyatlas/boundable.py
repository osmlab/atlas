class Boundable(object):
    """
    A Boundable is an object that can be bounded by Rectangle.
    """

    def __init__(self):
        raise NotImplementedError('Boundable should not be instantiated')

    def get_bounds(self):
        raise NotImplementedError('subclass must implement')
