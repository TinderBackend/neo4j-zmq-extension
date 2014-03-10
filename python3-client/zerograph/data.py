
import json


class Data(object):

    @classmethod
    def decode(cls, string):
        if string.startswith("/*"):
            class_name, string = string[2:].partition("*/")[0::2]
            value = json.loads(string)
        else:
            value = json.loads(string)
            class_name = value.__class__.__name__
        return cls(class_name, value)

    def __init__(self, class_name, value):
        self.__class_name = class_name
        self.__value = value

    @property
    def class_name(self):
        return self.__class_name

    @property
    def value(self):
        return self.__value

    def encode(self):
        return "/*{0}*/{1}".format(self.__class_name,
                                   json.dumps(self.__value, separators=",:"))
