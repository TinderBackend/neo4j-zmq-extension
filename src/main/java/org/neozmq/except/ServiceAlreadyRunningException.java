package org.neozmq.except;

public class ServiceAlreadyRunningException extends ServiceException {

    public ServiceAlreadyRunningException(int port) {
        super(port);
    }

}
