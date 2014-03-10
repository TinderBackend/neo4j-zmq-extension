#!/usr/bin/env python3
# -*- encoding: utf-8 -*-


from __future__ import print_function, unicode_literals

import readline

from .zerograph import Zerograph, ClientError


def repl(zg):
    try:
        while True:
            line = input("\x1b[32;1mzerograph>\x1b[0m ")
            if line.lower() == "quit":
                break
            try:
                rs = zg.execute(line)
            except ClientError as err:
                print("\x1b[31;1m" + err.args[0] + "\x1b[0m")
            else:
                print(rs)
            print()
    except EOFError:
        print("⌁")

if __name__ == "__main__":
    repl(Zerograph())
