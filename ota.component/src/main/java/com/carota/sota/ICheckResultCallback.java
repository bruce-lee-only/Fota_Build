/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota;

import com.carota.sota.store.AppData;

public interface ICheckResultCallback {
    //code 见IActionSOTA

    //errorCode
    public static final int NOT_SUPPORT = 1;
    public static final int CONNECT_ERROR = 2;

    void onConnected(int code);
    void onResult(AppData appData);
    void onError(int errorCode, String msg);
}
