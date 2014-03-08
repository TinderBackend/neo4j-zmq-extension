package org.zerograph.except;

public class ServiceException extends Exception {

    final private int port;

    public ServiceException(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

}
