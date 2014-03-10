
import json

from .data import Data


class Entity(object):

    def __init__(self, attributes):
        self.__id = attributes.get("id")

    def __repr__(self):
        return "<Entity id={0}>".format(self._id)

    @property
    def _id(self):
        return self.__id


class PropertyContainer(Entity):

    def __init__(self, attributes):
        Entity.__init__(self, attributes)
        self.__properties = dict(attributes.get("properties", {}))

    def __repr__(self):
        return "<PropertyContainer id={0} properties={1}>".format(self._id, self.properties)

    @property
    def properties(self):
        return self.__properties


class Node(PropertyContainer):

    def __init__(self, attributes):
        PropertyContainer.__init__(self, attributes)
        self.__labels = set(attributes.get("labels", set()))

    def __repr__(self):
        return "<Node id={0} labels={1} properties={2}>".format(self._id, self.labels, self.properties)

    def __eq__(self, other):
        return self.labels == other.labels and \
               self.properties == other.properties

    def __ne__(self, other):
        return not self.__eq__(other)

    @property
    def labels(self):
        return self.__labels


class Rel(PropertyContainer):

    def __init__(self, attributes):
        PropertyContainer.__init__(self, attributes)
        self.__start = Node(attributes["start"])
        self.__end = Node(attributes["end"])
        self.__type = attributes["type"]

    def __repr__(self):
        return "<Rel id={0} start={1} end={2} type={3} properties={4}>".format(self._id, self.start, self.end, repr(self.type), self.properties)

    @property
    def start(self):
        return self.__start

    @property
    def end(self):
        return self.__end

    @property
    def type(self):
        return self.__type


class Pointer(object):

    def __init__(self, attributes):
        self.__address = attributes

    def __repr__(self):
        return "<Pointer address={0}>".format(self.address)

    def __eq__(self, other):
        return self.address == other.address

    def __ne__(self, other):
        return not self.__eq__(other)

    @property
    def address(self):
        return self.__address


def hydrate(string):
    data = Data.decode(string)
    if data.class_name == "Node":
        return Node(data.value)
    elif data.class_name == "Rel":
        return Rel(data.value)
    elif data.class_name == "Pointer":
        return Pointer(data.value)
    else:
        return data.value


def dehydrate(obj):
    if isinstance(obj, Pointer):
        return Data("Pointer", obj.address).encode()
    else:
        return json.dumps(obj, separators=",:")
