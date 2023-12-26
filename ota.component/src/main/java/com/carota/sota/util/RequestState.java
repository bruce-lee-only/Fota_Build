/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sota.util;

public class RequestState {

    public final int code;
    public final String message;

    public RequestState(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "REQ [" + code + "] " + message;
    }
}
