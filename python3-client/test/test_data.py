
from zerograph.data import Data


def test_can_decode_pointer():
    decoded = Data.decode('/*Pointer*/345')
    assert decoded.class_name == "Pointer"
    assert decoded.value == 345


def test_decode_node():
    decoded = Data.decode('/*Node*/{"id":123,"labels":["Human"],"properties":{"name":"Bob"}}')
    assert decoded.class_name == "Node"
    assert decoded.value == {"id":123, "labels":["Human"], "properties":{"name":"Bob"}}
