/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.svr;

import fi.iki.elonen.NanoHTTPD;

public enum PrivStatusCode implements NanoHTTPD.Response.IStatus {
    CONTINUE(101),
    OK(200),
    READY(290),

    REQ_UNKNOWN(490),
    REQ_INPUT_CMD(482),
    REQ_INPUT_CONVERT(481),
    REQ_INPUT_UNKNOWN(480),
    REQ_SEQ_TRIGGER(471),
    REQ_SEQ_UNKNOWN(470),

    REQ_TARGET_UNKNOWN(404),

    SRV_UNKNOWN(590),
    SRV_ACT_REMOTE(582),
    SRV_ACT_IMPLEMENTED(581),
    SRV_ACT_UNKNOWN(580);

    private final int value;

    PrivStatusCode(int requestStatus) {
        value = requestStatus;
    }

    @Override
    public int getStatusCode() {
        return value;
    }

    @Override
    public String getDescription() {
        return value + " CSC";
    }

    public boolean equals(int target) {
        return value == target;
    }
}
