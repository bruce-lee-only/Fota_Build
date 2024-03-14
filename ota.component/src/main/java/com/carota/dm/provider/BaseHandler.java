/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dm.provider;

import android.content.Context;

import com.carota.dm.task.ITaskManager;
import com.carota.svr.SimpleHandler;

public abstract class BaseHandler extends SimpleHandler {

    protected final ITaskManager mTm;
    protected final Context mContext;

    public BaseHandler(Context context, ITaskManager tm) {
        super();
        mContext = context;
        mTm = tm;
    }
}
