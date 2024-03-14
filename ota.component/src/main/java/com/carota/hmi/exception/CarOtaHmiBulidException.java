package com.carota.hmi.exception;

import com.carota.hmi.type.UpgradeType;

public class CarOtaHmiBulidException extends RuntimeException {
    static final long serialVersionUID = 123123123321L;
    public CarOtaHmiBulidException(String message, UpgradeType type) {
        super(String.format("Hmi Bulid Error: %1s @%2s", message, type));
    }

    public CarOtaHmiBulidException(String message) {
        super(String.format("Hmi Bulid Error: %1s", message));
    }

}
