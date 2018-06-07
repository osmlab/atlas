class IntegerDictionary(object):
    """
    Integer string two-way dictionary.
    """

    # TODO do we even need this class?

    def __init__(self):
        self.word_to_index = {}
        self.index_to_word = {}

    def add(self, index, word):
        if word in self.word_to_index:
            return self.word_to_index[word]
        self.word_to_index[word] = index
        self.index_to_word[index] = word

    def size(self):
        return len(self.word_to_index)

    def word(self, index):
        # TODO this could throw a KeyError, how to handle?
        return self.index_to_word[index]


def get_integer_dictionary_from_proto(proto_integer_dictionary):
    """
    Takes a decoded ProtoIntegerStringDictionary object and turns
    it into a more user friendly IntegerDictionary object.
    :param proto_integer_dictionary: the proto dictionary
    :return: the converted dictionary
    """
    new_dict = IntegerDictionary()
    size_indexes = len(proto_integer_dictionary.indexes)
    size_words = len(proto_integer_dictionary.words)
    if size_words != size_indexes:
        raise ValueError('proto array sizes do not match')

    for index, word in zip(proto_integer_dictionary.indexes, proto_integer_dictionary.words):
        new_dict.add(index, word)

    return new_dict
