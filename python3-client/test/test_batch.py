from uuid import uuid4

from zerograph import Zerograph


def test_single_execute():
    zg = Zerograph("localhost", 47474)
    batch = zg.create_batch()
    batch.execute("CREATE (a:Person {name:'Alice'}) RETURN a")
    for rs in batch.submit():
        print(rs)


def test_create_single_node():
    zg = Zerograph("localhost", 47474)
    batch = zg.create_batch()
    batch.create_node(["Person"], {"name": "Alice"})
    for rs in batch.submit():
        print(rs)


def test_create_a_hundred_nodes():
    zg = Zerograph("localhost", 47474)
    batch = zg.create_batch()
    for n in range(100):
        batch.create_node(["Number"], {"value": n})
    for rs in batch.submit():
        print(rs)


def test_create_ten_thousand_nodes():
    zg = Zerograph("localhost", 47474)
    batch = zg.create_batch()
    for n in range(10000):
        batch.create_node(["Number"], {"value": n, "uuid": uuid4().hex})
    for rs in batch.submit():
        print(rs)


def test_create_two_nodes_and_a_rel_100x():
    zg = Zerograph("localhost", 47474)
    batch = zg.create_batch()
    for n in range(100):
        alice = batch.create_node(["Person"], {"name": "Alice"})
        bob = batch.create_node(["Person"], {"name": "Bob"})
        batch.create_rel(alice, bob, "KNOWS", {"since": 1999})
    for rs in batch.submit():
        print(rs)
