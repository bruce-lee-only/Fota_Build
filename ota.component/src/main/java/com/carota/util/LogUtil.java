/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.content.Context;

import com.carota.component.BuildConfig;
import com.momock.util.Logger;

public class LogUtil {

    public final static String TAG_RPC_MDA = "ActionMDA";
    public final static String TAG_RPC_SDA = "ActionSDA";
    public final static String TAG_RPC_SVR = "ActionAPI";
    public final static String TAG_RPC_SH = "ActionSH";
    public final static String TAG_RPC_DM = "ActionDM";
    public final static String TAG_RPC_VSI = "ActionVSI";
    public final static String TAG_RPC_DTC = "ActionDTC";
    public final static String TAG_RPC_SOTA = "ActionSOTA";
    public final static String TAG_RPC_CMH = "ActionCMH";

    public static void initLogger(Context context) {
        Logger.open(context, "carota", 20, Logger.LEVEL_DEBUG);
        Logger.info("CAROTA CORE VER %s", BuildConfig.VERSION_NAME);
    }

}
