class PackedTagStore:
    """
    Store indexes into the global tag dictionary.
    """

    def __init__(self, dictionary):
        self.keys = []
        self.values = []
        self.dictionary = dictionary

    def set_dictionary(self, dictionary):
        self.dictionary = dictionary

    def __str__(self):
        string = "KEYS:\n"
        string += str(self.keys)
        string += "\nVALUES:\n"
        string += str(self.values)
        return string

    def to_key_value_dict(self, index):
        if self.dictionary is None:
            raise ValueError('dictionary is not set')

        result = {}
        if len(self.keys) == 0:
            return result
        key_array = self.keys[index]
        value_array = self.values[index]

        if len(key_array) != len(value_array):
            raise ValueError('array length mismatch')

        for key, value in zip(key_array, value_array):
            result[self.dictionary.word(key)] = self.dictionary.word(value)

        return result


def get_packed_tag_store_from_proto(proto_store):
    """
    Takes a decoded ProtoPackedTagStore object and turns
    it into a more user friendly PackedTagStore object.
    :param proto_store: the proto store
    :return: the converted PackedTagStore
    """
    new_store = PackedTagStore(None)

    for key_array in proto_store.keys.arrays:
        new_sub_array = []
        for element in key_array.elements:
            new_sub_array.append(element)
        new_store.keys.append(new_sub_array)

    for value_array in proto_store.values.arrays:
        new_sub_array = []
        for element in value_array.elements:
            new_sub_array.append(element)
        new_store.values.append(new_sub_array)

    return new_store
