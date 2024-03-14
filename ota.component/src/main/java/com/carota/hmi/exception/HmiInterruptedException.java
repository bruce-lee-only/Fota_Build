package com.carota.hmi.exception;

public class HmiInterruptedException extends RuntimeException {
    static final long serialVersionUID = 123123123322L;

    public HmiInterruptedException() {
        super("HMI-Task Task Interrupted");
    }
}
