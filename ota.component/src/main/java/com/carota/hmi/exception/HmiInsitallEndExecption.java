package com.carota.hmi.exception;

public class HmiInsitallEndExecption extends RuntimeException {
    static final long serialVersionUID = 123123123322L;

    public HmiInsitallEndExecption() {
        super("HMI-Task Install Success");
    }
}
