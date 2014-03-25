package org.neozmq.util;

public class Pointer {

    final public int address;

    public Pointer(int address) {
        this.address = address;
    }

    public int getAddress() {
        return this.address;
    }

}
