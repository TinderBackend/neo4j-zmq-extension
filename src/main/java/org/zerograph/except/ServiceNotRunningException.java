package org.zerograph.except;

public class ServiceNotRunningException extends ServiceException {

    public ServiceNotRunningException(int port) {
        super(port);
    }

}
